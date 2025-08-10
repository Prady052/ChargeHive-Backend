package com.charginghive.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StationInfoDto {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private Double latitude;
    private Double longitude;
    private boolean isApproved;
    private Long ownerId;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortInfo {
        private Long id;
        private String connectorType;
        private Double maxPowerKw;
        private Double pricePerHour;
    }
}
