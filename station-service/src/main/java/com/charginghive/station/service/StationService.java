package com.charginghive.station.service;

import com.charginghive.station.customException.NotFoundException; // added: custom exception
import com.charginghive.station.customException.OwnerIdMissMatchException;
import com.charginghive.station.customException.UserNotFoundException;
import com.charginghive.station.dto.*;
import com.charginghive.station.model.Station;
import com.charginghive.station.model.StationPort;
import com.charginghive.station.repository.StationPortRepository;
import com.charginghive.station.repository.StationRepository;
import com.charginghive.station.repository.StationSpecifications;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

// added: service encapsulating business logic for Station and Port operations
@Service
@Slf4j
public class StationService {

    private final StationRepository stationRepository;
    private final StationPortRepository stationPortRepository;
    private final ModelMapper modelMapper;
    private final RestClient userClient;
    private final RestClient bookingClient;

    public StationService(StationRepository repository, StationPortRepository repositoryPort, ModelMapper modelMapper, RestClient.Builder Client) {
        this.stationRepository = repository;
        this.stationPortRepository = repositoryPort;
        this.modelMapper = modelMapper;
        this.userClient = Client
                .baseUrl("http://AUTH-SERVICE")
                .build();
        this.bookingClient = Client.baseUrl("http://BOOKING-SERVICE")
                .build();
    }

    // added: create a station with optional ports
//    @Transactional
//    public StationDto createStation(@Valid CreateStationRequestDto request) {
//        Station station = mapper.map(request, Station.class);
//        // ensure ports handled manually due to skip config in ModelMapper
//        Set<StationPort> ports = new HashSet<>();
//        if (request.getPorts() != null) {
//            request.getPorts().forEach(p -> {
//                StationPort port = new StationPort();
//                port.setConnectorType(p.getConnectorType());
//                port.setMaxPowerKw(p.getMaxPowerKw());
//                port.setStation(station);
//                ports.add(port);
//            });
//        }
//        station.setPorts(ports);
//        Station saved = stationRepository.save(station);
//        return toDto(saved);
//    }

    @Transactional
    public StationDto createStation(CreateStationRequestDto requestDto,Long ownerId) {
        //Verify the ownerId exists by calling the User Service
        log.info("ownerId={}",ownerId);
        if(!verifyUserExists(ownerId)) {
            throw new UserNotFoundException("Owner not found with id "+ownerId);
        }
        log.info("Station info {}.", requestDto);
        //Use ModelMapper to map the basic properties
        Station station = modelMapper.map(requestDto, Station.class);
        station.setOwnerId(ownerId);
        station.setApproved(false); // New stations must always unapproved by default.

        //Manually handle the nested list of ports to set the bidirectional relationship
        if (requestDto.getPorts() != null && !requestDto.getPorts().isEmpty()) {
            requestDto.getPorts().forEach(portDto -> {
                StationPort port = modelMapper.map(portDto, StationPort.class);
                port.setStation(station); // Link the port to its parent station
                station.getPorts().add(port);
            });
        }

        Station savedStation = stationRepository.save(station);
        return modelMapper.map(savedStation, StationDto.class);
    }





    // added: fetch a station by id
    @Transactional
    public StationDto getStation(Long id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Station not found with id=" + id));
        return toDto(station);
    }

