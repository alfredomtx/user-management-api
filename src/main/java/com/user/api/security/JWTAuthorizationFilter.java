package com.user.api.security;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.user.api.security.util.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class JWTAuthorizationFilter extends OncePerRequestFilter {
	private static final Logger logger = LoggerFactory.getLogger(JWTAuthorizationFilter.class);

	public static final String ATTRIBUTE_PREFIX = "Bearer ";

	private String tokenPassword;

	public JWTAuthorizationFilter(String tokenPassword){
		this.tokenPassword = tokenPassword;
	}

	// override method to intercept request header
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws IOException, ServletException, TokenExpiredException {

		String authorizationHeader = request.getHeader(AUTHORIZATION);
		if (authorizationHeader == null) {
			request.setAttribute("noAuthorizationHeader", "\"Authorization\" header is missing.");
			filterChain.doFilter(request, response);
			return;
		}
		if (!authorizationHeader.startsWith(ATTRIBUTE_PREFIX)) {
			request.setAttribute("noBearerTokenHeader", "Authorization header value \"Bearer $token\" is missing.");
			filterChain.doFilter(request, response);
			return;
		}

		String token = authorizationHeader.replace(ATTRIBUTE_PREFIX, "");
		UsernamePasswordAuthenticationToken authToken;
		try {
			authToken = getAuthenticationToken(token, request);
		} catch (Exception e){
			logger.error("Error authenticating user: " + e);
			filterChain.doFilter(request, response);
			return;
		}
		SecurityContextHolder.getContext().setAuthentication(authToken);
		filterChain.doFilter(request, response);
	}

	// reads the token and return the user data to ensure it's a valid user
	private UsernamePasswordAuthenticationToken getAuthenticationToken(String token, HttpServletRequest request) {
		try {
			DecodedJWT decodedJWT = JWTUtil.verifyToken(token, tokenPassword);

			String email = decodedJWT.getSubject();
			String[] roles = decodedJWT.getClaim("roles").asArray(String.class);

			Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
			Arrays.stream(roles).forEach(role -> {
				authorities.add(new SimpleGrantedAuthority(role));
			});

			return new UsernamePasswordAuthenticationToken(email, null, authorities);
		} catch (TokenExpiredException e) {
			// setting header to be able to identify the error and send a custom response on AuthFailureHandler
			request.setAttribute("expired", e.getMessage());
			throw e;
		} catch (Exception e){
			request.setAttribute("otherException", e.getMessage());
			throw e;

		}

	}


}
