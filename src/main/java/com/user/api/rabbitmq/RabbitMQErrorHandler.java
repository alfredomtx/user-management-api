package com.user.api.rabbitmq;

import com.user.api.rabbitmq.model.FailedMessage;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.util.ErrorHandler;


@AllArgsConstructor
public class RabbitMQErrorHandler implements ErrorHandler {
	private static final Logger logger = LoggerFactory.getLogger(RabbitMQErrorHandler.class);

	private FailedMessageRepository failedMessageRepository;


	@Override
	public void handleError(Throwable t) {
		String queueName = ((ListenerExecutionFailedException) t).getFailedMessage().getMessageProperties().getConsumerQueue();
		String message = new String(((ListenerExecutionFailedException) t).getFailedMessage().getBody());

		FailedMessage failedMessage = new FailedMessage();
		failedMessage.setQueueName(queueName);
		failedMessage.setQueueMessage(message);
		failedMessage.setErrorClass(t.getCause().getClass().getName());
		failedMessage.setErrorMessage(t.getCause().getMessage());

		FailedMessage failedMessageSaved = null;
		try {
			failedMessageSaved = failedMessageRepository.save(failedMessage);
		} catch (Exception e){
			e.printStackTrace();
			logger.error("Error saving failed message: " + e);
		}

		Long id = null;
		if (failedMessageSaved != null)
			id = failedMessageSaved.getId();

		throw new AmqpRejectAndDontRequeueException("Message will not return to queue. " +
				"Failed message id: " + id);
	}
}
