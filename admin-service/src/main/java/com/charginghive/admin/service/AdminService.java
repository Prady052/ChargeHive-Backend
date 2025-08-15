package com.charginghive.admin.service;


import com.charginghive.admin.dto.*;
import com.charginghive.admin.model.AuditLog;
import com.charginghive.admin.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
@Slf4j
public class AdminService {

    private final RestClient userClient;
    private final RestClient stationClient;
    private final AuditLogRepository auditLogRepository;

    public AdminService(RestClient.Builder restClientBuilder,AuditLogRepository auditLogRepository) {
        this.userClient = restClientBuilder.baseUrl("http://AUTH-SERVICE").build();
        this.stationClient = restClientBuilder.baseUrl("http://STATION-SERVICE").build();
        this.auditLogRepository = auditLogRepository;
    }

    public AdminMetricsDto getMetrics() {
        long totalStations = 0;
        long pendingStations = 0;
        long approvedStations = 0;
        long totalUsers = 0;


        // Try-catch blocks are used here for resilience — if one API call fails,
        // we still return partial metrics instead of failing the entire request.
        try {
            List<StationDto> allStations = stationClient.get()
                    .uri("/stations")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
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
                    .body(new ParameterizedTypeReference<>() {});
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
                    .body(new ParameterizedTypeReference<>() {});
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


