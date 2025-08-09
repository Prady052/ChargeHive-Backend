package com.charginghive.auth.dto;

import com.charginghive.auth.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole userRole;
}