    // added: update partial fields of a station (ports managed via dedicated endpoints)
    @Transactional
    public StationDto updateStation(Long id, @Valid UpdateStationRequestDto update) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Station not found with id=" + id));
        modelMapper.map(update, station); // only non-null fields copied due to global config
        Station saved = stationRepository.save(station);
        return toDto(saved);
    }
    @Transactional
    public void updateStationStatus(StationApprovalDto approvalDto) {
        Station station = stationRepository.findById(approvalDto.getStationId())
                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + approvalDto.getStationId()));

        station.setApproved(approvalDto.isApproved());
        stationRepository.save(station);
    }

    // added: delete a station and its ports
    @Transactional
    public void deleteStation(Long id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Station not found with id=" + id));
        stationRepository.delete(station);
    }

    public List<StationDto> getAllStations() {
        return stationRepository.findAll().stream()
                .map(station -> modelMapper.map(station, StationDto.class)).toList();
    }


    // added: paginated filtering and search
    @Transactional(readOnly = true)
    public Page<StationDto> searchStations(
            Boolean approved,
            String city,
            String state,
            String name,
            Double minPrice,
            Double maxPrice,
            Double minLat,
            Double maxLat,
            Double minLon,
            Double maxLon,
            Pageable pageable
    ) {
        Specification<Station> spec = Specification
                .allOf(
                        StationSpecifications.hasApproval(approved),
                        StationSpecifications.hasCity(city),
                        StationSpecifications.hasState(state),
                        StationSpecifications.nameContains(name),
                        StationSpecifications.priceBetween(minPrice, maxPrice),
                        StationSpecifications.latitudeBetween(minLat, maxLat),
                        StationSpecifications.longitudeBetween(minLon, maxLon)
                );

        return stationRepository.findAll(spec, pageable).map(this::toDto);
    }

    // added: get stations by owner
    @Transactional(readOnly = true)
    public List<StationDto> getByOwner(Long ownerId) {
        return stationRepository.findByOwnerId(ownerId).stream().map(this::toDto).toList();
    }

    // added: approve a station
