package com.charginghive.station.controller;


import com.charginghive.station.dto.*;
import com.charginghive.station.service.StationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// added: REST API for Stations with versioned path
@RestController
@RequestMapping("/stations")
@RequiredArgsConstructor
@Validated
@Slf4j
public class StationController {

    private final StationService stationService;

    // added: create a station
//    @PostMapping
//    public ResponseEntity<StationDto> create(@Valid @RequestBody CreateStationRequestDto request) {
//        StationDto created = stationService.createStation(request);
//        return ResponseEntity.created(URI.create("/api/v1/stations/" + created.getId())).body(created);
//    }

    @PostMapping
    public ResponseEntity<StationDto> createStation(@RequestHeader("X-User-Id") Long ownerId,@Valid @RequestBody CreateStationRequestDto requestDto) {
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
    public ResponseEntity<StationDto> update(@PathVariable Long id, @Valid @RequestBody UpdateStationRequestDto request) {
        return ResponseEntity.ok(stationService.updateStation(id, request));
    }

    // added: delete by id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stationService.deleteStation(id);
        return ResponseEntity.noContent().build();
    }

    // added: search/filter with pagination and sorting
//    @GetMapping
//    public ResponseEntity<Page<StationDto>> search(
//            @RequestParam(required = false) Boolean approved,
//            @RequestParam(required = false) String city,
//            @RequestParam(required = false) String state,
//            @RequestParam(required = false) String name,
//            @RequestParam(required = false) Double minPrice,
//            @RequestParam(required = false) Double maxPrice,
//            @RequestParam(required = false) Double minLat,
//            @RequestParam(required = false) Double maxLat,
//            @RequestParam(required = false) Double minLon,
//            @RequestParam(required = false) Double maxLon,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @RequestParam(defaultValue = "name,asc") String sort // format: field,dir
//    ) {
//        String[] sortParts = sort.split(",");
//        Sort.Direction dir = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1]) ? Sort.Direction.DESC : Sort.Direction.ASC;
//        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortParts[0]));
//        Page<StationDto> result = stationService.searchStations(
//                approved, city, state, name, minPrice, maxPrice, minLat, maxLat, minLon, maxLon, pageable
//        );
//        return ResponseEntity.ok(result);
//    }

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

//    // added: approve station
//    @PostMapping("/{id}/approve")
//    @ResponseStatus(HttpStatus.OK)
//    public StationDto approve(@PathVariable Long id) {
//        return stationService.approveStation(id);
//    }
//
//    // added: reject station
//    @PostMapping("/{id}/reject")
//    @ResponseStatus(HttpStatus.OK)
//    public StationDto reject(@PathVariable Long id) {
//        return stationService.rejectStation(id);
//    }

