package com.user.api.rabbitmq;

import com.user.api.rabbitmq.model.FailedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FailedMessageRepository extends JpaRepository<FailedMessage, Long> {

	Optional<FailedMessage> findById(Long id);
}
