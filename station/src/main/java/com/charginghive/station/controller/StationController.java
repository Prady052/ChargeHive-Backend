package com.charginghive.station.controller;

import com.charginghive.station.customException.OwnerIdMissMatchException;
import com.charginghive.station.dto.CreateStationRequestDto;
import com.charginghive.station.dto.StationApprovalDto;
import com.charginghive.station.dto.StationDto;
import com.charginghive.station.model.Station;
import com.charginghive.station.service.StationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/stations")
@RequiredArgsConstructor
@Slf4j
public class StationController {

    private final StationService stationService;

    // Endpoint for operator to create/register a new station
    @PostMapping
    public ResponseEntity<StationDto> createStation(@RequestHeader("X-User-Id") Long ownerId,@Valid @RequestBody CreateStationRequestDto requestDto) {
        StationDto createdStation = stationService.createStation(requestDto,ownerId);
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

    @GetMapping("/get-station-by-owner")
    public ResponseEntity<List<StationDto>> getStationsByOwner(@RequestHeader("X-User-Id") Long ownerId) {
        return ResponseEntity.ok(stationService.getStationsByOwner(ownerId));
    }


    //to approve/reject a station for admin only
    @PutMapping("/update-status")
    public ResponseEntity<Void> updateStationStatus(@RequestBody StationApprovalDto approvalDto) {
        stationService.updateStationStatus(approvalDto);
        return ResponseEntity.ok().build();
    }



    // n+1 query problem here
    @DeleteMapping("/{stationId}")
    public ResponseEntity<String> deleteStation(@RequestHeader("X-User-Id") Long ownerId,@PathVariable Long stationId) {

        try{
            stationService.deleteStation(stationId,ownerId);
            return ResponseEntity.status(HttpStatus.OK).body("Deleted station with id: " + stationId);
        }
        catch (EntityNotFoundException | OwnerIdMissMatchException ex){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }


    //need to fix the logic
    //simple delete implemented for now
    @DeleteMapping("/ports/{portId}")
    public ResponseEntity<Void> deletePort(@PathVariable Long portId) {
        stationService.deletePort(portId);
        return ResponseEntity.noContent().build();
    }
}