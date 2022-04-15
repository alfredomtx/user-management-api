package com.user.api.user.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.user.api.user.enums.Role;
import com.user.api.userProperties.model.UserProperties;
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
@Table(name="users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "users_sequence")
	@SequenceGenerator(name = "users_sequence", sequenceName = "users_sequence", allocationSize = 1)
	private Long id;

	@NotBlank()
	@Email()
	@Size(min = 4, max = 255)
	@Column(unique = true)
	private String email;

	@NotBlank()
	@Size(min = 4, max = 255)
	private String password;

	@NotBlank()
	@Size(min = 2, max = 255)
	private String firstName;

	@NotBlank()
	@Size(min = 2, max = 255)
	private String lastName;

	@Column(updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@CreationTimestamp
	private Date creationDate;

	@Column(insertable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@UpdateTimestamp
	private Date updateDate;

	@Enumerated(EnumType.STRING)
	private Role role;

	private boolean active;

	@OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
	private UserProperties userProperties;

}
