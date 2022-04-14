package com.user.api.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.api.email.model.Email;
import com.user.api.email.model.EmailDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

	@Autowired
	private EmailService emailService;

	@RabbitListener(queues = "${spring.rabbitmq.queue}")
	private void consumer(String message) throws JsonProcessingException, InterruptedException {
		Email email = new ObjectMapper().readValue(message, Email.class);

		System.out.println("SENDING EMAIL FROM QUEUE");
		EmailDTO emailSent = emailService.sendEmail(email);
		System.out.println(emailSent);
		System.out.println("--------------------");

		//throw new IllegalArgumentException("test invalid argument");
	}
}
