package com.charginghive.booking.service;

import com.charginghive.booking.dto.*;
import com.charginghive.booking.entity.Booking;
import com.charginghive.booking.entity.Status;
import com.charginghive.booking.exception.BookingException;
import com.charginghive.booking.exception.ResourceNotFoundException;
import com.charginghive.booking.repository.BookingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.Console;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ExternalService externalService;
    private final ModelMapper modelMapper;

    // Constants for pricing
//    private static final double BASE_RATE_PER_HOUR = 2.50; // $2.50 per hour
//    private static final double POWER_MULTIPLIER = 0.10; // $0.10 per kW

    /**
     * Create a new booking
     */
    public BookingResponseDto createBooking(BookingRequestDto requestDto,Long userId) {
        log.info("Creating booking for user: {}, station: {}, port: {}",
                userId, requestDto.getStationId(), requestDto.getPortId());

        // Calculate end time
        LocalDateTime endTime = requestDto.getStartTime().plusMinutes(requestDto.getDuration());
        // Validate request
        validateBookingRequest(requestDto,userId,endTime);

        // Check if port is available
        if (isPortBooked(requestDto.getPortId(), requestDto.getStartTime(), endTime)) {
            throw new BookingException("Port is not available for the specified time range");
        }

        // Calculate cost
//        double totalCost = calculateBookingCost(requestDto);
        double totalCost = calculateBookingCost(requestDto.getStationId(),requestDto.getPortId(),requestDto.getDuration());

        // Create booking entity
        Booking booking = Booking.builder().userId(userId).stationId(requestDto.getStationId()).
                portId(requestDto.getPortId()).
                startTime(requestDto.getStartTime()).
                endTime(endTime).
                duration(requestDto.getDuration()).
                totalCost(totalCost).
                status(Status.BOOKED).build();

        // Save booking
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully with ID: {}", savedBooking.getId());

        return convertToResponseDto(savedBooking);
    }

    /**
     * Get booking by ID
     */
    public BookingResponseDto getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));
        return convertToResponseDto(booking);
    }

    /**
     * Get all bookings for a user
     */
    public List<BookingResponseDto> getUserBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return bookings.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    /**
     * Get all bookings for a station
     */
    public List<BookingResponseDto> getStationBookings(Long stationId) {
        List<Booking> bookings = bookingRepository.findByStationIdOrderByCreatedAtDesc(stationId);
        return bookings.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    /**
     * Get all bookings
     */
    public List<BookingResponseDto> getAllBookings() {
//        completeExpiredBookingsOnFetch();
        autoCompleteExpiredBookings();
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    /**
     * Get bookings by status
     */
    public List<BookingResponseDto> getBookingsByStatus(Status status) {
        List<Booking> bookings = bookingRepository.findByStatusOrderByCreatedAtDesc(status);
        return bookings.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    /**
     * Get active bookings
     */
    public List<BookingResponseDto> getActiveBookings() {
        List<Booking> bookings = bookingRepository.findActiveBookings();
        return bookings.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    /**
     * Update booking
     */
    public BookingResponseDto updateBooking(Long bookingId, BookingUpdateDto updateDto) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Check if booking can be updated
        if (booking.getStatus() == Status.CANCELLED) {
            throw new BookingException("Cannot update a cancelled booking");
        }

        if (booking.getStatus() == Status.COMPLETED) {
            throw new BookingException("Cannot update a completed booking");
        }

        // Update fields if provided
        if (updateDto.getStartTime() != null) {
            booking.setStartTime(updateDto.getStartTime());
        }

        if (updateDto.getEndTime() != null) {
            booking.setEndTime(updateDto.getEndTime());
        }

        if (updateDto.getDuration() != null) {
            booking.setDuration(updateDto.getDuration());
        }

        if (updateDto.getStatus() != null) {
            booking.setStatus(updateDto.getStatus());
        }

        // Recalculate cost if time changed
        if (updateDto.getStartTime() != null || updateDto.getEndTime() != null || updateDto.getDuration() != null) {
            double newCost = calculateBookingCost(booking.getStationId(),booking.getPortId(),booking.getDuration());
            booking.setTotalCost(newCost);
        }

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking updated successfully with ID: {}", updatedBooking.getId());

        return convertToResponseDto(updatedBooking);
    }

    /**
     * Cancel booking
     */
    public BookingResponseDto cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getStatus() == Status.CANCELLED) {
            throw new BookingException("Booking is already cancelled");
        }

        if (booking.getStatus() == Status.COMPLETED) {
            throw new BookingException("Cannot cancel a completed booking");
        }

        // Check if booking can be cancelled (e.g., not started yet)
        if (booking.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BookingException("Cannot cancel a booking that has already started");
        }

        booking.setStatus(Status.CANCELLED);
        Booking cancelledBooking = bookingRepository.save(booking);
        log.info("Booking cancelled successfully with ID: {}", cancelledBooking.getId());

        return convertToResponseDto(cancelledBooking);
    }

    /**
     * Complete booking
     */
    public BookingResponseDto completeBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getStatus() == Status.COMPLETED) {
            throw new BookingException("Booking is already completed");
        }

        if (booking.getStatus() == Status.CANCELLED) {
            throw new BookingException("Cannot complete a cancelled booking");
        }

        booking.setStatus(Status.COMPLETED);
        Booking completedBooking = bookingRepository.save(booking);
        log.info("Booking completed successfully with ID: {}", completedBooking.getId());

        return convertToResponseDto(completedBooking);
    }

    /**
     * Get upcoming bookings for a user
     */
    public List<BookingResponseDto> getUpcomingUserBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findUpcomingUserBookings(userId, LocalDateTime.now());
        return bookings.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    /**
     * Get completed bookings for a user
     */
    public List<BookingResponseDto> getCompletedUserBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findCompletedUserBookings(userId);
        return bookings.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    /**
     * Get bookings in date range
     */
    public List<BookingResponseDto> getBookingsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Booking> bookings = bookingRepository.findBookingsInDateRange(startDate, endDate);
        return bookings.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    /**
     * Get user bookings in date range
     */
    public List<BookingResponseDto> getUserBookingsInDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Booking> bookings = bookingRepository.findUserBookingsInDateRange(userId, startDate, endDate);
        return bookings.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    /**
     * Check if port is available for booking
     */
    public boolean isPortAvailable(Long portId, LocalDateTime startTime, LocalDateTime endTime) {
        return !isPortBooked(portId, startTime, endTime);
    }


    // Private helper methods

    private void validateBookingRequest(BookingRequestDto requestDto,Long userId,LocalDateTime endTime) {
        // Validate time constraints
        if (requestDto.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BookingException("Start time cannot be in the past");
        }

        if (endTime.isBefore(requestDto.getStartTime())) {
            throw new BookingException("End time must be after start time");
        }

        if (requestDto.getStartTime().equals(endTime)) {
            throw new BookingException("Start time and end time cannot be the same");
        }

        // Validate duration
        long actualDuration = ChronoUnit.MINUTES.between(requestDto.getStartTime(), endTime);
        if (actualDuration != requestDto.getDuration()) {
            throw new BookingException("Duration does not match the time range");
        }

        // Validate minimum booking duration (30 minutes)
        if (requestDto.getDuration() < 30) {
            throw new BookingException("Minimum booking duration is 30 minutes");
        }

        // Validate maximum booking duration (24 hours)
        if (requestDto.getDuration() > 1440) {
            throw new BookingException("Maximum booking duration is 24 hours");
        }

        // Validate user exists
        if (!externalService.validateUserExists(userId)) {
            throw new BookingException("User does not exist");
        }

        // Validate station exists
        if (!externalService.validateStationExists(requestDto.getStationId())) {
            throw new BookingException("Station does not exist");
        }
    }

    private boolean isPortBooked(Long portId, LocalDateTime startTime, LocalDateTime endTime) {
        return bookingRepository.isPortBooked(portId, startTime, endTime);
    }

