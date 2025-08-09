package com.charginghive.auth.controller;

import com.charginghive.auth.dto.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.charginghive.auth.service.UserService;

import lombok.AllArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {

	private final UserService userService;
	private final AuthenticationManager authenticationManager;
	private final ModelMapper modelMapper;

	@PostMapping("/register")
	public ResponseEntity<?> addNewUser(@Valid @RequestBody UserRegistrationReq credential){
		log.info("Registering user with email: {}", credential.getEmail());
		try {
			UserDto user = userService.saveUserDetails(credential);
			return ResponseEntity.status(HttpStatus.CREATED).body(user);
		} catch (RuntimeException e) {
			log.warn("User registration failed: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error registering user. Please check the details and try again.");
		}
	}
	
	@PostMapping("/login")
	public ResponseEntity<?> authenticateAndGetTocken(@Valid @RequestBody UserSignInReq signInReq){

		try{
			log.info("Attempting sign-in for email: {}", signInReq.getEmail());
			Authentication authToken = new UsernamePasswordAuthenticationToken(signInReq.getEmail(), signInReq.getPassword());
			log.debug("Before authentication - isAuthenticated: {}", authToken.isAuthenticated());

			Authentication validAuth = authenticationManager.authenticate(authToken);
			log.debug("After authentication - isAuthenticated: {}", validAuth.isAuthenticated());
			log.debug("Authenticated principal: {}", validAuth.getPrincipal());
			AuthResponse authResponse = AuthResponse.builder()
					.user(modelMapper.map(validAuth.getPrincipal(), UserDto.class))
					.token(userService.generateToken(validAuth))
					.build();

			return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
		} catch (BadCredentialsException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("error", "Invalid email or password"));
		} catch (AccessDeniedException ex) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("error", "You do not have permission to access this resource"));
		}
	}

	@GetMapping("/get-all")
	public ResponseEntity<?> getAllUsers(){
		return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsers());
	}


	//to do make test with postman
	@PutMapping("/edit-user")
	public ResponseEntity<?> editUser(@RequestBody UserEdirDto credential, @RequestHeader("X-User-Id") Long userId){
		log.info("update user details: "+credential);
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.editUserDetails(credential, userId));
	}

	//get user by id!
	@GetMapping("/get-by-id/{id}")
	public ResponseEntity<?> getUserById(@PathVariable("id") Long id){
		return ResponseEntity.status(HttpStatus.OK).body(userService.getById(id));
	}
}
