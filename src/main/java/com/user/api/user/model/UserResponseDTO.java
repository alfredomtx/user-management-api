package com.user.api.user.model;

import lombok.*;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class UserResponseDTO {

	private Long id;
	private String email;
	private String firstName;
	private String lastName;

	@Temporal(TemporalType.TIMESTAMP)
	private Date creationDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date updateDate;

	private boolean active;

}
