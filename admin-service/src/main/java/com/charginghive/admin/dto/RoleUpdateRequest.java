package com.charginghive.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleUpdateRequest {
    private String name;
    private Set<String> permissions;
}
