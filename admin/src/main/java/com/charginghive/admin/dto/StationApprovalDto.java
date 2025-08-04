package com.charginghive.admin.dto;

import lombok.Data;

@Data
public class StationApprovalDto {
    private Long stationId;
    private boolean approved;
    private String reason;
}
