package com.charginghive.admin.service;


import com.charginghive.admin.customException.UserNotFoundException;
import com.charginghive.admin.dto.AdminDto;
import com.charginghive.admin.dto.BookingResponseDto;
import com.charginghive.admin.model.AuditLog;
import com.charginghive.admin.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class BookingManagementService {
    private final RestClient bookingClient;
    private final RestClient userClient;    // newly added
    private final RestClient stationClient;
    private final AuditLogRepository auditLogRepository;


    public BookingManagementService(RestClient.Builder restClientBuilder, AuditLogRepository auditLogRepository) {
        this.bookingClient = restClientBuilder.baseUrl("http://BOOKING-SERVICE").build();
        this.userClient = restClientBuilder.baseUrl("http://AUTH-SERVICE").build();
        this.stationClient = restClientBuilder.baseUrl("http://STATION-SERVICE").build();
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Get all bookings from booking service
     */
    public List<BookingResponseDto> getAllBookings() {
        try {
            BookingResponseDto[] bookings = bookingClient.get()
                    .uri("/bookings")
                    .retrieve()
                    .body(BookingResponseDto[].class);
            return bookings != null ? Arrays.asList(bookings) : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching all bookings", e);
            return Collections.emptyList();
        }
    }

    /**
     * Get bookings by status
     */
    public List<BookingResponseDto> getBookingsByStatus(String status) {
        try {
            BookingResponseDto[] bookings = bookingClient.get()
                    .uri("/bookings/status/{status}", status)
                    .retrieve()
                    .body(BookingResponseDto[].class);
            return bookings != null ? Arrays.asList(bookings) : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching bookings by status: {}", status, e);
            return Collections.emptyList();
        }
    }

    /**
     * Get active bookings
     */
    public List<BookingResponseDto> getActiveBookings() {
        try {
            BookingResponseDto[] bookings = bookingClient.get()
                    .uri("/bookings/active")
                    .retrieve()
                    .body(BookingResponseDto[].class);
            return bookings != null ? Arrays.asList(bookings) : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching active bookings", e);
            return Collections.emptyList();
        }
    }

    /**
     * Cancel a booking with admin override
     */
    public BookingResponseDto cancelBooking(Long adminId, Long bookingId, String reason) {
        try {
            BookingResponseDto booking = bookingClient.put()
                    .uri("/bookings/{bookingId}/cancel", bookingId)
                    .retrieve()
                    .body(BookingResponseDto.class);

            // Log the admin action
            String adminUsername = getUsernameById(adminId);
            AuditLog auditLog = AuditLog.builder()
                    .adminUsername(adminUsername)
                    .action("CANCEL_BOOKING")
                    .targetEntity("Booking")
                    .targetId(bookingId)
                    .details("Booking cancelled by admin" + (reason != null ? " - Reason: " + reason : ""))
                    .build();
            auditLogRepository.save(auditLog);

            return booking;
        } catch (Exception e) {
            log.error("Error cancelling booking: {}", bookingId, e);
            throw new RuntimeException("Failed to cancel booking: " + e.getMessage());
        }
    }

    /**
     * Complete a booking with admin override
     */
    public BookingResponseDto completeBooking(Long adminId, Long bookingId) {
        try {
            BookingResponseDto booking = bookingClient.put()
                    .uri("/bookings/{bookingId}/complete", bookingId)
                    .retrieve()
                    .body(BookingResponseDto.class);

            // Log the admin action
            String adminUsername = getUsernameById(adminId);
            AuditLog auditLog = AuditLog.builder()
                    .adminUsername(adminUsername)
                    .action("COMPLETE_BOOKING")
                    .targetEntity("Booking")
                    .targetId(bookingId)
                    .details("Booking completed by admin")
                    .build();
            auditLogRepository.save(auditLog);

            return booking;
        } catch (Exception e) {
            log.error("Error completing booking: {}", bookingId, e);
            throw new RuntimeException("Failed to complete booking: " + e.getMessage());
        }
    }

    /**
     * Get booking statistics for admin dashboard
     */
    public Map<String, Object> getBookingStatistics() {
        try {
            // Get all bookings to calculate statistics
            List<BookingResponseDto> allBookings = getAllBookings();

            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("totalBookings", allBookings.size());
            stats.put("activeBookings", allBookings.stream()
                    .filter(b-> b.getStatus().toString().equals("BOOKED"))
                    .count());
            stats.put("completedBookings", allBookings.stream()
                    .filter(b -> "COMPLETED".equals(b.getStatus().toString()))
                    .count());
            stats.put("cancelledBookings", allBookings.stream()
                    .filter(b -> "CANCELLED".equals(b.getStatus().toString()))
                    .count());

            return stats;
        } catch (Exception e) {
            log.error("Error calculating booking statistics", e);
            return Collections.emptyMap();
        }
    }

    /**
     * Get bookings in date range
     */
    public List<BookingResponseDto> getBookingsInDateRange(String startDate, String endDate) {
        try {
            BookingResponseDto[] bookings = bookingClient.get()
                    .uri("/bookings/date-range?startDate={startDate}&endDate={endDate}", startDate, endDate)
                    .retrieve()
                    .body(BookingResponseDto[].class);
            return bookings != null ? Arrays.asList(bookings) : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching bookings in date range: {} to {}", startDate, endDate, e);
            return Collections.emptyList();
        }
    }

    private String getUsernameById(Long adminId) {
        try {
            AdminDto user = userClient.get()
                    .uri("auth/get-by-id/{id}", adminId)
                    .retrieve()
                    .body(AdminDto.class);

            if (user == null) {
                throw new UserNotFoundException("Received an empty response for admin ID: " + adminId);
            }
            return user.getName();
        } catch (RestClientResponseException e) {
            throw new UserNotFoundException("Admin user with ID " + adminId + " not found.");
        }
    }
}
