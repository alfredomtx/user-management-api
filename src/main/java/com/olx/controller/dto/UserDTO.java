package com.olx.controller.dto;

import com.olx.model.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter @Setter
public class UserDTO {

	private Long id;
	private String email;
	private String firstName;
	private String lastName;

	public static UserDTO converter(User u) {
		UserDTO user = new UserDTO();

		user.setId(u.getId());
		user.setEmail(u.getEmail());
		user.setFirstName(u.getFirstName());
		user.setLastName(u.getLastName());

		return user;
	}

}
