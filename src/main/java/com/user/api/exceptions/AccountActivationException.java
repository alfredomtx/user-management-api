package com.user.api.exceptions;

public class AccountActivationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public AccountActivationException(String message) {
		super(message);
	}

}
