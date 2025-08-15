package com.charginghive.station.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StationDto {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String state;
    private Double latitude;
    private Double longitude;
    private String postalCode;
    private boolean isApproved;
    private Long ownerId;
    private List<StationPortDto> ports;
}
