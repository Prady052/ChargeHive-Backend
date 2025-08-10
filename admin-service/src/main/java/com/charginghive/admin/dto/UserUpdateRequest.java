package com.charginghive.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// newly added
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    private String email;   // newly added
    private String name;    // newly added
    private Boolean active; // newly added - optional status toggle
}
