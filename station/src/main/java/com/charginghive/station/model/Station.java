package com.charginghive.station.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "stations")
@Getter
@Setter
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String postalCode;

    //updated by the Admin Service.
    @Column(nullable = false)
    private boolean isApproved = false;

    // This links the station to the user who owns it.
    @Column(nullable = false)
    private Long ownerId;

    // A station can have multiple charging ports.
    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StationPort> ports = new HashSet<>();
}
