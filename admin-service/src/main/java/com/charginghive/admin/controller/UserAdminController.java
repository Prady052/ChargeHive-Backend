package com.charginghive.admin.controller;

import com.charginghive.admin.dto.*;
import com.charginghive.admin.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserAdminController {

    private final UserManagementService userManagementService;


    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.info("Received request to get all users.");
        List<UserDto> users = userManagementService.getAllUsers();
        log.info("Found {} users.", users.size());
        return ResponseEntity.ok(users);
    }

    // newly added
    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@RequestBody UserCreateRequest request) {
        UserDto created = userManagementService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // newly added
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.getUserById(id));
    }

    // newly added
    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id,@RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userManagementService.updateUser(id, request));
    }

    // newly added
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userManagementService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    // newly added
    @PutMapping("/roles/{id}")
    public ResponseEntity<Void> updateRole(@PathVariable Long id,@RequestBody RoleUpdateRequest request) {
        userManagementService.updateRole(id, request);
        return ResponseEntity.noContent().build();
    }


    // must return user with recent 5-10 bookings
    @GetMapping("/users/{userId}/details")
    public ResponseEntity<UserDetailDto> getUserWithBooking(@PathVariable("userId") Long userId) {
        log.info("Received request to get user details with bookings for userId: {}", userId);
        UserDetailDto user = userManagementService.getUserDetials(userId);
        return ResponseEntity.ok(user);
    }

}
