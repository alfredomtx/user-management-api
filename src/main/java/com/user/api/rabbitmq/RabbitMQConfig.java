package com.user.api.rabbitmq;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.DirectRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	@Autowired
	private FailedMessageRepository failedMessageRepository;

	@Bean
	public RabbitListenerContainerFactory<DirectMessageListenerContainer> rabbitListenerContainerFactory(
			ConnectionFactory connectionFactory) {
		DirectRabbitListenerContainerFactory factory = new DirectRabbitListenerContainerFactory();

		factory.setConnectionFactory(connectionFactory);
		factory.setAcknowledgeMode(AcknowledgeMode.AUTO);

		factory.setErrorHandler(new RabbitMQErrorHandler(failedMessageRepository));
		return factory;
	}
}

