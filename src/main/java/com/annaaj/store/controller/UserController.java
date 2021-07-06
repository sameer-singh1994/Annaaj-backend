package com.annaaj.store.controller;


import com.annaaj.store.dto.ResponseDto;
import com.annaaj.store.exceptions.AuthenticationFailException;
import com.annaaj.store.exceptions.CustomException;
import com.annaaj.store.model.UserProfile;
import com.annaaj.store.repository.UserRepository;
import com.annaaj.store.service.AuthenticationService;
import com.annaaj.store.service.UserService;
import com.annaaj.store.dto.user.SignInDto;
import com.annaaj.store.dto.user.SignInResponseDto;
import com.annaaj.store.dto.user.SignupDto;
import com.annaaj.store.model.User;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("user")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    UserService userService;

    @GetMapping("/all")
    public List<User> findAllUser(@RequestParam("token") String token) throws AuthenticationFailException {
        authenticationService.authenticate(token);
        return userRepository.findAll();
    }

    @PostMapping("/signup")
    public ResponseDto Signup(@RequestBody SignupDto signupDto, HttpServletRequest request) throws CustomException {
        return userService.signUp(signupDto, getSiteURL(request));
    }

    @PostMapping("/getAssociatedUsers")
    public List<User> getAllAssociatedUsers(@RequestParam("token") String token,
                                            @Param("communityLeaderId") String communityLeaderId) throws CustomException {
        authenticationService.authenticate(token);
        return userRepository.findAssociatedUsers(Long.getLong(communityLeaderId));
    }

    @GetMapping("/verify")
    public String verifyUser(@Param("code") String code) {
        if (userService.verify(code)) {
            return "verify_success";
        } else {
            return "verify_fail";
        }
    }

    //TODO token should be updated
    @PostMapping("/signIn")
    public SignInResponseDto Signup(@RequestBody SignInDto signInDto) throws CustomException {
        return userService.signIn(signInDto);
    }


    private String getSiteURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();
        return siteURL.replace(request.getServletPath(), "");
    }

//    @PostMapping("/updateUser")
//    public ResponseDto updateUser(@RequestParam("token") String token, @RequestBody UserUpdateDto userUpdateDto) {
//        authenticationService.authenticate(token);
//        return userService.updateUser(token, userUpdateDto);
//    }


//    @PostMapping("/createUser")
//    public ResponseDto updateUser(@RequestParam("token") String token, @RequestBody UserCreateDto userCreateDto)
//            throws CustomException, AuthenticationFailException {
//        authenticationService.authenticate(token);
//        return userService.createUser(token, userCreateDto);
//    }
}