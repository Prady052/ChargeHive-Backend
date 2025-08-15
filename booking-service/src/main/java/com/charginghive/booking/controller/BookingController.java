package com.charginghive.booking.controller;

import com.charginghive.booking.dto.BookingRequestDto;
import com.charginghive.booking.dto.BookingResponseDto;
import com.charginghive.booking.dto.BookingUpdateDto;
import com.charginghive.booking.dto.EarningResponseDto;
import com.charginghive.booking.entity.Status;
import com.charginghive.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {

    private final BookingService bookingService;

    /**
     * Create a new booking
     */
    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(@RequestHeader("X-User-Id") Long id,@RequestBody BookingRequestDto requestDto) {
        log.info("Creating new booking for user: {}", id);
        BookingResponseDto response = bookingService.createBooking(requestDto,id);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get booking by ID
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> getBookingById(@PathVariable Long bookingId) {
        log.info("Fetching booking with ID: {}", bookingId);
        BookingResponseDto response = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all bookings for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponseDto>> getUserBookings(@PathVariable Long userId) {
        log.info("Fetching all bookings for user: {}", userId);
        List<BookingResponseDto> response = bookingService.getUserBookings(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all bookings for a station
     * can be accessed by admin and owner only
     */
    @GetMapping("/station/{stationId}")
    public ResponseEntity<List<BookingResponseDto>> getStationBookings(@PathVariable Long stationId) {
        log.info("Fetching all bookings for station: {}", stationId);
        List<BookingResponseDto> response = bookingService.getStationBookings(stationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all bookings (admin only)
     */
    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getAllBookings() {
        log.info("Fetching all bookings");
        List<BookingResponseDto> response = bookingService.getAllBookings();
        return ResponseEntity.ok(response);
    }

    /**
     * Get bookings by status
     *
     */
    @GetMapping("/admin/status/{status}")
    public ResponseEntity<List<BookingResponseDto>> getBookingsByStatus(@PathVariable Status status) {
        log.info("Fetching bookings with status: {}", status);
        List<BookingResponseDto> response = bookingService.getBookingsByStatus(status);
        return ResponseEntity.ok(response);
    }

    /**
     * Get active bookings
     */
    @GetMapping("/admin/active")
    public ResponseEntity<List<BookingResponseDto>> getActiveBookings() {
        log.info("Fetching active bookings");
        List<BookingResponseDto> response = bookingService.getActiveBookings();
        return ResponseEntity.ok(response);
    }

    /**
     * Update booking
     */
    @PutMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> updateBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody BookingUpdateDto updateDto) {
        log.info("Updating booking with ID: {}", bookingId);
        BookingResponseDto response = bookingService.updateBooking(bookingId, updateDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel booking
     */
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponseDto> cancelBooking(@PathVariable Long bookingId) {
        log.info("Cancelling booking with ID: {}", bookingId);
        BookingResponseDto response = bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(response);
    }

    /**
     * Complete booking
     */
    @PutMapping("/admin/{bookingId}/complete")
    public ResponseEntity<BookingResponseDto> completeBooking(@PathVariable Long bookingId) {
        log.info("Completing booking with ID: {}", bookingId);
        BookingResponseDto response = bookingService.completeBooking(bookingId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get upcoming bookings for a user
     */
    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<List<BookingResponseDto>> getUpcomingUserBookings(@PathVariable Long userId) {
        log.info("Fetching upcoming bookings for user: {}", userId);
        List<BookingResponseDto> response = bookingService.getUpcomingUserBookings(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get completed bookings for a user
     */
    @GetMapping("/user/{userId}/completed")
    public ResponseEntity<List<BookingResponseDto>> getCompletedUserBookings(@PathVariable Long userId) {
        log.info("Fetching completed bookings for user: {}", userId);
        List<BookingResponseDto> response = bookingService.getCompletedUserBookings(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get bookings in date range
     */
    @GetMapping("/admin/date-range")
    public ResponseEntity<List<BookingResponseDto>> getBookingsInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Fetching bookings between {} and {}", startDate, endDate);
        List<BookingResponseDto> response = bookingService.getBookingsInDateRange(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user bookings in date range
     */
    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<List<BookingResponseDto>> getUserBookingsInDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Fetching bookings for user {} between {} and {}", userId, startDate, endDate);
        List<BookingResponseDto> response = bookingService.getUserBookingsInDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Check if port is available
     */
    @GetMapping("/port/{portId}/availability")
    public ResponseEntity<Boolean> isPortAvailable(
            @PathVariable Long portId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        log.info("Checking availability for port {} between {} and {}", portId, startTime, endTime);
        boolean isAvailable = bookingService.isPortAvailable(portId, startTime, endTime);
        return ResponseEntity.ok(isAvailable);
    }

    /*
    * Get all Booking with payment for a particular user
    */

    @GetMapping("/user/{userId}/with-payments")
    public ResponseEntity<List<BookingResponseDto>> getBookingsWithPayments(@PathVariable Long userId) {
        log.info("Fetching all bookings with payments for user: {}", userId);
        return ResponseEntity.ok(bookingService.getAllBookingsByUserId(userId));
    }

    /*
    * get total earning for a station(by stationId)
    * access only by admin
     */

    @GetMapping("/admin/earnings/{stationId}")
    public ResponseEntity<EarningResponseDto> getTotalEaringinsForAStation(@PathVariable Long stationId){
        log.info("fetching all bookings froma station and calculating totl earning!");
        return ResponseEntity.ok(bookingService.getTotalEarningForAStationById(stationId));
    }
}
