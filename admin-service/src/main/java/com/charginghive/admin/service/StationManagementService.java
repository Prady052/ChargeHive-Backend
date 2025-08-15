package com.charginghive.admin.service;

import com.charginghive.admin.customException.UserNotFoundException;
import com.charginghive.admin.dto.AdminDto;
import com.charginghive.admin.dto.StationApprovalDto;
import com.charginghive.admin.dto.StationDto;
import com.charginghive.admin.model.AuditLog;
import com.charginghive.admin.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
@Slf4j
public class StationManagementService {

    private final RestClient userClient;
    private final RestClient stationClient;
    private final AuditLogRepository auditLogRepository;


    public StationManagementService(RestClient.Builder restClientBuilder, AuditLogRepository auditLogRepository) {
        this.userClient = restClientBuilder.baseUrl("http://AUTH-SERVICE").build();
        this.stationClient = restClientBuilder.baseUrl("http://STATION-SERVICE").build();
        this.auditLogRepository = auditLogRepository;
    }


    private String getUsernameById(Long adminId) {
        try {
            AdminDto user = userClient.get()
                    .uri("auth/get-by-id/{id}", adminId)
                    .retrieve()
                    .body(AdminDto.class);

            if (user == null) {
                throw new UserNotFoundException("Received an empty response for admin ID: " + adminId);
            }
            return user.getName();
        } catch (RestClientResponseException e) {
            throw new UserNotFoundException("Admin user with ID " + adminId + " not found.");
        }
    }


    @Transactional
    public void approveOrRejectStation(Long userId, StationApprovalDto approvalDto) {
        log.info("userId = {}, approval DTO details: {}", userId, approvalDto);
        stationClient.put()
                .uri("/stations/update-status")
                .body(approvalDto)
                .retrieve()
                .toBodilessEntity();

        String action = approvalDto.isApproved() ? "APPROVE_STATION" : "REJECT_STATION";
        String details = "Station " + (approvalDto.isApproved() ? "approved" : "rejected")
                + " with reason: " + approvalDto.getReason();

        AuditLog logEntry = AuditLog.builder()
                .adminUsername(getUsernameById(userId))
                .action(action)
                .targetEntity("Station")
                .targetId(approvalDto.getStationId())
                .details(details)
                .build();
        auditLogRepository.save(logEntry);
    }


    @Transactional
    public void approveStationById(Long userId, Long stationId, String reason) {
        StationApprovalDto dto = new StationApprovalDto();
        dto.setStationId(stationId);
        dto.setApproved(true);
        dto.setReason(reason);
        approveOrRejectStation(userId, dto);
    }


    @Transactional
    public void rejectStationById(Long userId, Long stationId, String reason) {
        StationApprovalDto dto = new StationApprovalDto();
        dto.setStationId(stationId);
        dto.setApproved(false);
        dto.setReason(reason);
        approveOrRejectStation(userId, dto);
    }


    public List<StationDto> getAllStations() {
        return stationClient.get()
                .uri("/stations")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }


    public List<StationDto> getUnapprovedStations() {
        return stationClient.get()
                .uri("/stations/unapproved")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
