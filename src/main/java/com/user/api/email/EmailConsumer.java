package com.user.api.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.api.email.model.Email;
import com.user.api.email.model.EmailDTO;
import com.user.api.exceptions.ObjectFieldsValidationException;

@Component
public class EmailConsumer {
	private static final Logger logger = LoggerFactory.getLogger(EmailConsumer.class);

	@Autowired
	private EmailService emailService;
	@Autowired
	private Validator validator;

	@RabbitListener(queues = "${spring.rabbitmq.queue}")
	private void consumer(String message) throws JsonProcessingException, ObjectFieldsValidationException {
		Email email = new ObjectMapper().readValue(message, Email.class);

		BindingResult result = new BeanPropertyBindingResult(email, "email");
		validator.validate(email, result);
		if (result.hasErrors()) {
			throw new ObjectFieldsValidationException(result.getFieldErrors());
		}

		logger.info("SENDING EMAIL FROM QUEUE");
		try {
			EmailDTO emailSent = emailService.sendEmail(email);
			logger.info("ID: " + emailSent.getId() + ", Subject: " + emailSent.getSubject() + ", From: " + emailSent.getAddressFrom() + ", To: " + emailSent.getAddressTo() );
		} catch (Exception e){
			logger.error("Error sending email: " + e);
		}
		//throw new IllegalArgumentException("Test exception");
	}
}
