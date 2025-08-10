package com.charginghive.admin.service;

import com.charginghive.admin.customException.UserNotFoundException;
import com.charginghive.admin.dto.*;
import com.charginghive.admin.model.AuditLog;
import com.charginghive.admin.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Service
@Slf4j
public class AdminService {

    private final RestClient userClient;
    private final RestClient stationClient;
    private final RestClient bookingClient;
    private final AuditLogRepository auditLogRepository;

    public AdminService(RestClient.Builder restClientBuilder,AuditLogRepository auditLogRepository) {
        // Use the service name registered with Eureka, prefixed with "lb://"
        this.userClient = restClientBuilder.baseUrl("http://AUTH-SERVICE").build();
        this.stationClient = restClientBuilder.baseUrl("http://STATION-SERVICE").build();
        this.bookingClient = restClientBuilder.baseUrl("http://BOOKING-SERVICE").build();
        this.auditLogRepository = auditLogRepository;
    }

//    private String getUsernameById(Long adminId) {
//        try {
//            // Attempt to retrieve the user DTO.
//            AdminDto user = userClient.get()
//                    .uri("auth/get-by-id/{id}", adminId)
//                    .retrieve()
//                    .body(AdminDto.class);
//
//            if (user == null) {
//                throw new UserNotFoundException("Received an empty response for admin ID: " + adminId);
//            }
//
//            return user.getName();
//
//        } catch (WebClientResponseException e) {
//            throw new UserNotFoundException("Admin user with ID " + adminId + " not found.");
//        }
//    }

    // newly added: compute basic metrics by delegating to existing services
    public AdminMetricsDto getMetrics() {
        long totalStations = 0;
        long pendingStations = 0;
        long approvedStations = 0;
        long totalUsers = 0;

        try {
            List<StationDto> allStations = stationClient.get()
                    .uri("/stations")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<StationDto>>() {});
            if (allStations != null) {
                totalStations = allStations.size();
                approvedStations = allStations.stream().filter(StationDto::isApproved).count();
            }
        } catch (RestClientException e) {
            log.warn("Failed to fetch stations for metrics", e);
        }

        try {
            List<StationDto> unapproved = stationClient.get()
                    .uri("/stations/unapproved")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<StationDto>>() {});
            if (unapproved != null) {
                pendingStations = unapproved.size();
            }
        } catch (RestClientException e) {
            log.warn("Failed to fetch unapproved stations for metrics", e);
        }

        try {
            List<UserDto> users = userClient.get()
                    .uri("/auth/get-all")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<UserDto>>() {});
            if (users != null) {
                totalUsers = users.size();
            }
        } catch (RestClientException e) {
            log.warn("Failed to fetch users for metrics", e);
        }

        return AdminMetricsDto.builder()
                .totalStations(totalStations)
                .approvedStations(approvedStations)
                .pendingStations(pendingStations)
                .totalUsers(totalUsers)
                .build();
    }

    // --- Admin Service ---

    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findAll();
    }
}




















//
// --- station
//
//    @Transactional
//    public void approveOrRejectStation(Long userId, StationApprovalDto approvalDto) {
//        //Call the Station Service to update the station
//        log.info("userId = {}, approval DTO details: {}", userId, approvalDto);
//        stationClient.put()
//                .uri("/stations/update-status") // Assuming this is the endpoint in Station Service
//                .body(approvalDto)
//                .retrieve()
//                .toBodilessEntity();
//
//        //Create and save an audit log
//        String action = approvalDto.isApproved() ? "APPROVE_STATION" : "REJECT_STATION";
//        String details = "Station " + (approvalDto.isApproved() ? "approved" : "rejected") + " with reason: " + approvalDto.getReason();
//
//        AuditLog log = AuditLog.builder()
//                .adminUsername(getUsernameById(userId))
//                .action(action)
//                .targetEntity("Station")
//                .targetId(approvalDto.getStationId())
//                .details(details)
//                .build();
//        auditLogRepository.save(log);
//    }
//
//    public List<StationDto> getAllStations() {
//        return stationClient.get()
//                .uri("/stations")
//                .retrieve()
//                //telling station client that it has to convert json
//                //to list of stationDto's
//                .body(new ParameterizedTypeReference<List<StationDto>>() {});
//    }
//
//    public List<StationDto> getUnapprovedStations() {
//        return stationClient.get()
//                .uri("/stations/unapproved")
//                .retrieve()
//                .body(new ParameterizedTypeReference<List<StationDto>>() {});
//    }
