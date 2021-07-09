package com.annaaj.store.controller;


import com.annaaj.store.common.ApiResponse;
import com.annaaj.store.dto.ResponseDto;
import com.annaaj.store.enums.Role;
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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @ApiOperation(value = "get all users, ROLE = ADMIN")
    @GetMapping("/all")
    public List<User> findAllUser(
        @ApiParam @RequestParam("token") String token) throws AuthenticationFailException {
        authenticationService.authenticate(token, Collections.singletonList(Role.admin));
        return userRepository.findAll();
    }

    @ApiOperation(value = "signup")
    @PostMapping("/signup")
    public ResponseDto Signup(
        @ApiParam(value = "for Community Leader the value of communityLeader field can be left as it is,"
            + " but it is mandatory in case of a user, (verification email will be sent after this API call)") @RequestBody SignupDto signupDto,
        HttpServletRequest request) throws CustomException {
        return userService.signUp(signupDto, getSiteURL(request));
    }

    @ApiOperation(value = "get user from token, ROLE = ADMIN, USER, COMMUNITY_LEADER")
    @PostMapping("/getUser")
    public User getUser(
        @ApiParam @RequestParam("token") String token) {
        authenticationService.authenticate(token, Arrays.asList(Role.user, Role.communityLeader, Role.admin));
        return authenticationService.getUser(token);
    }

    @ApiOperation(value = "get community leader connected to you, ROLE = USER")
    @GetMapping("/getCommunityLeader")
    public User getCommunityLeader(
        @ApiParam @RequestParam("token") String token) throws CustomException {
        authenticationService.authenticate(token, Collections.singletonList(Role.user));
        User user = authenticationService.getUser(token);
        Optional<User> communityLeader = userRepository.findById(user.getCommunityLeaderId());
        if (communityLeader.isPresent()) {
            return communityLeader.get();
        }
        throw new CustomException("communityLeaderId not present for user");
    }

    @ApiOperation(value = "get all users connected to you, ROLE = COMMUNITY_LEADER")
    @GetMapping("/getAssociatedUsers")
    public List<User> getAllAssociatedUsers(
        @ApiParam @RequestParam("token") String token) throws CustomException {
        authenticationService.authenticate(token, Collections.singletonList(Role.communityLeader));
        return userRepository.findAssociatedUsers(authenticationService.getUser(token).getId());
    }

    @ApiOperation(value = "for email verification(not to be used from here)")
    @GetMapping("/verify")
    public String verifyUser(@Param("code") String code) {
        if (userService.verify(code)) {
            return "verify_success";
        } else {
            return "verify_fail";
        }
    }

    @ApiOperation(value = "signIn")
    //TODO token should be updated
    @PostMapping("/signIn")
    public SignInResponseDto signIn(
        @ApiParam(value = "email and password") @RequestBody SignInDto signInDto) throws CustomException {
        return userService.signIn(signInDto);
    }

    @ApiOperation(value = "signOut (signIn token will be rotated), ROLE = COMMUNITY_LEADER, USER, ADMIN")
    @PostMapping("/signOut")
    public ResponseEntity<ApiResponse> signOut(
        @ApiParam @RequestParam("token") String token) throws CustomException {
        authenticationService.authenticate(token, Arrays.asList(Role.user, Role.communityLeader, Role.admin));
        userService.signOut(token);
        return new ResponseEntity<>(
            new ApiResponse(true, "User has been successfully logged out"), HttpStatus.OK);
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
