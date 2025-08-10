package com.charginghive.booking.dto;

import com.charginghive.booking.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingUpdateDto {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private Status status;
}
