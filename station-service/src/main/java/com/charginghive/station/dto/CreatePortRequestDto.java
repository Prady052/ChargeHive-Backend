package com.charginghive.station.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreatePortRequestDto {

    @Positive(message = "Price per hour must be a positive number.")
    private Double pricePerHour;
    @NotEmpty
    private String connectorType;
    @Positive
    private double maxPowerKw;
}
