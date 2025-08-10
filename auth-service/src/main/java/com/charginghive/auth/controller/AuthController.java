package com.charginghive.auth.controller;

import com.charginghive.auth.dto.*;
import com.charginghive.auth.dto.AdminUserCreateRequest;
import com.charginghive.auth.dto.AdminUserUpdateRequest;
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
			UserResDto user = userService.saveUserDetails(credential);
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

	// to do make test with postman
	@PutMapping("/edit-user")
	public ResponseEntity<?> editUser(@RequestBody UserEditDto credential, @RequestHeader("X-User-Id") Long userId){
		log.info("update user details: {}", credential);
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.editUserDetails(credential, userId));
	}

	// get user by id (primary path)
	@GetMapping("/get-by-id/{id}")
	public ResponseEntity<?> getUserById(@PathVariable("id") Long id){
		return ResponseEntity.status(HttpStatus.OK).body(userService.getById(id));
	}

	// newly added: alias path for admin compatibility
//	@GetMapping("/../get-by-id/{id}")
//	public ResponseEntity<?> getUserByIdAlias(@PathVariable("id") Long id){
//		return ResponseEntity.status(HttpStatus.OK).body(userService.getById(id));
//	}

    // logout (stateless JWT; noop for now)
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("X-User-Id") Long userId,
                                            @Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(userId, req);
        return ResponseEntity.ok(Map.of("message", "Password changed"));
    }

    // newly added: admin endpoints (consumed by Admin service)
    //dto need phone number to be added and role must be enum
    @PostMapping("/admin/users")
    public ResponseEntity<UserDto> adminCreateUser(@Valid @RequestBody AdminUserCreateRequest req) {
        UserDto created = userService.createUserAdmin(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/admin/users/{id}")
    public ResponseEntity<UserDto> adminUpdateUser(@PathVariable Long id, @Valid @RequestBody AdminUserUpdateRequest req) {
        return ResponseEntity.ok(userService.updateUserAdmin(id, req));
    }

    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<Void> adminDeleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // role endpoints used by admin service; since roles are enum, update/delete are no-ops here and return 204
//    @PutMapping("/admin/roles/{id}")
//    public ResponseEntity<Void> adminUpdateRoleNoop(@PathVariable Long id, @RequestBody Map<String, Object> body) {
//        // newly added: no-op because roles are enum in this service
//        return ResponseEntity.noContent().build();
//    }
//
//    @DeleteMapping("/admin/roles/{id}")
//    public ResponseEntity<Void> adminDeleteRoleNoop(@PathVariable Long id) {
//        // newly added: no-op because roles are enum in this service
//        return ResponseEntity.noContent().build();
//    }

    @PostMapping("/admin/users/{id}/roles")
    public ResponseEntity<Void> adminAssignRoles(@PathVariable Long id, @Valid @RequestBody AdminAssignRolesRequest req) {
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
