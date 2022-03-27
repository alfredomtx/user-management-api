package com.user.api.exceptions;

import lombok.Getter;
import org.springframework.validation.FieldError;

import java.util.List;

/*
 * Exception class used to collect the list of errors
 * to be used in UserFieldsValidationError on ControllerExceptionHandler
 * */
public class ObjectFieldsValidationException extends Exception {
	private static final long serialVersionUID = 1L;

	private @Getter
	List<FieldError> errors;

	public ObjectFieldsValidationException(List<FieldError> errors) {
		this.errors = errors;
	}

}