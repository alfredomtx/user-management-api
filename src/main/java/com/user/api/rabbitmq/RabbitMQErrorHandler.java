package com.user.api.rabbitmq;

import com.user.api.rabbitmq.model.FailedMessage;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.util.ErrorHandler;


public class RabbitMQErrorHandler implements ErrorHandler {

	@Override
	public void handleError(Throwable t) {
		String queueName = ((ListenerExecutionFailedException) t).getFailedMessage().getMessageProperties().getConsumerQueue();
		String message = new String(((ListenerExecutionFailedException) t).getFailedMessage().getBody());

		FailedMessage failedMessage = new FailedMessage();
		failedMessage.setQueueName(queueName);
		failedMessage.setQueueMessage(message);
		failedMessage.setErrorCause(t.getCause().getMessage());

		/*try {
			failedMessageRepository.save(failedMessage);
		} catch (Exception e){
			e.printStackTrace();
		}*/

		System.out.println("------------------------");
		System.out.println(failedMessage);
		System.out.println("------------------------");

		throw new AmqpRejectAndDontRequeueException("Shall not return to queue");

	}
}
