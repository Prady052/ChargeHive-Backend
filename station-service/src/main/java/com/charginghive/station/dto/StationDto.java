package com.charginghive.station.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
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
