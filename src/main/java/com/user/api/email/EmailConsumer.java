package com.user.api.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.api.email.model.Email;
import com.user.api.email.model.EmailDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

	@Autowired
	private EmailService emailService;

	private static final Logger logger = LoggerFactory.getLogger(EmailConsumer.class);

	@RabbitListener(queues = "${spring.rabbitmq.queue}")
	private void consumer(String message) throws JsonProcessingException, InterruptedException {
		Email email = new ObjectMapper().readValue(message, Email.class);

		logger.info("SENDING EMAIL FROM QUEUE");
		try {
			EmailDTO emailSent = emailService.sendEmail(email);
			logger.info(emailSent.toString());
		} catch (Exception e){
			logger.error("Error sending email: " + e.getMessage() + " | " + e.getCause().getMessage());
		}
	}
}
