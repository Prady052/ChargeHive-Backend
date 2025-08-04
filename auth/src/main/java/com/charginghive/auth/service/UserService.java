package com.charginghive.auth.service;


import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.charginghive.auth.dto.UserRegistrationReq;
import com.charginghive.auth.entity.UserRegistration;
import com.charginghive.auth.repository.UserRepository;
import com.charginghive.auth.security.JwtUtils;

import lombok.AllArgsConstructor;

@Service
@Transactional
@AllArgsConstructor
public class UserService {

	private final UserRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final ModelMapper modelMapper;
	private final JwtUtils jwtUtils;
	
	public String saveUserDetails(UserRegistrationReq credential) {
		String mesg = "user regestration failed!";
		try {
			System.out.println("user passowrd is: "+credential.getPassword());
			credential.setPassword(passwordEncoder.encode(credential.getPassword()));	
			
			repository.save(modelMapper.map(credential, UserRegistration.class));
			mesg = "user registred successfully!";
		}
		catch(Exception ex) {
			System.out.println("Error occured during registering user "+ex.getMessage());
		}
		return mesg;
	}

	public String generateToken(Authentication validAuth) {
		return jwtUtils.generateJwtToken(validAuth);
	}

}
