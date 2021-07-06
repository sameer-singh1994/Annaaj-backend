package com.annaaj.store.service;

import com.annaaj.store.repository.UserProfileRepository;
import java.util.List;

import com.annaaj.store.model.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

	@Autowired private UserProfileRepository userRepo;

	@Autowired private UserService userService;
	

	public void addProfile(UserProfile userProfile) {
		userRepo.save(userProfile);
		userService.updateUserAddress(userProfile.getEmail(), userProfile.getBillingAddress());
	}
	
	public List<UserProfile> listProfiles(){
		return userRepo.findAll();		
	}
	
}
