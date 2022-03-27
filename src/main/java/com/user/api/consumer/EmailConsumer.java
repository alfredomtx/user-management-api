package com.user.api.consumer;

import com.user.api.model.Email;
import com.user.api.model.dto.EmailDTO;
import com.user.api.service.impl.EmailServiceImplNew;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class EmailConsumer {

	@Autowired
	private EmailServiceImplNew emailService;

	@RabbitListener(queues = "${spring.rabbitmq.queue}")
	public void listen(@Payload EmailDTO emailDTO){
		Email email = new Email();
		BeanUtils.copyProperties(emailDTO, email);
		EmailDTO emailSent = emailService.sendEmail(email);
		System.out.println("@@@@@@@ Email");
		System.out.println(email);
		System.out.println(emailSent);
	}

}
