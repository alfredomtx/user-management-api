package com.user.api.security;

import com.user.api.exceptions.StandardError;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

/*
 * Class to Override spring security exceptions and respond in the same format as Controller's exceptions
 * */
public class AuthFailureHandler implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
			throws IOException, ServletException {
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		final String expired = (String) request.getAttribute("expired");
		if (expired != null) {
			setResponseError(request, response, HttpStatus.UNAUTHORIZED, expired);
			return;
		}

		final String badCredentials = (String) request.getAttribute("badCredentials");
		if (badCredentials != null) {
			setResponseError(request, response, HttpStatus.UNAUTHORIZED, badCredentials);
			return;
		}

		final String userDisabled = (String) request.getAttribute("userDisabled");
		if (userDisabled != null) {
			setResponseError(request, response, HttpStatus.UNAUTHORIZED, userDisabled);
			return;
		}

		final String noAuthorizationHeader = (String) request.getAttribute("noAuthorizationHeader");
		if (noAuthorizationHeader != null) {
			setResponseError(request, response, HttpStatus.BAD_REQUEST, noAuthorizationHeader);
			return;
		}

		final String noBearerTokenHeader = (String) request.getAttribute("noBearerTokenHeader");
		if (noBearerTokenHeader != null) {
			setResponseError(request, response, HttpStatus.BAD_REQUEST, noBearerTokenHeader);
			return;
		}

		final String otherException = (String) request.getAttribute("otherException");
		if (otherException != null) {
			setResponseError(request, response, HttpStatus.FORBIDDEN, otherException);
			return;
		}

		setResponseError(request, response, HttpStatus.FORBIDDEN, e.getMessage());
	}

	private void setResponseError(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String message) throws IOException {
		String URL = String.valueOf(request.getRequestURL());
		StandardError error = new StandardError(status.value(), status.name(), message
				, URL, LocalDateTime.now());
		response.getOutputStream().println(error.toString());
	}
}

