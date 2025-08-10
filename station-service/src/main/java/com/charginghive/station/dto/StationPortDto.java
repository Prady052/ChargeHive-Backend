package com.charginghive.station.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StationPortDto {
    private Long id;
    private String connectorType;
    private double maxPowerKw;

    @NotNull
    private Double pricePerHour;
}
