package com.charginghive.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDto {
    
    @NotNull(message = "Station ID is required")
    @Positive(message = "Station ID must be positive")
    private Long stationId;
    
    @NotNull(message = "Port ID is required")
    @Positive(message = "Port ID must be positive")
    private Long portId;
    
    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;
    
//    @NotNull(message = "End time is required")
//    @Future(message = "Start time must be in the future")
//    private LocalDateTime endTime;
    
    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be positive")
    private Integer duration; // in minutes
}
