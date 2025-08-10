package com.charginghive.booking.repository;

import com.charginghive.booking.dto.EarningResponseDto;
import com.charginghive.booking.entity.Booking;
import com.charginghive.booking.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    // Find all bookings for a specific user
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Booking> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Find all bookings for a specific station
    List<Booking> findByStationIdOrderByCreatedAtDesc(Long StationId);
    
    // Find all bookings for a specific port
    List<Booking> findByPortIdOrderByCreatedAtDesc(Long portId);
    
    // Find bookings by status
    List<Booking> findByStatusOrderByCreatedAtDesc(Status status);
    
    // Find active bookings (not cancelled or completed)
    @Query("SELECT b FROM Booking b WHERE b.status IN ('BOOKED') ORDER BY b.createdAt DESC")
    List<Booking> findActiveBookings();
    
    // Find bookings for a specific user and status
    List<Booking> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Status status);
    
    // Find bookings for a specific station and status
    List<Booking> findByStationIdAndStatusOrderByCreatedAtDesc(Long stationId, Status status);
    
    // Check if port is available for a given time range
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.portId = :portId " +
           "AND b.status IN ('BOOKED') " +
           "AND ((b.startTime <= :startTime AND b.endTime > :startTime) " +
           "OR (b.startTime < :endTime AND b.endTime >= :endTime) " +
           "OR (b.startTime >= :startTime AND b.endTime <= :endTime))")
    boolean isPortBooked(@Param("portId") Long portId, 
                        @Param("startTime") LocalDateTime startTime, 
                        @Param("endTime") LocalDateTime endTime);
    
    // Find overlapping bookings for a port
    @Query("SELECT b FROM Booking b WHERE b.portId = :portId " +
           "AND b.status IN ('BOOKED') " +
           "AND ((b.startTime <= :startTime AND b.endTime > :startTime) " +
           "OR (b.startTime < :endTime AND b.endTime >= :endTime) " +
           "OR (b.startTime >= :startTime AND b.endTime <= :endTime))")
    List<Booking> findOverlappingBookings(@Param("portId") Long portId, 
                                         @Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);
    
    // Find bookings within a date range
    @Query("SELECT b FROM Booking b WHERE b.startTime >= :startDate AND b.startTime <= :endDate " +
           "ORDER BY b.startTime")
    List<Booking> findBookingsInDateRange(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    // Find bookings for a user within a date range
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId " +
           "AND b.startTime >= :startDate AND b.startTime <= :endDate " +
           "ORDER BY b.startTime")
    List<Booking> findUserBookingsInDateRange(@Param("userId") Long userId,
                                             @Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
    
    // Find upcoming bookings for a user
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId " +
           "AND b.startTime >= :now AND b.status = 'BOOKED' " +
           "ORDER BY b.startTime")
    List<Booking> findUpcomingUserBookings(@Param("userId") Long userId, 
                                          @Param("now") LocalDateTime now);
    
    // Find completed bookings for a user
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId " +
           "AND b.status = 'COMPLETED' " +
           "ORDER BY b.endTime DESC")
    List<Booking> findCompletedUserBookings(@Param("userId") Long userId);
    
    // Count total bookings for a user
    long countByUserId(Long userId);
    
    // Count bookings by status for a user
    long countByUserIdAndStatus(Long userId, Status status);
    
    // Count total bookings for a station
    long countByStationId(Long stationId);
    
    // Count bookings by status for a station
    long countByStationIdAndStatus(Long stationId, Status status);

    List<Booking> findByStatusAndEndTimeBefore(Status status, LocalDateTime now);

    List<Booking> findByStatus(Status status);

    @Query("SELECT COALESCE(SUM(b.totalCost), 0) " +
            "FROM Booking b " +
            "WHERE b.stationId = :stationId AND b.status = :status")
    Double sumTotalCostByStationIdAndStatus(@Param("stationId") Long stationId,
                                            @Param("status") Status status);

}
