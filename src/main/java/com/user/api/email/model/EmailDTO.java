package com.user.api.email.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.user.api.email.enums.StatusEmail;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailDTO implements Serializable {

	@Id
	private long id;

	@NotNull
	private Long userId;

	@NotBlank
	@Email
	private String addressFrom;

	@NotBlank
	@Email
	private String addressTo;

	@NotBlank
	private String recipientName;

	@NotBlank
	private String subject;

	@NotBlank
	private String body;

	@Enumerated(EnumType.STRING)
	private StatusEmail status;

}