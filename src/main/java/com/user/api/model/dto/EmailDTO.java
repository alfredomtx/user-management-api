package com.user.api.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.user.api.enums.StatusEmail;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailDTO {

	@Id
	private Long id;

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