//    @Transactional
//    public StationDto approveStation(Long id) {
//        Station station = stationRepository.findById(id)
//                .orElseThrow(() -> new NotFoundException("Station not found with id=" + id));
//        station.setApproved(true);
//        return toDto(stationRepository.save(station));
//    }
//
//    // added: reject a station
//    @Transactional
//    public StationDto rejectStation(Long id) {
//        Station station = stationRepository.findById(id)
//                .orElseThrow(() -> new NotFoundException("Station not found with id=" + id));
//        station.setApproved(false);
//        return toDto(stationRepository.save(station));
//    }

    // added: add a port to a station
    @Transactional
    public StationPortDto addPort(Long stationId, @Valid CreatePortRequestDto request) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new NotFoundException("Station not found with id=" + stationId));
        StationPort port = new StationPort();
        port.setConnectorType(request.getConnectorType());
        port.setMaxPowerKw(request.getMaxPowerKw());
        port.setPricePerHour(request.getPricePerHour());
        port.setStation(station);
        StationPort saved = stationPortRepository.save(port);
        // maintain the relationship on the owner side
        station.getPorts().add(saved);
        return modelMapper.map(saved, StationPortDto.class);
    }

    // added: list ports of a station
    @Transactional(readOnly = true)
    public List<StationPortDto> listPorts(Long stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new NotFoundException("Station not found with id=" + stationId));
        return station.getPorts().stream().map(p -> modelMapper.map(p, StationPortDto.class)).toList();
    }

    public List<StationDto> getUnapprovedStations() {
        return stationRepository.findByIsApprovedFalse().stream()
                .map(station -> modelMapper.map(station, StationDto.class)).toList();
    }


    // added: remove a specific port from a station
    @Transactional
    public void removePort(Long stationId, Long portId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new NotFoundException("Station not found with id=" + stationId));

        Optional<StationPort> portOpt = station.getPorts().stream()
                .filter(p -> portId.equals(p.getId()))
                .findFirst();

        StationPort port = portOpt.orElseThrow(() ->
                new NotFoundException("Port not found with id=" + portId + " for station id=" + stationId));

        station.getPorts().remove(port);
        stationPortRepository.delete(port);
    }


    @Transactional
    public StationDto updatePort(Long ownerId, Long portId, CreatePortRequestDto dto) {
        StationPort port = stationPortRepository.findById(portId)
                .orElseThrow(() -> new EntityNotFoundException("Station Port not found with id: " + portId));
        Station station = port.getStation();
        if (!ownerId.equals(station.getOwnerId())) {
            throw new OwnerIdMissMatchException("Station does not belong to the owner");
        }
        port.setConnectorType(dto.getConnectorType());
        port.setMaxPowerKw(dto.getMaxPowerKw());
        stationPortRepository.save(port);
        return modelMapper.map(station, StationDto.class);
    }



    public List<StationDto> searchStations(String query, String city,Boolean available) {
        return stationRepository.findAll().stream()
                .filter(s -> query == null || s.getName().toLowerCase().contains(query.toLowerCase()))
                .filter(s -> city == null || s.getCity().equalsIgnoreCase(city))
                // available flag could check ports size > 0 for now
                .filter(s -> available == null || (available && !s.getPorts().isEmpty()) || (!available))
                .map(s -> modelMapper.map(s, StationDto.class))
                .toList();
    }

    public List<StationDto> findNearby(double lat, double lng, double radiusKm) {
        final double earthRadiusKm = 6371.0;
        return stationRepository.findAll().stream()
                .filter(Station::isApproved)
                .map(s -> new Object[]{s, distanceKm(lat, lng, s.getLatitude(), s.getLongitude(), earthRadiusKm)})
                .filter(arr -> (double) arr[1] <= radiusKm)
                .sorted(Comparator.comparingDouble(a -> (double) a[1]))
                .map(arr -> modelMapper.map((Station) arr[0], StationDto.class))
                .collect(Collectors.toList());
    }

    private double distanceKm(double lat1, double lon1, Double lat2, Double lon2, double R) {
        if (lat2 == null || lon2 == null) return Double.MAX_VALUE;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public List<String> getAvailability(Long stationId, String dateIso) {
        // Placeholder: return every 30 min between 08:00 and 20:00 as available
        LocalDate date = LocalDate.parse(dateIso, DateTimeFormatter.ISO_DATE);
        List<String> slots = new ArrayList<>();
        LocalTime t = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(20, 0);
        while (!t.isAfter(end.minusMinutes(30))) {
            slots.add(date.atTime(t).toString());
            t = t.plusMinutes(30);
        }
        return slots;
    }





    // added: helper mapping to StationDto including ports
    private StationDto toDto(Station station) {
        StationDto dto = modelMapper.map(station, StationDto.class);
        if (station.getPorts() != null) {
            dto.setPorts(station.getPorts().stream()
                    .map(p -> modelMapper.map(p, StationPortDto.class))
                    .toList());
        }
        return dto;
    }
    private boolean verifyUserExists(Long ownerId) {

        try {
            UserDto user = userClient.get()
                    .uri("auth/get-by-id/{id}", ownerId)
                    //if any 4xx or 5xx status is received then .retrieve()
                    //throw exception
                    .retrieve()
                    .body(UserDto.class);

            if (user == null) {
                throw new UserNotFoundException("Received an empty response for admin ID: " + ownerId);
            }
        } catch (WebClientResponseException e) {
            throw new UserNotFoundException("Admin user with ID " + ownerId + " not found.");
        }

        return true;
    }



    /**
     * Check if a station exists
     */
    public boolean stationExists(Long stationId) {
        return stationRepository.existsById(stationId);
    }

    /**
     * Get specific port information for booking service
     */
    public StationPortDto getPortInfo(Long stationId, Long portId) {
        // First check if station exists
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + stationId));

        // Find the port in this station
        StationPort port = station.getPorts().stream()
                .filter(p -> p.getId().equals(portId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Port not found with id: " + portId + " in station: " + stationId));

        return modelMapper.map(port, StationPortDto.class);
    }

    /**
     * Get all ports for a station
     */
    public List<StationPortDto> getStationPorts(Long stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + stationId));

        return station.getPorts().stream()
                .map(port -> modelMapper.map(port, StationPortDto.class))
                .collect(Collectors.toList());
    }

    /**
     * Get only approved stations
     */
    public List<StationDto> getApprovedStations() {
        return stationRepository.findByIsApprovedTrue().stream()
                .map(station -> modelMapper.map(station, StationDto.class))
                .collect(Collectors.toList());
    }

    public TotalEarningRespDto getTotalEarningsFromAStaion(Long stationId) {
        TotalEarningRespDto totalEarning = null;
        try{
            totalEarning = bookingClient.get().uri("/bookings/earnings/"+stationId)
                    .retrieve()
                    .body(TotalEarningRespDto.class);
        }
        catch (Exception e){
            log.warn("Invalid station id: " + stationId);
        }
        return totalEarning;
    }



}
//        //Manually handle the nested list of ports to set the bidirectional relationship
//        if (requestDto.getPorts() != null && !requestDto.getPorts().isEmpty()) {
//            requestDto.getPorts().forEach(portDto -> {
//                StationPort port = modelMapper.map(portDto, StationPort.class);
//                port.setStation(station); // Link the port to its parent station
//                station.getPorts().add(port);
//            });
//        }
//
//        Station savedStation = stationRepository.save(station);
//        return modelMapper.map(station, StationDto.class);
//    }
//
//    public List<StationDto> getAllStations() {
//        return stationRepository.findAll().stream()
//                .map(station -> modelMapper.map(station, StationDto.class)).toList();
//    }
//
//    public StationDto getStationById(Long id) {
//        Station station = stationRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + id));
//        return modelMapper.map(station, StationDto.class);
//    }
//
//    public List<StationDto> getUnapprovedStations() {
//        return stationRepository.findByIsApprovedFalse().stream()
//                .map(station -> modelMapper.map(station, StationDto.class)).toList();
//    }
//
//    public List<StationDto> getStationsByOwner(Long ownerId) {
//        return stationRepository.findByOwnerId(ownerId)
//                .stream().map(station -> modelMapper.map(station, StationDto.class)).toList();
//    }
//
//
//    @Transactional
//    public void updateStationStatus(StationApprovalDto approvalDto) {
//        Station station = stationRepository.findById(approvalDto.getStationId())
//                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + approvalDto.getStationId()));
//
//        station.setApproved(approvalDto.isApproved());
//        stationRepository.save(station);
//    }
//
//    @Transactional
//    public void deleteStation(Long stationId, Long ownerId)throws OwnerIdMissMatchException,EntityNotFoundException{
//            Station station = stationRepository.findById(stationId).orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + stationId));
//            if(!ownerId.equals(station.getOwnerId())){
//                throw new OwnerIdMissMatchException("Station does not belong to the owner");
//            }
//            stationRepository.deleteById(stationId);
//    }
//
//    @Transactional
//    public void deletePort(Long portId) {
//        if (!stationPortRepository.existsById(portId)) {
//            throw new EntityNotFoundException("Station Port not found with id: " + portId);
//        }
//        stationPortRepository.deleteById(portId);
//    }
//
//    @Transactional
//    public StationDto updateStation(Long ownerId, Long stationId, UpdateStationRequestDto dto) {
//        Station station = stationRepository.findById(stationId)
//                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + stationId));
//        if (!ownerId.equals(station.getOwnerId())) {
//            throw new OwnerIdMissMatchException("Station does not belong to the owner");
//        }
//        station.setName(dto.getName());
//        station.setAddress(dto.getAddress());
//        station.setCity(dto.getCity());
//        station.setState(dto.getState());
//        station.setPostalCode(dto.getPostalCode());
//        station.setLatitude(dto.getLatitude());
//        station.setLongitude(dto.getLongitude());
//        station.setPricePerHour(dto.getPricePerHour());
//        Station saved = stationRepository.save(station);
//        return modelMapper.map(saved, StationDto.class);
//    }
//
//    @Transactional
//    public StationDto addPort(Long ownerId, Long stationId, CreatePortRequestDto dto) {
//        Station station = stationRepository.findById(stationId)
//                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + stationId));
//        if (!ownerId.equals(station.getOwnerId())) {
//            throw new OwnerIdMissMatchException("Station does not belong to the owner");
//        }
//        StationPort port = modelMapper.map(dto, StationPort.class);
//        port.setStation(station);
//        station.getPorts().add(port);
//        Station saved = stationRepository.save(station);
//        return modelMapper.map(saved, StationDto.class);
//    }
//
//    @Transactional
//    public StationDto updatePort(Long ownerId, Long portId, CreatePortRequestDto dto) {
//        StationPort port = stationPortRepository.findById(portId)
//                .orElseThrow(() -> new EntityNotFoundException("Station Port not found with id: " + portId));
//        Station station = port.getStation();
//        if (!ownerId.equals(station.getOwnerId())) {
//            throw new OwnerIdMissMatchException("Station does not belong to the owner");
//        }
//        port.setConnectorType(dto.getConnectorType());
//        port.setMaxPowerKw(dto.getMaxPowerKw());
//        stationPortRepository.save(port);
//        return modelMapper.map(station, StationDto.class);
//    }
//
//    public List<StationDto> searchStations(String query, String city, Double minPrice, Double maxPrice, Boolean available) {
//        return stationRepository.findAll().stream()
//                .filter(s -> query == null || s.getName().toLowerCase().contains(query.toLowerCase()))
//                .filter(s -> city == null || s.getCity().equalsIgnoreCase(city))
//                .filter(s -> minPrice == null || (s.getPricePerHour() != null && s.getPricePerHour() >= minPrice))
//                .filter(s -> maxPrice == null || (s.getPricePerHour() != null && s.getPricePerHour() <= maxPrice))
//                // available flag could check ports size > 0 for now
//                .filter(s -> available == null || (available && !s.getPorts().isEmpty()) || (!available))
//                .map(s -> modelMapper.map(s, StationDto.class))
//                .toList();
//    }
//
//    public List<StationDto> findNearby(double lat, double lng, double radiusKm) {
//        final double earthRadiusKm = 6371.0;
//        return stationRepository.findAll().stream()
//                .filter(Station::isApproved)
//                .map(s -> new Object[]{s, distanceKm(lat, lng, s.getLatitude(), s.getLongitude(), earthRadiusKm)})
//                .filter(arr -> (double) arr[1] <= radiusKm)
//                .sorted(Comparator.comparingDouble(a -> (double) a[1]))
//                .map(arr -> modelMapper.map((Station) arr[0], StationDto.class))
//                .collect(Collectors.toList());
//    }
//
//    private double distanceKm(double lat1, double lon1, Double lat2, Double lon2, double R) {
//        if (lat2 == null || lon2 == null) return Double.MAX_VALUE;
//        double dLat = Math.toRadians(lat2 - lat1);
//        double dLon = Math.toRadians(lon2 - lon1);
//        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
//                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
//                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//        return R * c;
//    }
//
//    public List<String> getAvailability(Long stationId, String dateIso) {
//        // Placeholder: return every 30 min between 08:00 and 20:00 as available
//        LocalDate date = LocalDate.parse(dateIso, DateTimeFormatter.ISO_DATE);
//        List<String> slots = new ArrayList<>();
//        LocalTime t = LocalTime.of(8, 0);
//        LocalTime end = LocalTime.of(20, 0);
//        while (!t.isAfter(end.minusMinutes(30))) {
//            slots.add(date.atTime(t).toString());
//            t = t.plusMinutes(30);
//        }
//        return slots;
//    }
//
//      private boolean verifyUserExists(Long ownerId) {
//
//          try {
//              UserDto user = userClient.get()
//                      .uri("auth/get-by-id/{id}", ownerId)
//                      //if any 4xx or 5xx status is received then .retrieve()
//                      //throw exception
//                      .retrieve()
//                      .body(UserDto.class);
//
//              if (user == null) {
//                  throw new UserNotFoundException("Received an empty response for admin ID: " + ownerId);
//              }
//          } catch (WebClientResponseException e) {
//              throw new UserNotFoundException("Admin user with ID " + ownerId + " not found.");
//          }
//
//        return true;
//      }
//}