package com.user.core.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice
public class ControllerExceptionHandler {

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<StandardError> userNotFoundException(UserNotFoundException e, HttpServletRequest request) {
		StandardError error = new StandardError(HttpStatus.NOT_FOUND, LocalDateTime.now(), e.getMessage(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(InvalidUserDataException.class)
	public ResponseEntity<StandardError> invalidUserDataException(InvalidUserDataException e, HttpServletRequest request) {
		StandardError error = new StandardError(HttpStatus.BAD_REQUEST, LocalDateTime.now(), e.getMessage(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<StandardError> userAlreadyExistsException(UserAlreadyExistsException e, HttpServletRequest request) {
		StandardError error = new StandardError(HttpStatus.CONFLICT, LocalDateTime.now(), e.getMessage(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}

	// override exception of JPA field validations when the @Valid notation is in the request param
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<StandardError> fieldValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
		StandardError error = new StandardError(HttpStatus.CONFLICT, LocalDateTime.now(), e.getBindingResult().getFieldError().getDefaultMessage(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<StandardError> constraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
		StringBuilder errors = new StringBuilder();
		errors.append("[");
		int item = 1;
		for (ConstraintViolation<?> validationError : e.getConstraintViolations()){
			errors.append("{\"" + validationError.getPropertyPath() + "\"");
			errors.append(": ");
			errors.append("\"" + validationError.getMessage() + "\"");
			errors.append("}");
			if (item != e.getConstraintViolations().size())
				errors.append(",");
			item++;
		}
		errors.append("]");

		StandardError error = new StandardError(HttpStatus.BAD_REQUEST, LocalDateTime.now(), errors.toString(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(UserFieldsValidationException.class)
	public ResponseEntity<String> userFieldsValidationException(UserFieldsValidationException e, HttpServletRequest request) {
		List<FieldError> errors = e.getErrors();
		UserFieldsValidationError error = new UserFieldsValidationError(HttpStatus.BAD_REQUEST, errors, request);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.toString());
	}



}
