package com.charginghive.station.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "station_ports")
@Getter
@Setter
public class StationPort {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Connector type is required")
    @Column(nullable = false)
    private String connectorType;

    @Column
    private Double pricePerHour;

    @NotNull(message = "Max power is required")
    @DecimalMin(value = "0.1", message = "Max power must be greater than 0")
    @Column(nullable = false)
    private double maxPowerKw;

    // Many ports belong to one station.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;
}
