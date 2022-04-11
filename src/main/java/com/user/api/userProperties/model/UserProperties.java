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
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@OneToOne
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@Column(columnDefinition = "TEXT")
	private String resetPasswordToken;

	@Column(columnDefinition = "TEXT")
	private String activateAccountToken;


}
