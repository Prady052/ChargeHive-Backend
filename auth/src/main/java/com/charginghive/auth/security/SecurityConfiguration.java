package com.charginghive.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

import com.charginghive.auth.repository.UserRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	@Bean 
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		 
		http.csrf(csrf -> csrf.disable());
		
		http.authorizeHttpRequests(request ->
						request.requestMatchers("/swagger-ui/**", "/v**/api-docs/**"
								, "/api/auth/register", "/api/auth/login").permitAll()
								.requestMatchers("/error").permitAll()
				);
		
		http.sessionManagement(sessoion -> 
            sessoion.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		
		return http.build();
	}
	
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public UserDetailsService userDetailsService(UserRepository repository) {
        return new CustomUserDetailsService(repository);
    }
}
