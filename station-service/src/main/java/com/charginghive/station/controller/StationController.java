package com.charginghive.station.controller;


import com.charginghive.station.dto.*;
import com.charginghive.station.service.StationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stations")
@RequiredArgsConstructor
@Validated
@Slf4j
public class StationController {

    private final StationService stationService;

    @PostMapping
    public ResponseEntity<StationDto> createStation(@RequestHeader("X-User-Id") Long ownerId,@RequestBody CreateStationRequestDto requestDto) {
        StationDto createdStation = stationService.createStation(requestDto,ownerId);
        return new ResponseEntity<>(createdStation, HttpStatus.CREATED);
    }

    //to get all stations
    @GetMapping
    public ResponseEntity<List<StationDto>> getAllStations() {
        return ResponseEntity.ok(stationService.getAllStations());
    }


    // added: get by id
    @GetMapping("/{id}")
    public ResponseEntity<StationDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(stationService.getStation(id));
    }

    // added: update by id (partial update using non-null fields)
    @PutMapping("/{id}")
    public ResponseEntity<StationDto> update(@PathVariable Long id,@RequestBody UpdateStationRequestDto request) {
        return ResponseEntity.ok(stationService.updateStation(id, request));
    }

    // added: delete by id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stationService.deleteStation(id);
        return ResponseEntity.noContent().build();
    }

    // added: list stations by owner
    @GetMapping("/get-station-by-owner")
    public ResponseEntity<List<StationDto>> getStationsByOwner(@RequestHeader("X-User-Id") Long ownerId) {
        log.info("Received request to get stations by owner: {}", ownerId);
        return ResponseEntity.ok(stationService.getByOwner(ownerId));
    }

    //to approve/reject a station for admin only
    @PutMapping("/update-status")
    public ResponseEntity<Void> updateStationStatus(@RequestBody StationApprovalDto approvalDto) {
        stationService.updateStationStatus(approvalDto);
        return ResponseEntity.ok().build();
    }

    // added: add port to station
    @PostMapping("/{id}/ports")
    @ResponseStatus(HttpStatus.CREATED)
    public StationPortDto addPort(@PathVariable Long id,@RequestBody CreatePortRequestDto request) {
        return stationService.addPort(id, request);
    }

    // added: list ports for station
    @GetMapping("/{id}/ports")
    public ResponseEntity<List<StationPortDto>> listPorts(@PathVariable Long id) {
        return ResponseEntity.ok(stationService.listPorts(id));
    }

    // update port
    @PutMapping("/ports/{portId}")
    public ResponseEntity<StationDto> updatePort(@RequestHeader("X-User-Id") Long ownerId,
                                                 @PathVariable Long portId,
                                                 @RequestBody CreatePortRequestDto requestDto) {
        return ResponseEntity.ok(stationService.updatePort(ownerId, portId, requestDto));
    }

    // added: remove a port
    @DeleteMapping("/{id}/ports/{portId}")
    public ResponseEntity<Void> removePort(@PathVariable Long id, @PathVariable Long portId) {
        stationService.removePort(id, portId);
        return ResponseEntity.noContent().build();
    }

    //to get unapproved stations
    @GetMapping("/unapproved")
    public ResponseEntity<List<StationDto>> getUnapprovedStations() {
        return ResponseEntity.ok(stationService.getUnapprovedStations());
    }

    // search
    @GetMapping("/search")
    public ResponseEntity<List<StationDto>> searchStations(@RequestParam(required = false) String query,
                                                           @RequestParam(required = false) String city,
                                                           @RequestParam(required = false) Boolean available) {
        return ResponseEntity.ok(stationService.searchStations(query, city,available));
    }

    // nearby
    @GetMapping("/nearby")
    public ResponseEntity<List<StationDto>> nearby(@RequestParam double lat,
                                                   @RequestParam double lng,
                                                   @RequestParam(defaultValue = "5") double radiusKm) {
        return ResponseEntity.ok(stationService.findNearby(lat, lng, radiusKm));
    }

    // availability
    @GetMapping("/{stationId}/availability")
    public ResponseEntity<List<String>> availability(@PathVariable Long stationId,
                                                     @RequestParam String date) {
        return ResponseEntity.ok(stationService.getAvailability(stationId, date));
    }

    // --- Endpoints for Booking Service Integration ---

    /**
     * Check if a station exists - required by booking service
     */
    @GetMapping("/{stationId}/exists")
    public ResponseEntity<Boolean> checkStationExists(@PathVariable Long stationId) {
        log.info("Checking if station exists with ID: {}", stationId);
        boolean exists = stationService.stationExists(stationId);
        return ResponseEntity.ok(exists);
    }

    /**
     * Get specific port information - required by booking service
     */
    @GetMapping("/{stationId}/ports/{portId}")
    public ResponseEntity<StationPortDto> getPortInfo(@PathVariable Long stationId, @PathVariable Long portId) {
        log.info("Getting port info for station: {} and port: {}", stationId, portId);
        StationPortDto portInfo = stationService.getPortInfo(stationId, portId);
        return ResponseEntity.ok(portInfo);
    }

    /**
     * Get approved stations only - for booking service to show available stations
     */
    @GetMapping("/approved")
    public ResponseEntity<List<StationDto>> getApprovedStations() {
        log.info("Getting all approved stations");
        List<StationDto> approvedStations = stationService.getApprovedStations();
        return ResponseEntity.ok(approvedStations);
    }

    /**
     * Get Total earnings for a station and total completed
     */

    @GetMapping("/totalearnings/{stationId}")
    public ResponseEntity<TotalEarningRespDto> getTotlaEaringns(@PathVariable Long stationId){
        return ResponseEntity.ok(stationService.getTotalEarningsFromAStaion(stationId));
    }

}