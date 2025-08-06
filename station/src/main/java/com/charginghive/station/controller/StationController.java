package com.charginghive.station.controller;

import com.charginghive.station.dto.CreateStationRequestDto;
import com.charginghive.station.dto.StationApprovalDto;
import com.charginghive.station.dto.StationDto;
import com.charginghive.station.service.StationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    // Endpoint for operator to create/register a new station
    @PostMapping
    public ResponseEntity<StationDto> createStation(@Valid @RequestBody CreateStationRequestDto requestDto) {
        StationDto createdStation = stationService.createStation(requestDto);
        return new ResponseEntity<>(createdStation, HttpStatus.CREATED);
    }

    // --- Endpoints for Admin Service ---

    //to get all stations
    @GetMapping
    public ResponseEntity<List<StationDto>> getAllStations() {
        return ResponseEntity.ok(stationService.getAllStations());
    }

    //to get unapproved stations
    @GetMapping("/unapproved")
    public ResponseEntity<List<StationDto>> getUnapprovedStations() {
        return ResponseEntity.ok(stationService.getUnapprovedStations());
    }

    //to approve/reject a station
    @PutMapping("/update-status")
    public ResponseEntity<Void> updateStationStatus(@RequestBody StationApprovalDto approvalDto) {
        stationService.updateStationStatus(approvalDto);
        return ResponseEntity.ok().build();
    }



    // n+1 query problem here
    @DeleteMapping("/{stationId}")
    public ResponseEntity<Void> deleteStation(@PathVariable Long stationId) {
        stationService.deleteStation(stationId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/ports/{portId}")
    public ResponseEntity<Void> deletePort(@PathVariable Long portId) {
        stationService.deletePort(portId);
        return ResponseEntity.noContent().build();
    }
}