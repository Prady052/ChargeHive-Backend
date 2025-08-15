package com.charginghive.auth.controller;

import com.charginghive.auth.dto.*;
import com.charginghive.auth.dto.AdminUserCreateRequest;
import com.charginghive.auth.dto.AdminUserUpdateRequest;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.charginghive.auth.service.UserService;

import lombok.AllArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@Slf4j
@Validated
public class AuthController {

	private final UserService userService;
	private final AuthenticationManager authenticationManager;
	private final ModelMapper modelMapper;

	@PostMapping("/register")
	public ResponseEntity<?> addNewUser(@RequestBody UserRegistrationReq credential){
		log.info("Registering user with email: {}", credential.getEmail());
		try {
			UserResDto user = userService.saveUserDetails(credential);
			return ResponseEntity.status(HttpStatus.CREATED).body(user);
		} catch (RuntimeException e) {
			log.warn("User registration failed: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error registering user. Please check the details and try again.");
		}
	}
	
	@PostMapping("/login")
	public ResponseEntity<?> authenticateAndGetTocken(@RequestBody UserSignInReq signInReq){

		try{
			log.info("Attempting sign-in for email: {}", signInReq.getEmail());
			Authentication authToken = new UsernamePasswordAuthenticationToken(signInReq.getEmail(), signInReq.getPassword());
			Authentication validAuth = authenticationManager.authenticate(authToken);
			AuthResponse authResponse = AuthResponse.builder()
					.user(modelMapper.map(validAuth.getPrincipal(), UserResDto.class))
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

	@PutMapping("/edit-user")
	public ResponseEntity<?> editUser(@RequestBody UserEditDto credential, @RequestHeader("X-User-Id") Long userId){
		log.info("update user details: {}", credential);
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.editUserDetails(credential, userId));
	}

	@GetMapping("/get-by-id/{id}")
	public ResponseEntity<?> getUserById(@PathVariable("id") Long id){
		return ResponseEntity.status(HttpStatus.OK).body(userService.getById(id));
	}

    @GetMapping("/me")
    public ResponseEntity<?> getDetails(@RequestHeader("X-User-Id") Long userId){
        log.info("Received request to get user details for userId: {}", userId);

        return ResponseEntity.status(HttpStatus.OK).body(userService.getById(userId));
    }

    // logout (stateless JWT; noop for now)
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("X-User-Id") Long userId,
                                            @RequestBody ChangePasswordRequest req) {
        userService.changePassword(userId, req);
        return ResponseEntity.ok(Map.of("message", "Password changed"));
    }

    @PostMapping("/admin/users")
    public ResponseEntity<UserDto> adminCreateUser(@RequestBody AdminUserCreateRequest req) {
        UserDto created = userService.createUserAdmin(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/admin/users/{id}")
    public ResponseEntity<UserDto> adminUpdateUser(@PathVariable Long id,@RequestBody AdminUserUpdateRequest req) {
        return ResponseEntity.ok(userService.updateUserAdmin(id, req));
    }

    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<Void> adminDeleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/users/{id}/roles")
    public ResponseEntity<Void> adminAssignRoles(@PathVariable Long id,@RequestBody AdminAssignRolesRequest req) {
        userService.assignRoles(id, req);
        return ResponseEntity.noContent().build();
    }

    //check if user exists - required by booking service
    @GetMapping("/get-by-id/{id}/exists")
    public ResponseEntity<Boolean> checkUserExists(@PathVariable("id") Long id){
        log.info("Checking if user exists with ID: {}", id);
        boolean exists = userService.userExists(id);
        return ResponseEntity.ok(exists);
    }

}
