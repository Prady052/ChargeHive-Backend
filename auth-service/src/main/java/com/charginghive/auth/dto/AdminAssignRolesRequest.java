package com.charginghive.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// payload for assigning roles; we will take the first role provided
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAssignRolesRequest {
    @NotEmpty(message = "At least one role is required")
    private List<String> roles;
}
