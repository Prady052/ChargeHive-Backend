package com.charginghive.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

// newly added
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {
    private String email;  // newly added
    private String name;   // newly added
    private String password; // newly added
    private Set<String> roles; // newly added - optional initial roles
}
