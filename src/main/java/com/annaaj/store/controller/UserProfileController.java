package com.annaaj.store.controller;

import com.annaaj.store.enums.Role;
import com.annaaj.store.model.User;
import com.annaaj.store.service.AuthenticationService;
import com.annaaj.store.service.UserProfileService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.validation.Valid;

import com.annaaj.store.model.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.annaaj.store.common.ApiResponse;

@RestController
@RequestMapping("/user")
public class UserProfileController {

	@Autowired private UserProfileService userProfileService;

	@Autowired private AuthenticationService authenticationService;

	@ApiOperation(value = "get profile of all users, ROLE = ADMIN")
	@GetMapping("/")
	public ResponseEntity<List<UserProfile>> getUsers(@ApiParam @RequestParam("token") String token) {
		authenticationService.authenticate(token, Collections.singletonList(Role.admin));
		List<UserProfile> dtos = userProfileService.listProfiles();
		return new ResponseEntity<>(dtos, HttpStatus.OK);
	}

	@ApiOperation(value = "get user profile(add billing address which will be copied to the user object as well), ROLE = USER, COMMUNITY_LEADER")
	@PostMapping("/add")
	public ResponseEntity<ApiResponse> addProfile(
			@ApiParam(value = "user profile object(can leave id as it is)") @RequestBody @Valid UserProfile profile,
			@ApiParam @RequestParam("token") String token) {
		authenticationService.authenticate(token, Arrays.asList(Role.user, Role.communityLeader));
		User user = authenticationService.getUser(token);
		if (!user.getId().equals(profile.getId()))
			return new ResponseEntity<>(new ApiResponse(false, "User was not found."), HttpStatus.NOT_FOUND);
		userProfileService.addProfile(profile);
		return new ResponseEntity<>(new ApiResponse(true, "Profile has been created."), HttpStatus.CREATED);
	}	
}
