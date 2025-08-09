package com.charginghive.auth.entity;

import java.util.Collection;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Users")
public class UserRegistration implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "First name must not be blank")
	@Size(min = 2, max = 30, message = "First name must be between 2 and 30 characters")
	@Column(nullable = false)
	private String firstName;

	@NotBlank(message = "Last name must not be blank")
	@Size(min = 2, max = 30, message = "Last name must be between 2 and 30 characters")
	@Column(nullable = false)
	private String lastName;


	@NotBlank(message = "Phone number is required")
	@Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
	@Pattern(
			regexp = "^(\\+\\d{1,3})?\\d{10,15}$",
			message = "Invalid phone number format"
	)
	private String phoneNumber;


	@NotBlank(message = "Email must not be blank")
	@Email(message = "Email should be valid")
	@Column(nullable = false, unique = true)
	private String email;

	@NotBlank(message = "Password must not be blank")
	@Size(min = 8, message = "Password must be at least 8 characters long")
	@Column(nullable = false)
	private String password;

	@NotNull(message = "User role must not be null")
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserRole userRole;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return List.of(new SimpleGrantedAuthority(this.userRole.name()));
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return password;
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return email;
	}
}
