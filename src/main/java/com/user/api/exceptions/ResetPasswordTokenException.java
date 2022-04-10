package com.user.api.exceptions;

public class ResetPasswordTokenException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ResetPasswordTokenException(String message) {
		super(message);
	}

}
