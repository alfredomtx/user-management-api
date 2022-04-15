package com.user.api.email.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.user.api.email.enums.StatusEmail;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Email implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	private String addressFrom;

	private String addressTo;

	private String recipientName;

	private String subject;

	@Column(columnDefinition = "TEXT")
	private String body;

	private LocalDateTime sendDate;

	@Enumerated(EnumType.STRING)
	private StatusEmail status;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String errorDetails;
}