package com.charginghive.station.dto;

import lombok.Data;

@Data
public class StationPortDto {
    private Long id;
    private String connectorType;
    private double maxPowerKw;
}
