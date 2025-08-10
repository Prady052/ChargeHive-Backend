package com.charginghive.booking.dto;

import com.charginghive.booking.entity.Status;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDto {
    private Long id;
    private Long userId;
    private Long stationId;
    private Long portId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private Double totalCost;
    private Status status;
    private LocalDateTime createdAt;
    private String stationName;
    private String stationAddress;
    private String connectorType;
    private Double maxPowerKw;

    public Integer getDuration() {
        if (startTime != null && endTime != null) {
            return (int) java.time.Duration.between(startTime, endTime).toMinutes();
        }
        return 0;
    }
}
