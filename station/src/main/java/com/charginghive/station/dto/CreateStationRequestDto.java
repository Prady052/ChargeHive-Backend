package com.charginghive.station.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.util.List;

@Data
public class CreateStationRequestDto {
    @NotEmpty
    private String name;
    @NotEmpty
    private String address;
    @NotEmpty
    private String city;
    @NotEmpty
    private String state;
    @NotEmpty
    private String postalCode;
    @NotNull
    private Long ownerId;
    @Valid
    private List<CreatePortRequestDto> ports;
}
