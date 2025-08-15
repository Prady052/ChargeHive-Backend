package com.charginghive.auth.service;


import com.charginghive.auth.dto.*;
import com.charginghive.auth.dto.AdminUserCreateRequest;
import com.charginghive.auth.dto.AdminUserUpdateRequest;
import com.charginghive.auth.entity.UserRole;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.charginghive.auth.entity.UserRegistration;
import com.charginghive.auth.repository.UserRepository;
import com.charginghive.auth.security.JwtUtils;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.charginghive.auth.customException.NotFoundException;

@Service
@Transactional
@AllArgsConstructor
public class UserService {

	private final UserRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final ModelMapper modelMapper;
	private final JwtUtils jwtUtils;

	/**
	 * Registers a new user in the system.
	 * @param credential UserRegistrationReq DTO containing user signup details.
	 */
	public UserResDto saveUserDetails(UserRegistrationReq credential) {
		try {
			credential.setPassword(passwordEncoder.encode(credential.getPassword()));
			UserRegistration userEntity = modelMapper.map(credential, UserRegistration.class);
			UserRegistration savedUser = repository.save(userEntity);
			return modelMapper.map(savedUser, UserResDto.class);
		} catch (Exception ex) {
			System.out.println("Error occurred while registering user: " + ex.getMessage());
			throw new RuntimeException("User registration failed");
		}
	}

	/**
	 * Generates a JWT token for authenticated user.
	 */
	public String generateToken(Authentication validAuth) {
		return jwtUtils.generateJwtToken(validAuth);
	}

	/**
	 * Fetches all non-admin users from the database.
	 */
	public List<UserResDto> getAllUsers() {
		List<UserResDto> list = new ArrayList<>();
		try {
			list = repository.findAll().stream()
					.filter(p -> p.getUserRole() != UserRole.ROLE_ADMIN)
					.map(this::mapToDto)
					.collect(Collectors.toList());
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return list;
	}

	public String editUserDetails(UserEditDto credential, Long id) {
		String msg = "User update failed!";
		try {
			UserRegistration user = repository.findById(id)
					.orElseThrow(() -> new NotFoundException("User not found with ID: " + id)); // edited: custom 404

			user.setFirstName(credential.getFirstName());
			user.setLastName(credential.getLastName());
			user.setEmail(credential.getEmail());
			if (credential.getPassword() != null && !credential.getPassword().isBlank()) {
				user.setPassword(passwordEncoder.encode(credential.getPassword()));
			}
			repository.save(user);
			msg = "User updated successfully!";
        } catch (Exception ex) {
            System.out.println("Error updating user: " + ex.getMessage());
        }
        return msg;
    }

    /*
       new endpoints added for password change/reset
     */

    public void changePassword(Long userId, ChangePasswordRequest req) {
        UserRegistration user = repository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId)); // edited
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect"); // edited
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        repository.save(user);
    }

	public UserResDto getById(Long id) {
		UserRegistration user = repository.findById(id).orElseThrow(() -> new NotFoundException("User not found with ID: " + id)); // edited
		return modelMapper.map(user, UserResDto.class);
	}

	public UserResDto mapToDto(UserRegistration user) {
		return UserResDto.builder()
				.email(user.getEmail())
				.id(user.getId())
				.firstName(user.getFirstName())
                .phoneNumber(user.getPhoneNumber())
				.lastName(user.getLastName())
				.userRole(user.getUserRole())
				.build();
	}

	// ================== Admin operations (used by Admin service) ==================

	// admin create user with optional role
	public UserDto createUserAdmin(AdminUserCreateRequest req) {
		UserRegistration user = new UserRegistration();
		user.setFirstName(req.getFirstName());
		user.setLastName(req.getLastName());
		user.setEmail(req.getEmail());
		user.setPassword(passwordEncoder.encode(req.getPassword()));
        //have to change this logic
		UserRole role = UserRole.ROLE_DRIVER;
		if (req.getUserRole() != null) {
			try { role = req.getUserRole(); } catch (IllegalArgumentException ignored) {}
		}
		user.setUserRole(role);
		UserRegistration saved = repository.save(user);
		return modelMapper.map(saved, UserDto.class);
	}

	// admin update user with optional fields
	public UserDto updateUserAdmin(Long id, AdminUserUpdateRequest req) {
		UserRegistration user = repository.findById(id)
				.orElseThrow(() -> new NotFoundException("User not found with ID: " + id));
		if (req.getFirstName() != null) user.setFirstName(req.getFirstName());
		if (req.getLastName() != null) user.setLastName(req.getLastName());
		if (req.getEmail() != null) user.setEmail(req.getEmail());
		if (req.getPassword() != null && !req.getPassword().isBlank()) {
			user.setPassword(passwordEncoder.encode(req.getPassword()));
		}
		if (req.getUserRole() != null) {
			try { user.setUserRole(req.getUserRole()); } catch (IllegalArgumentException ignored) {}
		}
		UserRegistration saved = repository.save(user);
		return modelMapper.map(saved, UserDto.class);
	}

	// admin delete user
	public void deleteUser(Long id) {
		UserRegistration user = repository.findById(id)
				.orElseThrow(() -> new NotFoundException("User not found with ID: " + id));
		repository.delete(user);
	}

	// admin assign roles to user (take first role)
	public void assignRoles(Long id, AdminAssignRolesRequest req) {
		UserRegistration user = repository.findById(id)
				.orElseThrow(() -> new NotFoundException("User not found with ID: " + id));
		if (req.getRoles() != null && !req.getRoles().isEmpty()) {
			String roleStr = req.getRoles().get(0);
			try { user.setUserRole(UserRole.valueOf(roleStr)); } catch (IllegalArgumentException ignored) {}
			repository.save(user);
		}
	}

    /**
     * Check if a user exists by ID - required by booking service
     */
    public boolean userExists(Long id) {
        return repository.existsById(id);
    }

}
