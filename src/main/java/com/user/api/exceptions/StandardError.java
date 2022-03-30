package com.user.api.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class StandardError {

	private int code;
	private String status;
	private String message;
	private String path;
	private LocalDateTime timestamp;

	public String toString() {
		return "{"
				+ "\"code\": " + code + ","
				+ "\"status\": \"" + status + "\","
				+ "\"timestamp\": \"" + timestamp + "\","
				+ "\"message\": \"" + message + "\","
				+ "\"path\": \"" + path + "\""
				+ "}";
	}


}
