package com.olx.controller.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
public class UserPasswordDTO {
	
	@NotBlank(message = "Password is blank.")
	@Size(min = 4, max = 255, message = "Password is too short(less than 4) or too big(more than 255).")
	// annotation to not return the password field on requests
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;


}
