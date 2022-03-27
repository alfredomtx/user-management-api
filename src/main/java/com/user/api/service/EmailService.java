package com.user.api.service;

import com.user.api.model.Email;
import com.user.api.model.dto.EmailDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface EmailService {

	ResponseEntity<Email> sendEmail(EmailDTO emailDto);
	ResponseEntity<Page<Email>> getAll(Pageable pageable);
	ResponseEntity<Object> getById(Long emailId);

}
