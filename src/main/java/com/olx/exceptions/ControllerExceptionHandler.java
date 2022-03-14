package com.olx.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@ControllerAdvice
public class ControllerExceptionHandler {

	@ExceptionHandler(ObjectNotFoundException.class)
	public ResponseEntity<StandardError> invalidUserData(ObjectNotFoundException e, HttpServletRequest request){
		StandardError error = new StandardError(HttpStatus.NOT_FOUND.value(), LocalDateTime.now(), e.getMessage(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<StandardError> invalidUserData(UserNotFoundException e, HttpServletRequest request){
		StandardError error = new StandardError(HttpStatus.NOT_FOUND.value(), LocalDateTime.now(), e.getMessage(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(InvalidUserDataException.class)
	public ResponseEntity<StandardError> invalidUserData(InvalidUserDataException e, HttpServletRequest request){
		StandardError error = new StandardError(HttpStatus.BAD_REQUEST.value(), LocalDateTime.now(), e.getMessage(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<StandardError> invalidUserData(UserAlreadyExistsException e, HttpServletRequest request){
		StandardError error = new StandardError(HttpStatus.CONFLICT.value(), LocalDateTime.now(), e.getMessage(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}

	// override exception of JPA field validations when the @Valid notation is in the request param
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<StandardError> fieldValidationException(MethodArgumentNotValidException e, HttpServletRequest request){
		StandardError error = new StandardError(HttpStatus.CONFLICT.value(), LocalDateTime.now(), e.getBindingResult().getFieldError().getDefaultMessage(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}

}
