package com.olx.model;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@ToString
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private @Getter @Setter Long id;

	@NotBlank(message = "Email is blank.")
	@Email(message = "Not a valid email.")
	@Size(min = 4, max = 255, message = "Field is too short(less than 4) or too big(more than 255).")
	@Column(unique = true)
	private @Getter @Setter String email;

	@NotBlank(message = "Password is blank.")
	@Size(min = 4, max = 255, message = "Field is too short(less than 4) or too big(more than 255).")
	// annotation to not return the password field on requests
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private @Getter @Setter String password;

	@NotBlank(message = "First name is blank.")
	@Size(min = 2, max = 255, message = "Field is too short(less than 2) or too big(more than 255).")
	private @Getter @Setter String firstName;

	@NotBlank(message = "Last name is blank.")
	@Size(min = 2, max = 255, message = "Field is too short(less than 2) or too big(more than 255).")
	private @Getter @Setter String lastName;
	
	private LocalDate creationDate;
	
}
