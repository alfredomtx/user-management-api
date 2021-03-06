package com.user.api.user.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRequestDTO {

	@NotBlank(message = "Email is blank.")
	@Email(message = "Not a valid email.")
	@Size(min = 4, max = 255, message = "Field is too short(less than 4) or too long(more than 255).")
	private String email;

	@NotBlank(message = "Password is blank.")
	@Size(min = 4, max = 255, message = "Field is too short(less than 4) or too long(more than 255).")
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;

	@NotBlank(message = "First name is blank.")
	@Size(min = 2, max = 255, message = "Field is too short(less than 2) or too long(more than 255).")
	private String firstName;

	@NotBlank(message = "Last name is blank.")
	@Size(min = 2, max = 255, message = "Field is too short(less than 2) or too long(more than 255).")
	private String lastName;

}
