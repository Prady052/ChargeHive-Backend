package com.charginghive.admin.service;

import com.charginghive.admin.customException.UserNotFoundException;
import com.charginghive.admin.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Service
@Slf4j
public class UserManagementService {

    private final RestClient userClient;
    private final RestClient bookingClient;

    public UserManagementService(RestClient.Builder restClientBuilder) {
        this.userClient = restClientBuilder.baseUrl("http://AUTH-SERVICE").build();
        this.bookingClient = restClientBuilder.baseUrl("http://BOOKING-SERVICE").build();
    }


    public UserDto createUser(UserCreateRequest request) {
        return userClient.post()
                .uri("/auth/admin/users")
                .body(request)
                .retrieve()
                .body(UserDto.class);
    }


    public UserDto getUserById(Long id) {
        return userClient.get()
                .uri("/get-by-id/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    if (res.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                    }
                })
                .body(UserDto.class);
    }


    public UserDto updateUser(Long id, UserUpdateRequest request) {
        return userClient.put()
                .uri("/auth/admin/users/{id}", id)
                .body(request)
                .retrieve()
                .body(UserDto.class);
    }

    public void deactivateUser(Long id) {
        userClient.delete()
                .uri("/auth/admin/users/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    public void updateRole(Long roleId, RoleUpdateRequest request) {
        userClient.put()
                .uri("/auth/admin/roles/{id}", roleId)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public void deleteRole(Long roleId) {
        userClient.delete()
                .uri("/auth/admin/roles/{id}", roleId)
                .retrieve()
                .toBodilessEntity();
    }

    public void assignRolesToUser(Long userId, AssignRolesRequest request) {
        userClient.post()
                .uri("/auth/admin/users/{id}/roles", userId)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public List<UserDto> getAllUsers() {
        return userClient.get()
                .uri("/auth/get-all")
                .retrieve()
                .body(new ParameterizedTypeReference<List<UserDto>>() {});
    }



    public UserDetailDto getUserDetials(Long userId) {

        UserDto userDto = userClient.get()
                .uri("/get-by-id/{id}", userId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    if (response.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                        throw new UserNotFoundException(userId.toString());
                    }
                    throw new RuntimeException("User service returned 4xx: " + response.getStatusCode());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new RuntimeException("User service returned 5xx: " + response.getStatusCode());
                })
                .body(UserDto.class);

        if (userDto == null) {
            throw new RuntimeException("User service returned empty body for id: " + userId);
        }

        BookingResponseDto[] arr = bookingClient.get()
                .uri("/bookings/{userId}", userId)
                .retrieve()
                .body(BookingResponseDto[].class);

        return UserDetailDto.builder()
                .user(userDto)
                .bookings(arr != null ? Arrays.asList(arr) : Collections.emptyList())
                .build();
    }


}
