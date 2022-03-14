package com.olx.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@ToString
@Getter @Setter
public class UserPasswordDTO {
	
	@NotBlank(message = "Password is blank.")
	@Size(min = 4, max = 255, message = "Password is too short(less than 4) or too big(more than 255).")
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;


}
