package com.charginghive.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailDto {

    private UserDto user;
    private List<BookingResponseDto> bookings;
    //private List<PaymentResponseDto> payments;
}
