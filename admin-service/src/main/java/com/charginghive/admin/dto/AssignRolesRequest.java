package com.charginghive.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

// newly added
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignRolesRequest {
    private Set<String> roles; // newly added
}
