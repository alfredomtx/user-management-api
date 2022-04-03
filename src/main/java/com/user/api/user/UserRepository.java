package com.user.api.user;

import com.user.api.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	@Query("SELECT u FROM User u WHERE u.email = :email AND u.password = :password")
	Optional<User> searchUserLogin(String email, String password);
	Optional<User> findByEmail(String email);

}