    // added: add port to station
    @PostMapping("/{id}/ports")
    @ResponseStatus(HttpStatus.CREATED)
    public StationPortDto addPort(@PathVariable Long id, @Valid @RequestBody CreatePortRequestDto request) {
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
                                                 @Valid @RequestBody CreatePortRequestDto requestDto) {
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


    //newly added
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
     * Get all ports for a station - useful for booking service
     */
//    @GetMapping("/{stationId}/ports")
//    public ResponseEntity<List<StationPortDto>> getStationPorts(@PathVariable Long stationId) {
//        log.info("Getting all ports for station: {}", stationId);
//        List<StationPortDto> ports = stationService.getStationPorts(stationId);
//        return ResponseEntity.ok(ports);
//    }

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
    // --- Endpoints for Admin Service ---

//    //to get all stations
//    @GetMapping
//    public ResponseEntity<List<StationDto>> getAllStations() {
//        return ResponseEntity.ok(stationService.getAllStations());
//    }
//
//    // get one by id
//    @GetMapping("/{id}")
//    public ResponseEntity<StationDto> getStationById(@PathVariable Long id) {
//        return ResponseEntity.ok(stationService.getStationById(id));
//    }
//
//    //to get unapproved stations
//    @GetMapping("/unapproved")
//    public ResponseEntity<List<StationDto>> getUnapprovedStations() {
//        return ResponseEntity.ok(stationService.getUnapprovedStations());
//    }
//
//    @GetMapping("/get-station-by-owner")
//    public ResponseEntity<List<StationDto>> getStationsByOwner(@RequestHeader("X-User-Id") Long ownerId) {
//        return ResponseEntity.ok(stationService.getStationsByOwner(ownerId));
//    }
//
//
//    //to approve/reject a station for admin only
//    @PutMapping("/update-status")
//    public ResponseEntity<Void> updateStationStatus(@RequestBody StationApprovalDto approvalDto) {
//        stationService.updateStationStatus(approvalDto);
//        return ResponseEntity.ok().build();
//    }
//
//
//
//    // n+1 query problem here
//    @DeleteMapping("/{stationId}")
//    public ResponseEntity<String> deleteStation(@RequestHeader("X-User-Id") Long ownerId,@PathVariable Long stationId) {
//
//        try{
//            stationService.deleteStation(stationId,ownerId);
//            return ResponseEntity.status(HttpStatus.OK).body("Deleted station with id: " + stationId);
//        }
//        catch (EntityNotFoundException | OwnerIdMissMatchException ex){
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
//        }
//    }


//    //need to fix the logic
//    //simple delete implemented for now
//    @DeleteMapping("/ports/{portId}")
//    public ResponseEntity<Void> deletePort(@PathVariable Long portId) {
//        stationService.deletePort(portId);
//        return ResponseEntity.noContent().build();
//    }
//
//    // update station
//    @PutMapping("/{stationId}")
//    public ResponseEntity<StationDto> updateStation(@RequestHeader("X-User-Id") Long ownerId,
//                                                    @PathVariable Long stationId,
//                                                    @Valid @RequestBody UpdateStationRequestDto requestDto) {
//        return ResponseEntity.ok(stationService.updateStation(ownerId, stationId, requestDto));
//    }
//
//    // add port
//    @PostMapping("/{stationId}/ports")
//    public ResponseEntity<StationDto> addPort(@RequestHeader("X-User-Id") Long ownerId,
//                                              @PathVariable Long stationId,
//                                              @Valid @RequestBody CreatePortRequestDto requestDto) {
//        return new ResponseEntity<>(stationService.addPort(ownerId, stationId, requestDto), HttpStatus.CREATED);
//    }
//
//    // update port
//    @PutMapping("/ports/{portId}")
//    public ResponseEntity<StationDto> updatePort(@RequestHeader("X-User-Id") Long ownerId,
//                                                 @PathVariable Long portId,
//                                                 @Valid @RequestBody CreatePortRequestDto requestDto) {
//        return ResponseEntity.ok(stationService.updatePort(ownerId, portId, requestDto));
//    }
//
//    // search
//    @GetMapping("/search")
//    public ResponseEntity<List<StationDto>> searchStations(@RequestParam(required = false) String query,
//                                                           @RequestParam(required = false) String city,
//                                                           @RequestParam(required = false) Double minPrice,
//                                                           @RequestParam(required = false) Double maxPrice,
//                                                           @RequestParam(required = false) Boolean available) {
//        return ResponseEntity.ok(stationService.searchStations(query, city, minPrice, maxPrice, available));
//    }
//
//    // nearby
//    @GetMapping("/nearby")
//    public ResponseEntity<List<StationDto>> nearby(@RequestParam double lat,
//                                                   @RequestParam double lng,
//                                                   @RequestParam(defaultValue = "5") double radiusKm) {
//        return ResponseEntity.ok(stationService.findNearby(lat, lng, radiusKm));
//    }
//
//    // availability
//    @GetMapping("/{stationId}/availability")
//    public ResponseEntity<List<String>> availability(@PathVariable Long stationId,
//                                                     @RequestParam String date) {
//        return ResponseEntity.ok(stationService.getAvailability(stationId, date));
//    }
//}