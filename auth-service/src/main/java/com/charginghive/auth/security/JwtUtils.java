package com.charginghive.auth.security;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.charginghive.auth.entity.UserRegistration;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j

public class JwtUtils {
	
	@Value("${jwt.secret.key}")
	private String jwtSecret;
	
	@Value("${jwt.expiration.time}")
	private int jwtExpirationMs;
	
	private SecretKey key; //this represents symmetric key
	
	@PostConstruct
	public void init() {
		log.info("Key {} Exp Time {}", jwtSecret, jwtExpirationMs);
		key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
	}
	
	
	public String generateJwtToken(Authentication authentication) {
		log.info("generate jwt token "+ authentication);
		
		UserRegistration userPrincipal = (UserRegistration) authentication.getPrincipal();
		
		return Jwts.builder()
				.subject(userPrincipal.getEmail())
				.issuedAt(new Date())
				.expiration(new Date(new Date().getTime() + jwtExpirationMs))
				.claim("authorities", getAuthoritiesInString(userPrincipal.getAuthorities()))
				.claim("user_id", userPrincipal.getId())
				.signWith(key, Jwts.SIG.HS256)
				.compact();
				
	}


	private List<String> getAuthoritiesInString(Collection<? extends GrantedAuthority> authorities) {
		return authorities.stream().map(auth -> auth.getAuthority()).collect(Collectors.toList());
	}
}
