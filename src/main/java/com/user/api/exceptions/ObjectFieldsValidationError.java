package com.user.api.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class ObjectFieldsValidationError {

	private HttpStatus status;
	private List<FieldError> errors;
	private HttpServletRequest request;

	public String toString() {
		return "{"
				+ "\"code\": " + status.value() + ","
				+ "\"status\": \"" + status.name() + "\","
				+ "\"timestamp\": \"" + LocalDateTime.now() + "\","
				+ "\"message\": " + getErrorsJson() + ","
				+ "\"path\": \"" + request.getRequestURI() + "\""
				+ "}";
	}

	private String getErrorsJson() {
		StringBuilder errorsJson = new StringBuilder();
		errorsJson.append("[");
		int item = 1;
		for (FieldError e : errors) {
			errorsJson.append("{\"" + e.getField() + "\"");
			errorsJson.append(": ");
			errorsJson.append("\"" + e.getDefaultMessage() + "\"");
			errorsJson.append("}");
			if (item != errors.size())
				errorsJson.append(",");
			item++;
		}
		errorsJson.append("]");
		return errorsJson.toString();

	}


}
