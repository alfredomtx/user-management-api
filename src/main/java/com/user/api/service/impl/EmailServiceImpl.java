package com.user.api.service.impl;

import com.user.api.enums.StatusEmail;
import com.user.api.model.Email;
import com.user.api.repository.EmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailServiceImpl {

	@Autowired
	private EmailRepository emailRepository;

	@Autowired
	private JavaMailSender emailSender;

	public Email sendEmail(Email emailModel) {
		emailModel.setSendDate(LocalDateTime.now());
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(emailModel.getAddressFrom());
			message.setTo(emailModel.getAddressTo());
			message.setSubject(emailModel.getSubject());
			message.setText(emailModel.getBody());
			emailSender.send(message);

			emailModel.setStatus(StatusEmail.SENT);
		} catch (MailException e) {
			e.printStackTrace();
			emailModel.setStatus(StatusEmail.ERROR);
		} finally {
			return emailRepository.save(emailModel);
		}
	}

	public Page<Email> findAll(Pageable pageable) {
		return emailRepository.findAll(pageable);
	}

	public Optional<Email> findById(UUID emailId) {
		return emailRepository.findById(emailId);
	}
}