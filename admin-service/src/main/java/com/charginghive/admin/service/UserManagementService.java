package com.charginghive.admin.service;

import com.charginghive.admin.customException.UserNotFoundException;
import com.charginghive.admin.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Remove;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode; // newly added
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// newly added
@Service
@Slf4j
public class UserManagementService { // newly added

    private final RestClient userClient; // newly added
    private final RestClient bookingClient;
    // newly added
    public UserManagementService(RestClient.Builder restClientBuilder) {
        this.userClient = restClientBuilder.baseUrl("http://AUTH-SERVICE").build();
        this.bookingClient = restClientBuilder.baseUrl("http://BOOKING-SERVICE").build();
    }

    // edited: removed try/catch; exceptions handled by GlobalExceptionHandler
    public UserDto createUser(UserCreateRequest request) {
        return userClient.post()
                .uri("/auth/admin/users")
                .body(request)
                .retrieve()
                .body(UserDto.class);
    }

    // edited: removed try/catch; 404 mapping via onStatus, others handled by GlobalExceptionHandler
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

    // edited: removed try/catch; delegate error handling to GlobalExceptionHandler
    public UserDto updateUser(Long id, UserUpdateRequest request) {
        return userClient.put()
                .uri("/auth/admin/users/{id}", id)
                .body(request)
                .retrieve()
                .body(UserDto.class);
    }

    // edited: removed try/catch; downstream errors handled globally
    public void deactivateUser(Long id) {
        userClient.delete()
                .uri("/auth/admin/users/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    // edited: removed try/catch; rely on GlobalExceptionHandler
    public void updateRole(Long roleId, RoleUpdateRequest request) {
        userClient.put()
                .uri("/auth/admin/roles/{id}", roleId)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    // edited: removed try/catch; downstream errors handled globally
    public void deleteRole(Long roleId) {
        userClient.delete()
                .uri("/auth/admin/roles/{id}", roleId)
                .retrieve()
                .toBodilessEntity();
    }

    // edited: removed try/catch; rely on GlobalExceptionHandler
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

        try {
            // make synchronous call, handle 404 explicitly via onStatus
            UserDto userDto = userClient.get()
                    .uri("/get-by-id/{id}", userId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        // Response gives you access to status so you can inspect it
                        if (response.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                            throw new UserNotFoundException(userId.toString());
                        }
                        throw new RuntimeException("User service returned 4xx: " + response.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError,
                            (request, response) -> { throw new RuntimeException("User service returned 5xx: " + response.getStatusCode()); })
                    .body(UserDto.class); // synchronous, blocking conversion

            if (userDto == null) {
                throw new RuntimeException("User service returned empty body for id: " + userId);
            }

            BookingResponseDto[] arr = bookingClient.get()
                    .uri("/bookings/{userId}",userId)
                    .retrieve()
                    .body(BookingResponseDto[].class);

            return UserDetailDto.builder()
                    .user(userDto)
                    .bookings(arr != null ? Arrays.asList(arr) : Collections.emptyList())
                    .build();

        } catch (UserNotFoundException e) {
            throw e; // propagate as 404
        } catch (RestClientResponseException ex) {
            // error response: we can read body if needed via ex.getResponseBodyAsString()
            throw new RuntimeException("User service error: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString(), ex);
        } catch (RestClientException ex) {
            // network / IO / other client errors
            throw new RuntimeException("Failed to call user service", ex);
        }
    }

}
