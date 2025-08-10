package com.charginghive.booking.service;

import com.charginghive.booking.dto.StationInfoDto;
import com.charginghive.booking.dto.UserInfoDto;
import com.charginghive.booking.exception.BookingException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class ExternalService {

    private final RestClient stationClient;
    private final RestClient userClient;
    public ExternalService(RestClient.Builder restClientBuilder) {
        this.userClient = restClientBuilder.baseUrl("http://AUTH-SERVICE").build();
        this.stationClient = restClientBuilder.baseUrl("http://STATION-SERVICE").build();
    }

    
    public StationInfoDto getStationInfo(Long stationId) {
        try {
            String url = "/stations/" + stationId;
            StationInfoDto stationInfo = stationClient.get().uri(url)
                                                       .retrieve()
                                                       .body(StationInfoDto.class);
            if (stationInfo == null) {
                throw new BookingException("Station not found with ID: " + stationId);
            }
            return stationInfo;
        } catch (Exception e) {
            log.error("Error fetching station info for stationId: {}", stationId, e);
            throw new BookingException("Failed to fetch station information: " + e.getMessage());
        }
    }
    
    public StationInfoDto.PortInfo getPortInfo(Long stationId, Long portId) {
        try {
            String url = "/stations/" + stationId + "/ports/" + portId;
            StationInfoDto.PortInfo portInfo = stationClient.get().uri(url)
                    .retrieve()
                    .body(StationInfoDto.PortInfo.class);
            if (portInfo == null) {
                throw new BookingException("Port not found with ID: " + portId + " in station: " + stationId);
            }
            return portInfo;
        } catch (Exception e) {
            log.error("Error fetching port info for stationId: {} and portId: {}", stationId, portId, e);
            throw new BookingException("Failed to fetch port information: " + e.getMessage());
        }
    }
    
    public UserInfoDto getUserInfo(Long userId) {
        try {
            String url = "/api/users/" + userId;
            UserInfoDto userInfo = userClient.get().uri(url)
                    .retrieve()
                    .body(UserInfoDto.class);
            if (userInfo == null) {
                throw new BookingException("User not found with ID: " + userId);
            }
            return userInfo;
        } catch (Exception e) {
            log.error("Error fetching user info for userId: {}", userId, e);
            throw new BookingException("Failed to fetch user information: " + e.getMessage());
        }
    }
    
    public boolean validateUserExists(Long userId) {
        try {
            String url = "/auth/get-by-id/" + userId + "/exists";
            Boolean exists = userClient.get().uri(url)
                    .retrieve()
                    .body(Boolean.class);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Error validating user existence for userId: {}", userId, e);
            return false;
        }
    }
    
    public boolean validateStationExists(Long stationId) {
        try {
            String url = "/stations/" + stationId + "/exists";
            Boolean exists = stationClient.get().uri(url)
                    .retrieve()
                    .body(Boolean.class);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Error validating station existence for stationId: {}", stationId, e);
            return false;
        }
    }
}
