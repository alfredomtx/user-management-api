package com.user.core.api.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public UserAlreadyExistsException(Long id) {
		super("User with id [" + id + "] already exists.");
	}

	public UserAlreadyExistsException(String email) {
		super("User with e-mail [" + email + "] already exists.");
	}
	
	
	
}
