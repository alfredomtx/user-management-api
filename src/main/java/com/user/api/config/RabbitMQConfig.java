package com.user.api.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class RabbitMQConfig {

	@Value("${spring.rabbitmq.queue}")
	private String queue;

	@Bean
	public Queue queue(){
		return new Queue(queue, true);
	}

	@Bean
	// create converter to be able to receive the Payload object from EmailConsumer->listen()
	public Jackson2JsonMessageConverter messageConverter(){
		return new Jackson2JsonMessageConverter();
	}

}
