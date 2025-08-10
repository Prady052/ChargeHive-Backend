package com.charginghive.auth.dto;

import jakarta.validation.constraints.NotEmpty; // newly added
import lombok.Data; // newly added

import java.util.List; // newly added

// newly added: payload for assigning roles; we will take the first role provided
@Data
public class AdminAssignRolesRequest {
    @NotEmpty(message = "At least one role is required")
    private List<String> roles;
}
