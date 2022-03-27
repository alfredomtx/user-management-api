package com.user.api.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailDTO {

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

}