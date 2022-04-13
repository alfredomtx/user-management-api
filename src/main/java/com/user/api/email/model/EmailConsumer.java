package com.user.api.email.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.api.email.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

	@Autowired
	private EmailService emailService;

	@RabbitListener(queues = "${spring.rabbitmq.queue}")
	private void consumer(String message) throws JsonProcessingException, InterruptedException{
		EmailDTO emailDTO = new ObjectMapper().readValue(message, EmailDTO.class);

		Email email = new Email();
		BeanUtils.copyProperties(emailDTO, email);
		System.out.println("SENDING EMAIL FROM QUEUE");

		EmailDTO emailSent = emailService.sendEmail(email);
		System.out.println("SENT");
		System.out.println(emailSent);
		System.out.println("--------------------");


	}
}
