package com.olx.model.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/*
	UserUpdateDTO does not contain the "password" field,
	so it's possible to use @Valid notation to validate all other fields
	and the password will have a separated method implementation
	for update at "/api/user/:id/changePassword"
*/

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter @Setter
public class UserUpdateDTO {

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
