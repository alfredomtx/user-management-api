package com.user.api.user.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;

	@Temporal(TemporalType.TIMESTAMP)
	private Date creationDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date updateDate;

}
