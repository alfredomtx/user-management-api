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
	public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e)
			throws IOException, ServletException {
		httpServletResponse.setContentType("application/json");
		httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		final String expired = (String) httpServletRequest.getAttribute("expired");
		if (expired != null) {
			StandardError error = new StandardError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.name(), expired
					, httpServletRequest.getRequestURI(), LocalDateTime.now());
			httpServletResponse.getOutputStream().println(error.toString());
			return;
		}

		final String badCredentials = (String) httpServletRequest.getAttribute("badCredentials");
		if (badCredentials != null) {
			StandardError error = new StandardError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.name(), badCredentials
					, httpServletRequest.getRequestURI(), LocalDateTime.now());
			httpServletResponse.getOutputStream().println(error.toString());
			return;
		}

		final String noAuthorizationHeader = (String) httpServletRequest.getAttribute("noAuthorizationHeader");
		if (noAuthorizationHeader != null) {
			StandardError error = new StandardError(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(), noAuthorizationHeader
					, httpServletRequest.getRequestURI(), LocalDateTime.now());
			httpServletResponse.getOutputStream().println(error.toString());
			return;
		}

		final String noBearerTokenHeader = (String) httpServletRequest.getAttribute("noBearerTokenHeader");
		if (noBearerTokenHeader != null) {
			StandardError error = new StandardError(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(), noBearerTokenHeader
					, httpServletRequest.getRequestURI(), LocalDateTime.now());
			httpServletResponse.getOutputStream().println(error.toString());
			return;
		}

		StandardError error = new StandardError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.name(), e.getMessage()
				, httpServletRequest.getRequestURI(), LocalDateTime.now());
		httpServletResponse.getOutputStream().println(error.toString());
	}
}

