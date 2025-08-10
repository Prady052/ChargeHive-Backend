package com.charginghive.admin.controller;


import com.charginghive.admin.dto.BookingResponseDto;
import com.charginghive.admin.service.BookingManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class BookingAdminController {

    private final BookingManagementService bookingManagementService;

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponseDto>> getAllBookings() {
        log.info("Received request to get all bookings.");
        List<BookingResponseDto> bookings = bookingManagementService.getAllBookings();
        log.info("Found {} bookings.", bookings.size());
        return ResponseEntity.ok(bookings);
    }

    /**
     * Get bookings by status for admin filtering
     */
    @GetMapping("/bookings/status/{status}")
    public ResponseEntity<List<BookingResponseDto>> getBookingsByStatus(@PathVariable String status) {
        log.info("Received request to get bookings by status: {}", status);
        List<BookingResponseDto> bookings = bookingManagementService.getBookingsByStatus(status);
        log.info("Found {} bookings with status: {}", bookings.size(), status);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Get active bookings for admin monitoring
     */
    @GetMapping("/bookings/active")
    public ResponseEntity<List<BookingResponseDto>> getActiveBookings() {
        log.info("Received request to get active bookings.");
        List<BookingResponseDto> bookings = bookingManagementService.getActiveBookings();
        log.info("Found {} active bookings.", bookings.size());
        return ResponseEntity.ok(bookings);
    }

    /**
     * Cancel a booking - admin override capability
     */
    @PutMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<BookingResponseDto> cancelBooking(@RequestHeader("X-User-Id") Long adminId,
                                                            @PathVariable Long bookingId,
                                                            @RequestParam(required = false) String reason) {
        log.info("Admin {} cancelling booking {}", adminId, bookingId);
        BookingResponseDto booking = bookingManagementService.cancelBooking(adminId, bookingId, reason);
        return ResponseEntity.ok(booking);
    }

    /**
     * Complete a booking - admin override capability
     */
    @PutMapping("/bookings/{bookingId}/complete")
    public ResponseEntity<BookingResponseDto> completeBooking(@RequestHeader("X-User-Id") Long adminId,
                                                              @PathVariable Long bookingId) {
        log.info("Admin {} completing booking {}", adminId, bookingId);
        BookingResponseDto booking = bookingManagementService.completeBooking(adminId, bookingId);
        return ResponseEntity.ok(booking);
    }

    /**
     * Get booking statistics for dashboard
     */
    @GetMapping("/bookings/statistics")
    public ResponseEntity<Map<String, Object>> getBookingStatistics() {
        log.info("Received request for booking statistics.");
        Map<String, Object> statistics = bookingManagementService.getBookingStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get bookings in date range for reporting
     */
    @GetMapping("/bookings/date-range")
    public ResponseEntity<List<BookingResponseDto>> getBookingsInDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("Getting bookings from {} to {}", startDate, endDate);
        List<BookingResponseDto> bookings = bookingManagementService.getBookingsInDateRange(startDate, endDate);
        log.info("Found {} bookings in date range.", bookings.size());
        return ResponseEntity.ok(bookings);
    }

}
