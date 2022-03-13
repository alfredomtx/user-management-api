package com.olx.controller.dto;

import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter @Setter
public class UserInsertDTO {

	@Id
	private Long id;

	@NotBlank(message = "Email is blank.")
	@Email(message = "Not a valid email.")
	@Size(min = 4, max = 255, message = "Field is too short(less than 4) or too big(more than 255).")
	private String email;
	
	@NotBlank(message = "First name is blank.")
	@Size(min = 2, max = 255, message = "Field is too short(less than 2) or too big(more than 255).")
	private String firstName;
	
	@NotBlank(message = "Last name is blank.")
	@Size(min = 2, max = 255, message = "Field is too short(less than 2) or too big(more than 255).")
	private String lastName;

}
