package com.charginghive.auth.dto;

import com.charginghive.auth.entity.UserRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserRegistrationReq {

	private String name;
	private String email;
	private String password;
	// make it list of enum as admin can be a customer also
	private UserRole userRole;
}
