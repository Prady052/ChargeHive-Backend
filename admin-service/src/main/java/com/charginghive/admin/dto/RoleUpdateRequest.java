package com.charginghive.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

// newly added
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleUpdateRequest {
    private String name;               // newly added
    private Set<String> permissions;   // newly added - optional updated permissions
}
