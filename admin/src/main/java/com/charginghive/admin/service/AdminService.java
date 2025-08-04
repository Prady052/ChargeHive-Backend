package com.charginghive.admin.service;

import com.charginghive.admin.customException.UserNotFoundException;
import com.charginghive.admin.dto.StationApprovalDto;
import com.charginghive.admin.dto.StationDto;
import com.charginghive.admin.dto.UserDto;
import com.charginghive.admin.dto.UserStatusUpdateDto;
import com.charginghive.admin.model.AuditLog;
import com.charginghive.admin.repository.AuditLogRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    private final RestClient userClient;
    private final RestClient stationClient;
    private final AuditLogRepository auditLogRepository;

    public AdminService(@Qualifier("userRestClient") RestClient userClient,
                        @Qualifier("stationRestClient") RestClient stationClient,
                        AuditLogRepository auditLogRepository) {
        this.userClient = userClient;
        this.stationClient = stationClient;
        this.auditLogRepository = auditLogRepository;
    }

    private String getUsernameById(Long adminId) {

        Optional<UserDto> userOptional = userClient.get()
                .uri("/api/users/{id}", adminId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new UserNotFoundException("Admin user with ID " + adminId + " not found.");
                })
                .toEntity(UserDto.class)
                .getBody() != null ? Optional.ofNullable(userClient.get().uri("/api/users/{id}", adminId).retrieve().body(UserDto.class)) : Optional.empty();

        return userOptional
                .map(UserDto::getUsername)
                .orElseThrow(() -> new UserNotFoundException("Username not found for admin ID: " + adminId));
    }

// --- station

    @Transactional
    public void approveOrRejectStation(Long userId, StationApprovalDto approvalDto) {
        // 1. Call the Station Service to update the station
        System.out.println(userId);
        stationClient.put()
                .uri("/api/stations/update-status") // Assuming this is the endpoint in Station Service
                .body(approvalDto)
                .retrieve()
                .toBodilessEntity();

        // 2. Create and save an audit log
        String action = approvalDto.isApproved() ? "APPROVE_STATION" : "REJECT_STATION";
        String details = "Station " + (approvalDto.isApproved() ? "approved" : "rejected") + " with reason: " + approvalDto.getReason();

//        AuditLog log = AuditLog.builder()
//                .adminUsername(getUsernameById(userId))
//                .action(action)
//                .targetEntity("Station")
//                .targetId(approvalDto.getStationId())
//                .details(details)
//                .build();
        AuditLog log = AuditLog.builder()
                .adminUsername("admin 1")
                .action(action)
                .targetEntity("Station")
                .targetId(approvalDto.getStationId())
                .details(details)
                .build();
        auditLogRepository.save(log);
    }

    public List<StationDto> getAllStations() {
        return stationClient.get()
                .uri("/api/stations")
                .retrieve()
                .body(new ParameterizedTypeReference<List<StationDto>>() {});
    }

    public List<StationDto> getUnapprovedStations() {
        return stationClient.get()
                .uri("/api/stations/unapproved")
                .retrieve()
                .body(new ParameterizedTypeReference<List<StationDto>>() {});
    }

    // --- User

    @Transactional
    public void blockOrUnblockUser(Long userId, UserStatusUpdateDto statusDto) {
        // 1. Call the User Service to update the user's status
        userClient.put()
                .uri("/api/users/update-status") // Assuming this is the endpoint in User Service
                .body(statusDto)
                .retrieve()
                .toBodilessEntity();

        // 2. Create and save an audit log
        String action = statusDto.isEnabled() ? "UNBLOCK_USER" : "BLOCK_USER";
        String details = "User account " + (statusDto.isEnabled() ? "unblocked" : "blocked");

        AuditLog log = AuditLog.builder()
                .adminUsername(getUsernameById(userId))
                .action(action)
                .targetEntity("User")
                .targetId(statusDto.getUserId())
                .details(details)
                .build();
        auditLogRepository.save(log);
    }

    public List<UserDto> getAllUsers() {
        return userClient.get()
                .uri("/api/users")
                .retrieve()
                .body(new ParameterizedTypeReference<List<UserDto>>() {});
    }

    // --- Admin Service ---

    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findAll();
    }
}