//    private double calculateBookingCost(BookingRequestDto requestDto) {
//        // Get port information to calculate cost based on power
//        StationInfoDto.PortInfo portInfo = externalService.getPortInfo(requestDto.getStationId(), requestDto.getPortId());
//
//        // Calculate hours
//        double hours = requestDto.getDuration() / 60.0;
//
//        // Base cost + power-based cost
//        double baseCost = BASE_RATE_PER_HOUR * hours;
//        double powerCost = portInfo.getMaxPowerKw() * POWER_MULTIPLIER * hours;
//
//        return baseCost + powerCost;
//    }
       private double calculateBookingCost(Long stationId, Long portId,Integer duration) {

           StationInfoDto.PortInfo portInfo = externalService.getPortInfo(stationId,portId);


           if (portInfo == null || portInfo.getPricePerHour() == null) {
               log.error("Could not retrieve price information for port {} at station {}",
                       portId, stationId);
               throw new IllegalStateException("Unable to calculate cost: price information is missing.");
           }

           double hours = duration / 60.0;

           double finalCost = portInfo.getPricePerHour() * hours;

           return finalCost;
       }

//    private double calculateBookingCost(Booking booking) {
//        // Get port information
//        StationInfoDto.PortInfo portInfo = externalService.getPortInfo(booking.getStationId(), booking.getPortId());
//
//        // Calculate hours
//        double hours = booking.getDuration() / 60.0;
//
//        // Base cost + power-based cost
//        double baseCost = BASE_RATE_PER_HOUR * hours;
//        double powerCost = portInfo.getMaxPowerKw() * POWER_MULTIPLIER * hours;
//
//        return baseCost + powerCost;
//    }

    private BookingResponseDto convertToResponseDto(Booking booking) {
        BookingResponseDto responseDto = modelMapper.map(booking, BookingResponseDto.class);

        try {
            // Add station information
            StationInfoDto stationInfo = externalService.getStationInfo(booking.getStationId());
            responseDto.setStationName(stationInfo.getName());
            responseDto.setStationAddress(stationInfo.getAddress());

            // Add port information
            StationInfoDto.PortInfo portInfo = externalService.getPortInfo(booking.getStationId(), booking.getPortId());
            responseDto.setConnectorType(portInfo.getConnectorType());
            responseDto.setMaxPowerKw(portInfo.getMaxPowerKw());
        } catch (Exception e) {
            log.warn("Could not fetch external information for booking: {}", booking.getId(), e);
        }

        return responseDto;
    }

    public List<BookingResponseDto> getAllBookingsByUserId(Long userId) {
        List<BookingResponseDto> responseDtos = new ArrayList<>();
        try{
            responseDtos = bookingRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId)
                    .stream()
                    .map(b -> modelMapper.map(b, BookingResponseDto.class))
                    .toList();
        }
        catch (Exception e){
            log.warn("could not fetch bookings due to invalid userId: {}", userId, e);
        }
        return responseDtos;
    }



    // Statistics class
    public static class BookingStatistics {
        private final long totalBookings;
        private final long activeBookings;
        private final long completedBookings;
        private final long cancelledBookings;

        public BookingStatistics(long totalBookings, long activeBookings, long completedBookings, long cancelledBookings) {
            this.totalBookings = totalBookings;
            this.activeBookings = activeBookings;
            this.completedBookings = completedBookings;
            this.cancelledBookings = cancelledBookings;
        }

        // Getters
        public long getTotalBookings() { return totalBookings; }
        public long getActiveBookings() { return activeBookings; }
        public long getCompletedBookings() { return completedBookings; }
        public long getCancelledBookings() { return cancelledBookings; }
    }

    /**
     * Auto-complete expired bookings (runs every 1 minute)
     */
    @Scheduled(fixedRate = 60000)
    public void autoCompleteExpiredBookings() {
        System.out.println("Scheduler triggered: " + LocalDateTime.now());
        List<Booking> expiredBookings = bookingRepository
                .findByStatusAndEndTimeBefore(Status.BOOKED, LocalDateTime.now());
        System.out.println("Found " + expiredBookings.size() + " expired bookings");

        //TO check timezone
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Current time in app: " + now);

        // For debugging: fetch all BOOKED bookings and print their endTime
        List<Booking> bookedBookings = bookingRepository.findByStatus(Status.BOOKED);
        bookedBookings.forEach(b -> System.out.println("Booking id=" + b.getId() + ", endTime=" + b.getEndTime()));


        if (!expiredBookings.isEmpty()) {
            System.out.println("changing bookied status to Completed status!");
            expiredBookings.forEach(b -> b.setStatus(Status.COMPLETED));
            bookingRepository.saveAll(expiredBookings);
            log.info("Auto-completed {} expired bookings", expiredBookings.size());
        }
    }

    /**
     * Call this before returning booking lists to ensure statuses are fresh
     */
    private void completeExpiredBookingsOnFetch() {
        autoCompleteExpiredBookings();
    }


    public EarningResponseDto getTotalEarningForAStationById(Long stationId) {

        EarningResponseDto earningResponseDto = null;
        try{
//            int count = Math.toIntExact(bookingRepository.countByStationId(stationId));
            Double sum = bookingRepository.sumTotalCostByStationIdAndStatus(stationId, Status.BOOKED);
//            earningResponseDto = bookingRepository.countBookingsAndSumTotalCostByStationIdAndStatus(stationId, Status.BOOKED);
            log.info("got the total earning of booked ports: "+sum);
            earningResponseDto = EarningResponseDto.builder().totalEarning(sum).build();
        }
        catch (Exception e){
            log.warn("could not fetch bookings due to invalid stationId: {}", stationId, e);
        }
        return earningResponseDto;
    }
}