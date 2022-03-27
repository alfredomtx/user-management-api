package com.user.api.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class StandardError {

	private HttpStatus status;
	private String error;
	private String path;

	public String toString() {
		return "{"
				+ "\"code\": " + status.value() + ","
				+ "\"status\": \"" + status.name() + "\","
				+ "\"timestamp\": \"" + LocalDateTime.now() + "\","
				+ "\"message\": \"" + error + "\","
				+ "\"path\": \"" + path + "\""
				+ "}";
	}


}
