package com.olx.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olx.data.UserDetailData;
import com.olx.model.User;

public class JWTAuthenticateFilter extends UsernamePasswordAuthenticationFilter {
	
	// token expiration of 10 minutes
	// private final static int TOKEN_EXPIRATION = 600_000;
	private final static int TOKEN_EXPIRATION = 600_000_000;
	
	// token unique password, generate on  https://guidgenerator.com/
	// TODO remove from source code and use a configuration file 
	public final static String TOKEN_PASSWORD = "d2eb2c8d-bafe-4e81-8c1a-ac0e58c6c652";
	
	@Autowired
	private AuthenticationManager authManager;
	
	public JWTAuthenticateFilter(AuthenticationManager authManager) {
		this.authManager = authManager;		
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {

		try {
			User user = new ObjectMapper().readValue(request.getInputStream(), User.class);
			
			return authManager.authenticate(new UsernamePasswordAuthenticationToken(
					user.getEmail(),
					user.getPassword(),
					new ArrayList<>() // user permissions
				));
		} catch (StreamReadException e) {
			throw new RuntimeException("Failed to authenticate user", e);
		} catch (DatabindException e) {
			throw new RuntimeException("Failed to authenticate user", e);
		} catch (IOException e) {
			throw new RuntimeException("Failed to authenticate user", e);
		}
		

	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {


		UserDetailData userData = (UserDetailData) authResult.getPrincipal();
		
		String token = JWT.create()
				.withSubject(userData.getUsername())
				// set token expiration with current time + TOKEN_EXPIRATION
				.withExpiresAt(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION))
				.sign(Algorithm.HMAC512(TOKEN_PASSWORD));
		
		
		response.getWriter().write(token);
		response.getWriter().flush();
	}
	
	
	
	

}
