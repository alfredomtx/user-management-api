package com.user.core.api.security;

import com.user.core.api.exceptions.StandardError;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
			StandardError error = new StandardError(HttpStatus.UNAUTHORIZED, expired, httpServletRequest.getRequestURI());
			httpServletResponse.getOutputStream().println(error.toString());
			return;
		}

		final String badCredentials = (String) httpServletRequest.getAttribute("badCredentials");
		if (badCredentials != null) {
			StandardError error = new StandardError(HttpStatus.UNAUTHORIZED, badCredentials, httpServletRequest.getRequestURI());
			httpServletResponse.getOutputStream().println(error.toString());
			return;
		}

		System.out.println(e);

		StandardError error = new StandardError(HttpStatus.UNAUTHORIZED, e.getMessage(), httpServletRequest.getRequestURI());
		httpServletResponse.getOutputStream().println(error.toString());
	}
}

