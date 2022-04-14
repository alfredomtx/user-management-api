package com.user.api.rabbitmq;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class RabbitMQConnection {

	@Value("${spring.rabbitmq.queue}")
	private String queue;
	@Value("${spring.rabbitmq.exchange}")
	private String exchange;

	private final AmqpAdmin amqpAdmin;

	private RabbitMQConnection(AmqpAdmin amqpAdmin){
		this.amqpAdmin = amqpAdmin;
	}

	private Queue queue(String queueName){
		return new Queue(queueName, true, false, false);
	}

	private DirectExchange directExchange(){
		return new DirectExchange(exchange);
	}

	private Binding relationship(Queue queue, DirectExchange exchange){
		return new Binding(queue.getName(), Binding.DestinationType.QUEUE, exchange.getName()
				, queue.getName(), null);
	}

	// Run add method after the class is initialized, because of @Component annotation
	@PostConstruct
	private void add(){
		Queue emailQueue = queue(queue);

		DirectExchange exchange = directExchange();

		Binding relation = relationship(emailQueue, exchange);

		amqpAdmin.declareQueue(emailQueue);
		amqpAdmin.declareExchange(exchange);
		amqpAdmin.declareBinding(relation);
	}

}
