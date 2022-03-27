package com.user.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.user.api.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

	@Query("SELECT u FROM User u WHERE u.email = :email AND u.password = :password")
	public Optional<User> searchUserLogin(String email, String password);

	public Optional<User> findByEmail(String email);

}