package com.user.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.api.user.UserRepository;
import com.user.api.user.model.UserRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {


	// TODO find a way to get the token info from application properties file
	@Value("${spring.security.token.expiration_minutes}")
	public final static int TOKEN_EXPIRATION_MINUTES = 100000;
	@Value("${spring.security.token.password}")
	// Token unique password for sign, generate on https://guidgenerator.com/
	public final static String TOKEN_PASSWORD = "d2eb2c8d-bafe-4e81-8c1a-ac0e58c6c652";

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
		} catch (Exception e) {
			throw new RuntimeException("Failed to authenticate user", e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
											Authentication authResult) throws IOException {
		UserDetailData userData = (UserDetailData) authResult.getPrincipal();

		// set token expiration with current time + TOKEN_EXPIRATION_MINUTES
		Date tokenExpiration = new Date(System.currentTimeMillis() + ((TOKEN_EXPIRATION_MINUTES * 60) * 1000));

		String accessToken = JWT.create()
				.withSubject(userData.getUsername())
				.withExpiresAt(tokenExpiration)
				.withIssuer(request.getRequestURL().toString())
				.withClaim("roles"
						, userData.getAuthorities().stream().map(GrantedAuthority::getAuthority)
								.collect(Collectors.toList()))
				.sign(Algorithm.HMAC512(TOKEN_PASSWORD));

		String refreshToken = JWT.create()
				.withSubject(userData.getUsername())
				.withExpiresAt(tokenExpiration)
				.withIssuer(request.getRequestURL().toString())
				.sign(Algorithm.HMAC512(TOKEN_PASSWORD));

		response.setHeader("access_token", accessToken);
		response.setHeader("refresh_token", refreshToken);
		Map<String, String> tokens = new HashMap<>();
		tokens.put("access_token", accessToken);
		tokens.put("refresh_token", refreshToken);
		response.setContentType(APPLICATION_JSON_VALUE);
		new ObjectMapper().writeValue(response.getOutputStream(), tokens);




	}


}
