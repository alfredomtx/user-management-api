package com.user.api.email;

import com.user.api.email.model.Email;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailRepository extends JpaRepository<Email, Long> {

	Optional<Email> findById(Long id);
	Page<Email> findByAddressTo(Pageable pageable, String email);
}
