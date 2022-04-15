package com.user.api.userProperties.model;

import com.user.api.user.model.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@ToString
@Getter
@Setter
public class UserProperties {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "user_properties_sequence")
	@SequenceGenerator(name = "user_properties_sequence", sequenceName = "user_properties_sequence", allocationSize = 1)
	private Long id;

	@OneToOne
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@Column(columnDefinition = "TEXT")
	private String resetPasswordToken;

	@Column(columnDefinition = "TEXT")
	private String activateAccountToken;


}
