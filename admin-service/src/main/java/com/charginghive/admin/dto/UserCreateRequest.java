package com.charginghive.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {
    private String email;
    private String name;
    private String password;
    private Set<String> roles;
}
