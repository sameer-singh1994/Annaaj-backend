package com.annaaj.store.controller;

import com.annaaj.store.model.User;
import com.annaaj.store.service.AuthenticationService;
import com.annaaj.store.service.UserProfileService;
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

	@GetMapping("/")
	public ResponseEntity<List<UserProfile>> getUsers() {
		List<UserProfile> dtos = userProfileService.listProfiles();
		return new ResponseEntity<List<UserProfile>>(dtos, HttpStatus.OK);
	}

	@PostMapping("/add")
	public ResponseEntity<ApiResponse> addProfile(@RequestBody @Valid UserProfile profile, @RequestParam("token") String token) {
		authenticationService.authenticate(token);
		User user = authenticationService.getUser(token);
		if (!user.getId().equals(profile.getId()))
			return new ResponseEntity<>(new ApiResponse(false, "User was not found."), HttpStatus.NOT_FOUND);
		userProfileService.addProfile(profile);
		return new ResponseEntity<>(new ApiResponse(true, "Profile has been created."), HttpStatus.CREATED);
	}	
}
