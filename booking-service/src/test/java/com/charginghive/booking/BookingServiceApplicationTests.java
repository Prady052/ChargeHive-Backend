package com.charginghive.booking;

import com.charginghive.booking.dto.BookingRequestDto;
import com.charginghive.booking.entity.Status;
import com.charginghive.booking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class BookingServiceApplicationTests {

//    @Autowired
//    private BookingService bookingService;
//
//    @Test
//    void contextLoads() {
//        // Test that the application context loads successfully
//        assertNotNull(bookingService);
//    }
//
//    @Test
//    void testBookingValidation() {
//        // Test booking validation logic
//        BookingRequestDto request = new BookingRequestDto();
//        request.setStationId(1L);
//        request.setPortId(1L);
//        request.setStartTime(LocalDateTime.now().plusHours(1));
//        request.setDuration(60);
//
//        // This should not throw an exception for valid data
//        assertDoesNotThrow(() -> {
//            // Note: This would fail in a real test due to external service dependencies
//            // In a real test, you would mock the ExternalService
//        });
//    }
//
//    @Test
//    void testStatusEnum() {
//        // Test that all status values exist
//        assertNotNull(Status.BOOKED);
//        assertNotNull(Status.CANCELLED);
//        assertNotNull(Status.COMPLETED);
//
//        assertEquals("BOOKED", Status.BOOKED.name());
//        assertEquals("CANCELLED", Status.CANCELLED.name());
//        assertEquals("COMPLETED", Status.COMPLETED.name());
//    }
}
