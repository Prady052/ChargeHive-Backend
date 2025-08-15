package com.charginghive.station.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StationPortDto {
    private Long id;
    private String connectorType;
    private double maxPowerKw;

    @NotNull
    private Double pricePerHour;
}
