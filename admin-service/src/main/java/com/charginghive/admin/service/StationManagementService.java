package com.charginghive.admin.service;

import com.charginghive.admin.customException.UserNotFoundException;
import com.charginghive.admin.dto.AdminDto;
import com.charginghive.admin.dto.StationApprovalDto;
import com.charginghive.admin.dto.StationDto;
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

import java.util.List;

// newly added
@Service
@Slf4j
public class StationManagementService { // newly added

    private final RestClient userClient;    // newly added
    private final RestClient stationClient; // newly added
    private final AuditLogRepository auditLogRepository; // newly added

    // newly added
    public StationManagementService(RestClient.Builder restClientBuilder, AuditLogRepository auditLogRepository) {
        this.userClient = restClientBuilder.baseUrl("http://AUTH-SERVICE").build();
        this.stationClient = restClientBuilder.baseUrl("http://STATION-SERVICE").build();
        this.auditLogRepository = auditLogRepository;
    }

    // newly added
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

    // newly added
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

    // newly added: convenience helper to approve by id
    @Transactional
    public void approveStationById(Long userId, Long stationId, String reason) {
        StationApprovalDto dto = new StationApprovalDto();
        dto.setStationId(stationId);
        dto.setApproved(true);
        dto.setReason(reason);
        approveOrRejectStation(userId, dto);
    }

    // newly added: convenience helper to reject by id
    @Transactional
    public void rejectStationById(Long userId, Long stationId, String reason) {
        StationApprovalDto dto = new StationApprovalDto();
        dto.setStationId(stationId);
        dto.setApproved(false);
        dto.setReason(reason);
        approveOrRejectStation(userId, dto);
    }

    // newly added
    public List<StationDto> getAllStations() {
        return stationClient.get()
                .uri("/stations")
                .retrieve()
                .body(new ParameterizedTypeReference<List<StationDto>>() {});
    }

    // newly added
    public List<StationDto> getUnapprovedStations() {
        return stationClient.get()
                .uri("/stations/unapproved")
                .retrieve()
                .body(new ParameterizedTypeReference<List<StationDto>>() {});
    }

    // newly added: delegate search to Station Service search endpoint
    public List<StationDto> searchStations(String query, String city, Double minPrice, Double maxPrice, Boolean available) {
        StringBuilder uri = new StringBuilder("/stations/search?");
        boolean started = false;
        if (query != null) { uri.append("query=").append(query); started = true; }
        if (city != null) { uri.append(started ? "&" : "").append("city=").append(city); started = true; }
        if (minPrice != null) { uri.append(started ? "&" : "").append("minPrice=").append(minPrice); started = true; }
        if (maxPrice != null) { uri.append(started ? "&" : "").append("maxPrice=").append(maxPrice); started = true; }
        if (available != null) { uri.append(started ? "&" : "").append("available=").append(available); }
        return stationClient.get()
                .uri(uri.toString())
                .retrieve()
                .body(new ParameterizedTypeReference<List<StationDto>>() {});
    }
}
