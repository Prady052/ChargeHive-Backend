package com.charginghive.admin.controller;

import com.charginghive.admin.dto.StationDto;
import com.charginghive.admin.service.StationManagementService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@Validated
public class StationAdminController {

    private final StationManagementService stationManagementService;

    // newly added: convenience approve by id
    @PostMapping("/stations/{stationId}/approve")
    public ResponseEntity<Void> approveById(@RequestHeader("X-User-Id") Long adminId,
                                            @PathVariable Long stationId,
                                            @RequestParam(required = false) String reason) {
        stationManagementService.approveStationById(adminId, stationId, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stations/{stationId}/reject")
    public ResponseEntity<Void> rejectById(@RequestHeader("X-User-Id") Long adminId,
                                           @PathVariable Long stationId,
                                           @RequestParam(required = false) @NotBlank(message = "Reason is required when rejecting") String reason) { // newly added
        stationManagementService.rejectStationById(adminId, stationId, reason); // newly added
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stations")
    public ResponseEntity<List<StationDto>> getAllStations() {
        List<StationDto> stations = stationManagementService.getAllStations();
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/stations/unapproved")
    public ResponseEntity<List<StationDto>> getUnapprovedStations() {
        List<StationDto> unapproved = stationManagementService.getUnapprovedStations();
        return ResponseEntity.ok(unapproved);
    }

}
