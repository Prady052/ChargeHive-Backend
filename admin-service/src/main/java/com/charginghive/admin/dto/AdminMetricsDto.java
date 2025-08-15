package com.charginghive.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminMetricsDto {
    private long totalStations;
    private long approvedStations;
    private long pendingStations;
    private long totalUsers;
}
