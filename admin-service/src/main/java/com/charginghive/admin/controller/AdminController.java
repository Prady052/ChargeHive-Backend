package com.charginghive.admin.controller;

import com.charginghive.admin.dto.AdminMetricsDto;
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
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    // newly added: Audit Log Endpoint
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        log.info("Received request to get audit logs.");
        List<AuditLog> auditLogs = adminService.getAuditLogs();
        log.info("Found {} audit log entries.", auditLogs.size());
        return ResponseEntity.ok(auditLogs);
    }

    // newly added: simple dashboard metrics
    @GetMapping("/metrics")
    public ResponseEntity<AdminMetricsDto> getMetrics() {
        return ResponseEntity.ok(adminService.getMetrics());
    }
}
