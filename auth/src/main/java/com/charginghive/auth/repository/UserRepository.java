package com.charginghive.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.charginghive.auth.entity.UserRegistration;


public interface UserRepository extends JpaRepository<UserRegistration, Long> {

	Optional<UserRegistration> findByEmail(String email);

}
