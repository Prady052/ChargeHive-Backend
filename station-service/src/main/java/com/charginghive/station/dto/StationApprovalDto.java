package com.charginghive.station.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StationApprovalDto {
    private Long stationId;
    private boolean approved;
    private String reason;
}
