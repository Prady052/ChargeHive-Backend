package com.charginghive.admin.dto;

import lombok.AllArgsConstructor; // newly added
import lombok.Builder; // newly added
import lombok.Data; // newly added
import lombok.NoArgsConstructor; // newly added

// newly added: metrics dto to present counts in admin dashboard
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
