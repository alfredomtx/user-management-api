package com.user.api.repository;

import com.user.api.model.Email;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailRepository extends JpaRepository<Email, Long> {

	Optional<Email> findById(Long id);
	List<Email> findByAddressTo(String email);
}
