package com.user.api.security;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.api.security.util.JWTUtil;
import com.user.api.user.UserRepository;
import com.user.api.user.model.UserRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	public final int ACCESS_TOKEN_EXPIRATION_MINUTES = 60;

	@Autowired
	private AuthenticationManager authManager;

	@Autowired
	private UserRepository userRepository;

	public JWTAuthenticationFilter(AuthenticationManager authManager, UserRepository userRepository) {
		this.authManager = authManager;
		this.userRepository = userRepository;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {

		// map json body into user object
		try {
			UserRequestDTO user = new ObjectMapper().readValue(request.getInputStream(), UserRequestDTO.class);
			return authManager.authenticate(new UsernamePasswordAuthenticationToken(
					user.getEmail(),
					user.getPassword(),
					new ArrayList<>()
			));
		} catch (StreamReadException e) {
			throw new RuntimeException("Failed to authenticate user", e);
		} catch (BadCredentialsException e) {
			// setting header to be able to identify the error and send a custom response on AuthFailureHandler
			request.setAttribute("badCredentials", e.getMessage());
			throw new BadCredentialsException("Invalid user credentials", e);
		} catch (DisabledException e) {
			request.setAttribute("userDisabled", "User is not activated, check your email to active the account.");
			throw new DisabledException(e.getMessage());
		} catch (Exception e) {
			throw new RuntimeException("Failed to authenticate user", e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
											Authentication authResult) throws IOException {
		UserDetailData userData = (UserDetailData) authResult.getPrincipal();

		String accessToken = JWTUtil.createTokenLogin(userData.getUsername()
				, userData.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList())
				, ACCESS_TOKEN_EXPIRATION_MINUTES, request.getRequestURL().toString());

		response.setHeader("access_token", accessToken);
		Map<String, String> tokens = new HashMap<>();
		tokens.put("access_token", accessToken);
		response.setContentType(APPLICATION_JSON_VALUE);
		new ObjectMapper().writeValue(response.getOutputStream(), tokens);
	}


}
