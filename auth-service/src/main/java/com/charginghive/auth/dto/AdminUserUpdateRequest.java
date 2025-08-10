package com.charginghive.auth.dto;

import com.charginghive.auth.entity.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Data; // newly added

// newly added: payload for admin to update users (all fields optional)
@Data
public class AdminUserUpdateRequest {

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
