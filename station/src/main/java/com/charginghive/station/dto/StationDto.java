package com.charginghive.station.dto;

import lombok.Data;
import java.util.List;

@Data
public class StationDto {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private boolean isApproved;
    private Long ownerId;
    private List<StationPortDto> ports;
}
