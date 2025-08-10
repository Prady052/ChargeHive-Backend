package com.charginghive.admin.controller;

import com.charginghive.admin.dto.StationApprovalDto;
import com.charginghive.admin.dto.StationDto;
import com.charginghive.admin.service.StationManagementService;
import jakarta.validation.constraints.NotBlank; // newly added
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// newly added
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class StationAdminController { // newly added

    private final StationManagementService stationManagementService; // newly added

    // newly added
//    @PostMapping("/stations/process-approval")
//    public ResponseEntity<Void> processStationApproval(@RequestHeader("X-User-Id") Long adminId,
//                                                       @RequestBody StationApprovalDto approvalDto) {
//        log.info("Received request to process station approval from adminId: {}", adminId);
//        stationManagementService.approveOrRejectStation(adminId, approvalDto);
//        return ResponseEntity.ok().build();
//    }

    // newly added: convenience approve by id
    @PostMapping("/stations/{stationId}/approve")
    public ResponseEntity<Void> approveById(@RequestHeader("X-User-Id") Long adminId,
                                            @PathVariable Long stationId,
                                            @RequestParam(required = false) String reason) {
        stationManagementService.approveStationById(adminId, stationId, reason); // newly added
        return ResponseEntity.ok().build();
    }

    // newly added: convenience reject by id
    @PostMapping("/stations/{stationId}/reject")
    public ResponseEntity<Void> rejectById(@RequestHeader("X-User-Id") Long adminId,
                                           @PathVariable Long stationId,
                                           @RequestParam(required = false) @NotBlank(message = "Reason is required when rejecting") String reason) { // newly added
        stationManagementService.rejectStationById(adminId, stationId, reason); // newly added
        return ResponseEntity.ok().build();
    }

    // newly added
    @GetMapping("/stations")
    public ResponseEntity<List<StationDto>> getAllStations() {
        List<StationDto> stations = stationManagementService.getAllStations();
        return ResponseEntity.ok(stations);
    }

    // newly added
    @GetMapping("/stations/unapproved")
    public ResponseEntity<List<StationDto>> getUnapprovedStations() {
        List<StationDto> unapproved = stationManagementService.getUnapprovedStations();
        return ResponseEntity.ok(unapproved);
    }

    // newly added: optional search proxy to Station Service (leverages Station search endpoint)
    @GetMapping("/stations/search")
    public ResponseEntity<List<StationDto>> searchStations(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean available
    ) {
        List<StationDto> results = stationManagementService.searchStations(query, city, minPrice, maxPrice, available);
        return ResponseEntity.ok(results);
    }
}
