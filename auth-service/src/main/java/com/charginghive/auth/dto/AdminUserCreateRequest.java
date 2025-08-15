package com.charginghive.auth.dto;

import com.charginghive.auth.entity.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// payload for admin to create users
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserCreateRequest {

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    @Pattern(
            regexp = "^(\\+\\d{1,3})?\\d{10,15}$",
            message = "Invalid phone number format"
    )
    private String phoneNumber;

    @NotNull(message = "User role must not be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 30)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 30)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

}
