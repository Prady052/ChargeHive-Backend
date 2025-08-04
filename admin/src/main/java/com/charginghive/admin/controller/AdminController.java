package com.charginghive.admin.controller;

import com.charginghive.admin.dto.StationApprovalDto;
import com.charginghive.admin.dto.StationDto;
import com.charginghive.admin.dto.UserDto;
import com.charginghive.admin.dto.UserStatusUpdateDto;
import com.charginghive.admin.model.AuditLog;
import com.charginghive.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin")
public class AdminController {

    /*
     * This must be an instance field for Spring's Dependency Injection.
     *
     * It is not 'static' because that would break the Inversion of Control (IoC) pattern
     */
    private final AdminService adminService;

    // Station Endpoints
    @PostMapping("/stations/process-approval")
    public ResponseEntity<Void> processStationApproval(@RequestHeader("X-Admin-Id") Long adminId, @RequestBody StationApprovalDto approvalDto) {
        log.info("Received request to process station approval from adminId: {}", adminId);
        log.debug("Approval DTO details: {}", approvalDto);
        adminService.approveOrRejectStation(adminId, approvalDto);
        log.info("Successfully processed station approval for stationId: {}", approvalDto.getStationId());
        return ResponseEntity.ok().build();
    }

//    // Station Endpoints
//    @PostMapping("/stations/process-approval")
//    public ResponseEntity<Void> processStationApproval(@RequestBody StationApprovalDto approvalDto) {
//        adminService.approveOrRejectStation(getCurrentAdminUsername(), approvalDto);
//        return ResponseEntity.ok().build();
//    }

    @GetMapping("/stations")
    public ResponseEntity<List<StationDto>> getAllStations() {
        log.info("Received request to get all stations.");
        List<StationDto> stations = adminService.getAllStations();
        log.info("Found {} stations.", stations.size());
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/stations/unapproved")
    public ResponseEntity<List<StationDto>> getUnapprovedStations() {
        log.info("Received request to get all unapproved stations.");
        List<StationDto> unapprovedStations = adminService.getUnapprovedStations();
        log.info("Found {} unapproved stations.", unapprovedStations.size());
        return ResponseEntity.ok(unapprovedStations);
    }

    // User Endpoints
    @PostMapping("/users/update-status")
    public ResponseEntity<Void> updateUserStatus(@RequestHeader("X-Admin-Id") Long adminId, @RequestBody UserStatusUpdateDto statusDto) {
        log.info("Received request to update user status from adminId: {}", adminId);
        log.debug("User status update DTO: {}", statusDto);

        adminService.blockOrUnblockUser(adminId, statusDto);

        String status = statusDto.isEnabled() ? "ENABLED (Unblocked)" : "DISABLED (Blocked)";
        log.info("Successfully updated status for userId: {} to {}", statusDto.getUserId(), status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.info("Received request to get all users.");
        List<UserDto> users = adminService.getAllUsers();
        log.info("Found {} users.", users.size());
        return ResponseEntity.ok(users);
    }


    // Audit Log Endpoint
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        log.info("Received request to get audit logs.");
        List<AuditLog> auditLogs = adminService.getAuditLogs();
        log.info("Found {} audit log entries.", auditLogs.size());
        return ResponseEntity.ok(auditLogs);
    }

}
