package com.charginghive.station.repository;

import com.charginghive.station.model.StationPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StationPortRepository extends JpaRepository<StationPort, Long> {
}
