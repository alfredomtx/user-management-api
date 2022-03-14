package com.olx.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

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
	private @Getter @Setter String password;

	@NotBlank(message = "First name is blank.")
	@Size(min = 2, max = 255, message = "Field is too short(less than 2) or too big(more than 255).")
	private @Getter @Setter String firstName;

	@NotBlank(message = "Last name is blank.")
	@Size(min = 2, max = 255, message = "Field is too short(less than 2) or too big(more than 255).")
	private @Getter @Setter String lastName;

	@Column(updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@CreationTimestamp
	private @Getter Date creationDate;

	@Column(insertable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@UpdateTimestamp
	private @Getter Date updateDate;

}
