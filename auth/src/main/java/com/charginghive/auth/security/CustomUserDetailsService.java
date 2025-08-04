package com.charginghive.auth.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.charginghive.auth.entity.UserRegistration;
import com.charginghive.auth.repository.UserRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository repository;
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserRegistration user = repository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Invalid Email!!!"));
		return user;
	}

}
