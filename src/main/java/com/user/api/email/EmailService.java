package com.user.api.email;

import com.user.api.email.model.Email;
import com.user.api.email.model.EmailDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmailService {

	Page<EmailDTO> getAll(Pageable pageable);
	EmailDTO getById(Long id);
	List<EmailDTO> getByAddressTo(String emailAddress);
	EmailDTO sendEmail(Email email);


}
