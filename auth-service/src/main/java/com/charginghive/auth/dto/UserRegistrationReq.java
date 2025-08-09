package com.charginghive.auth.dto;

import com.charginghive.auth.entity.UserRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserRegistrationReq {

	private String firstName;
	private String lastName;
	private String email;
	private String phoneNumber;
	private String password;
	private UserRole userRole;
}
