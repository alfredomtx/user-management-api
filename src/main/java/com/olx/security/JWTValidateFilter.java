package com.olx.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

public class JWTValidateFilter extends BasicAuthenticationFilter {
	
	public static final String HEADER_ATTRIBUTE = "Authorization";
	public static final String ATTRIBUTE_PREFIX = "Bearer ";
	

	public JWTValidateFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
	}

	// override method to intercept request header
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		String attribute = request.getHeader(HEADER_ATTRIBUTE);
		
		if (attribute == null) {
			chain.doFilter(request, response);
			return;
		}
		
		if (!attribute.startsWith(ATTRIBUTE_PREFIX)) {
			chain.doFilter(request, response);
			return;
		}
		
		String token = attribute.replace(ATTRIBUTE_PREFIX, "");
		
		UsernamePasswordAuthenticationToken authToken = getAuthenticationToken(token);
		
		SecurityContextHolder.getContext().setAuthentication(authToken);
		
		chain.doFilter(request, response);
	}
	

	// reads the token and return the user data to ensure it's a valid user
	private UsernamePasswordAuthenticationToken getAuthenticationToken(String token) {

        String user = JWT.require(Algorithm.HMAC512(JWTAuthenticateFilter.TOKEN_PASSWORD))
                .build()
                .verify(token)
                .getSubject();

        if (user == null) {
            return null;
        }

        return new UsernamePasswordAuthenticationToken(user,null, new ArrayList<>());
		
	}
	
}
