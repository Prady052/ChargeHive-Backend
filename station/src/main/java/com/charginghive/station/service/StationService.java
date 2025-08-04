package com.charginghive.station.service;

import com.charginghive.station.customException.UserNotFoundException;
import com.charginghive.station.dto.CreateStationRequestDto;
import com.charginghive.station.dto.StationApprovalDto;
import com.charginghive.station.dto.StationDto;
import com.charginghive.station.model.Station;
import com.charginghive.station.model.StationPort;
import com.charginghive.station.repository.StationPortRepository;
import com.charginghive.station.repository.StationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class StationService {

    private final StationRepository stationRepository;
    private final StationPortRepository stationPortRepository;
    private final ModelMapper modelMapper;
    private final RestClient userClient;

    public StationService(StationRepository stationRepository,
                          StationPortRepository stationPortRepository,
                          ModelMapper modelMapper,
                          RestClient userClient) {
        this.stationRepository = stationRepository;
        this.stationPortRepository = stationPortRepository;
        this.modelMapper = modelMapper;
        this.userClient = userClient;
    }

    @Transactional
    public StationDto createStation(CreateStationRequestDto requestDto) {
        //Verify the ownerId exists by calling the User Service
//        verifyUserExists(requestDto.getOwnerId());
         System.out.println(requestDto);
        //Use ModelMapper to map the basic properties
        Station station = modelMapper.map(requestDto, Station.class);
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
        return modelMapper.map(station, StationDto.class);
    }

    public List<StationDto> getAllStations() {
        return stationRepository.findAll().stream()
                .map(station -> modelMapper.map(station, StationDto.class)).toList();
    }

    public List<StationDto> getUnapprovedStations() {
        return stationRepository.findByIsApprovedFalse().stream()
                .map(station -> modelMapper.map(station, StationDto.class)).toList();
    }

    @Transactional
    public void updateStationStatus(StationApprovalDto approvalDto) {
        Station station = stationRepository.findById(approvalDto.getStationId())
                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + approvalDto.getStationId()));

        station.setApproved(approvalDto.isApproved());
        stationRepository.save(station);
    }

    @Transactional
    public void deleteStation(Long stationId) {
        if (!stationRepository.existsById(stationId)) {
            throw new EntityNotFoundException("Station not found with id: " + stationId);
        }
        stationRepository.deleteById(stationId);
    }

    @Transactional
    public void deletePort(Long portId) {
        if (!stationPortRepository.existsById(portId)) {
            throw new EntityNotFoundException("Station Port not found with id: " + portId);
        }
        stationPortRepository.deleteById(portId);
    }

//need to implement this method to verify user
//    private void verifyUserExists(Long ownerId)

}