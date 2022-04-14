package com.user.api.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQService {

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${spring.rabbitmq.queue}")
	private String queue;

	public void sendMessage(Object message){
		try {
			String jsonMessage = objectMapper.writeValueAsString(message);
			rabbitTemplate.convertAndSend(queue, jsonMessage);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
