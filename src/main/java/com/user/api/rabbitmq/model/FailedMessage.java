package com.user.api.rabbitmq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name="rabbitmq_failed_messages")
public class FailedMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	private String queueName;

	@Column(columnDefinition = "TEXT")
	private String queueMessage;

	@Column(columnDefinition = "TEXT")
	private String errorMessage;

	@Column(columnDefinition = "TEXT")
	private String errorCause;

	@Column(updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@CreationTimestamp
	private Date creationDate;

}
