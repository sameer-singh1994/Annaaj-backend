package com.annaaj.store.service;


import com.annaaj.store.config.AdminConfig;
import com.annaaj.store.dto.ResponseDto;
import com.annaaj.store.exceptions.AuthenticationFailException;
import com.annaaj.store.exceptions.CustomException;
import com.annaaj.store.model.Order;
import com.annaaj.store.repository.UserRepository;
import com.annaaj.store.config.MessageStrings;
import com.annaaj.store.dto.user.SignInDto;
import com.annaaj.store.dto.user.SignInResponseDto;
import com.annaaj.store.dto.user.SignupDto;
import com.annaaj.store.dto.user.UserCreateDto;
import com.annaaj.store.enums.ResponseStatus;
import com.annaaj.store.enums.Role;
import com.annaaj.store.model.AuthenticationToken;
import com.annaaj.store.model.User;
import com.annaaj.store.utils.Helper;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.Optional;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import net.bytebuddy.utility.RandomString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.annaaj.store.config.MessageStrings.USER_CREATED;


@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    private AdminConfig adminConfig;

    @Autowired
    private JavaMailSender mailSender;

    Logger logger = LoggerFactory.getLogger(UserService.class);


    public ResponseDto signUp(SignupDto signupDto, String siteUrl) throws CustomException {
        // Check to see if the current email address has already been registered.
        if (Helper.notNull(userRepository.findByEmail(signupDto.getEmail()))) {
            // If the email address has been registered then throw an exception.
            throw new CustomException("User already exists");
        }

        if (Role.fromString(signupDto.getUserRole()).equals(Role.admin)) {
            checkAdmin(signupDto);
        }
        // first encrypt the password
        String encryptedPassword = signupDto.getPassword();
        try {
            encryptedPassword = hashPassword(signupDto.getPassword());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            logger.error("hashing password failed {}", e.getMessage());
        }

        User user = new User(signupDto.getFirstName(), signupDto.getLastName(), signupDto.getEmail(),
                             encryptedPassword, signupDto.getPhoneNumber());

        Role role = Role.fromString(signupDto.getUserRole());
        user.setRole(role);
        if(role.equals(Role.user)) {
            if (Objects.isNull(signupDto.getCommunityLeaderId())) {
                throw new CustomException("communityLeaderId not present for user");
            }
            Optional <User> communityLeader = userRepository.findById(signupDto.getCommunityLeaderId());
            if (communityLeader.isPresent()) {
                user.setCommunityLeaderId(signupDto.getCommunityLeaderId());
            }
            else {
                throw new CustomException("communityLeaderId not present for user");
            }
        }
        else if (role.equals(Role.communityLeader)) {
            user.setTotalEarnings(0L);
        }


        String randomCode = RandomString.make(64);
        user.setVerificationCode(randomCode);
        user.setEnabled(false);

        if (role.equals(Role.admin)) {
            user.setVerificationCode(null);
            user.setEnabled(true);
        }

        User createdUser;
        try {
            // save the User
            createdUser = userRepository.save(user);
            // generate token for user
            final AuthenticationToken authenticationToken = new AuthenticationToken(createdUser);
            // save token in database
            authenticationService.saveConfirmationToken(authenticationToken);
            if (!role.equals(Role.admin)) {
                //send verification email
                sendVerificationEmail(createdUser, siteUrl);
            }
            // success in creating
            return new ResponseDto(ResponseStatus.success.toString(), USER_CREATED);
        } catch (Exception e) {
            // handle signup error
            throw new CustomException(e.getMessage());
        }
    }

    private void sendVerificationEmail(User user, String siteURL)
        throws UnsupportedEncodingException, MessagingException {
        String toAddress = user.getEmail();
        String fromAddress = "support@annaaj.com";
        String senderName = "Annaaj";
        String subject = "Please verify your registration";
        String content = "Dear [[name]],<br>"
            + "Please click the link below to verify your registration:<br>"
            + "<h3><a href=\"[[URL]]\" target=\"_self\">Tasty and healthy food loading... (click here)</a></h3>"
            + "Thank you,<br>"
            + "The Annaaj team.";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", user.getFullName());
        String verifyURL = siteURL + "/user/verify?code=" + user.getVerificationCode();

        content = content.replace("[[URL]]", verifyURL);

        helper.setText(content, true);

        mailSender.send(message);

    }

    public boolean verify(String verificationCode) {
        User user = userRepository.findByVerificationCode(verificationCode);

        if (user == null || user.isEnabled()) {
            return false;
        } else {
            user.setVerificationCode(null);
            user.setEnabled(true);
            userRepository.save(user);

            return true;
        }
    }

    public SignInResponseDto signIn(SignInDto signInDto) throws CustomException {
        // first find User by email
        User user = userRepository.findByEmail(signInDto.getEmail());
        if(!Helper.notNull(user)){
            throw  new AuthenticationFailException("user not present");
        }
        try {
            // check if password is right
            if (!user.getPassword().equals(hashPassword(signInDto.getPassword()))){
                // passowrd doesnot match
                throw  new AuthenticationFailException(MessageStrings.WRONG_PASSWORD);
            }

            if (!user.isEnabled()) {
                throw new AuthenticationFailException("Email Id not verified");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            logger.error("hashing password failed {}", e.getMessage());
            throw new CustomException(e.getMessage());
        }

        AuthenticationToken token = authenticationService.getToken(user);

        if(!Helper.notNull(token)) {
            // token not present
            throw new CustomException("token not present");
        }

        return new SignInResponseDto ("success", token.getToken());
    }

    public void signOut(String token) {
        User user = authenticationService.getUser(token);
        authenticationService.removeAuthenticationToken(token);
        authenticationService.saveConfirmationToken(new AuthenticationToken(user));
    }

    String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
        byte[] digest = md.digest();
        String myHash = DatatypeConverter
                .printHexBinary(digest).toUpperCase();
        return myHash;
    }

    public ResponseDto createUser(String token, UserCreateDto userCreateDto) throws CustomException, AuthenticationFailException {
        User creatingUser = authenticationService.getUser(token);
        if (!canCrudUser(creatingUser.getRole())) {
            // user can't create new user
            throw  new AuthenticationFailException(MessageStrings.USER_NOT_PERMITTED);
        }
        String encryptedPassword = userCreateDto.getPassword();
        try {
            encryptedPassword = hashPassword(userCreateDto.getPassword());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            logger.error("hashing password failed {}", e.getMessage());
        }

        User user = new User(userCreateDto.getFirstName(), userCreateDto.getLastName(), userCreateDto.getEmail(),
                             encryptedPassword, userCreateDto.getPhoneNumber());
        user.setRole(userCreateDto.getRole());
        User createdUser;
        try {
            createdUser = userRepository.save(user);
            final AuthenticationToken authenticationToken = new AuthenticationToken(createdUser);
            authenticationService.saveConfirmationToken(authenticationToken);
            return new ResponseDto(ResponseStatus.success.toString(), USER_CREATED);
        } catch (Exception e) {
            // handle user creation fail error
            throw new CustomException(e.getMessage());
        }

    }

    public void updateUserAddress(String userEmail, String address){
        User user = userRepository.findByEmail(userEmail);
        if(!Helper.notNull(user)){
            throw  new AuthenticationFailException("user not present");
        }
        user.setAddress(address);
        userRepository.save(user);
    }

    public void updateCommunityLeaderIncentive(User user, Order order) {
        user.setTotalEarnings(user.getTotalEarnings() + order.getIncentive());
        userRepository.save(user);
    }

    private void checkAdmin(SignupDto signupDto) {
        if (!signupDto.getEmail().equals(adminConfig.getUserEmail()) ||
            !signupDto.getPassword().equals(adminConfig.getPassword())) {
            throw new CustomException("Invalid email or password for admin");
        }
    }

    boolean canCrudUser(Role role) {
        if (role == Role.admin || role == Role.communityLeader) {
            return true;
        }
        return false;
    }

    boolean canCrudUser(User userUpdating, Integer userIdBeingUpdated) {
        Role role = userUpdating.getRole();
        // admin and manager can crud any user
        if (role == Role.admin || role == Role.communityLeader) {
            return true;
        }
        // user can update his own record, but not his role
        if (role == Role.user && userUpdating.getId() == userIdBeingUpdated) {
            return true;
        }
        return false;
    }
}
