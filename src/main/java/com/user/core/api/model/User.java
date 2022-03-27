package com.user.core.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	@NotBlank(message = "Email is blank.")
	@Email(message = "Not a valid email.")
	@Size(min = 4, max = 255, message = "Field is too short(less than 4) or too big(more than 255).")
	@Column(unique = true)
	private String email;

	@NotBlank(message = "Password is blank.")
	@Size(min = 4, max = 255, message = "Field is too short(less than 4) or too big(more than 255).")
	private String password;

	@NotBlank(message = "First name is blank.")
	@Size(min = 2, max = 255, message = "Field is too short(less than 2) or too big(more than 255).")
	private String firstName;

	@NotBlank(message = "Last name is blank.")
	@Size(min = 2, max = 255, message = "Field is too short(less than 2) or too big(more than 255).")
	private String lastName;

	@Column(updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@CreationTimestamp
	private Date creationDate;

	@Column(insertable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@UpdateTimestamp
	private Date updateDate;

	private String token;

	@Temporal(TemporalType.TIMESTAMP)
	private Date tokenExpiration;

	private String role;

	private boolean active;


}
