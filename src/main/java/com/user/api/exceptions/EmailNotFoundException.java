package com.user.api.exceptions;

public class EmailNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public EmailNotFoundException(Long id) {
		super("Email with id [" + id + "] not found.");
	}
}
