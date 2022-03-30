package com.user.api.service;

import com.user.api.model.Email;
import com.user.api.model.dto.EmailDTO;

import java.util.List;

public interface EmailService {

	List<EmailDTO> getAll();
	EmailDTO getById(Long id);
	List<EmailDTO> getByAddressTo(String emailAddress);
	EmailDTO sendEmail(Email email);


}
