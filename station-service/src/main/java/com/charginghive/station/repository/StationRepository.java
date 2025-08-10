package com.charginghive.station.repository;

import com.charginghive.station.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // added for dynamic filtering
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationRepository extends JpaRepository<Station, Long>, JpaSpecificationExecutor<Station> { // added JpaSpecificationExecutor
    List<Station> findByIsApprovedFalse();
    List<Station> findByIsApprovedTrue();
    List<Station> findByOwnerId(Long ownerId);
}
