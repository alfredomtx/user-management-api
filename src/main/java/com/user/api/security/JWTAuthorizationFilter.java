package com.user.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
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

	public static final String ATTRIBUTE_PREFIX = "Bearer ";

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
		UsernamePasswordAuthenticationToken authToken = getAuthenticationToken(token, request);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		filterChain.doFilter(request, response);
	}

	// reads the token and return the user data to ensure it's a valid user
	private UsernamePasswordAuthenticationToken getAuthenticationToken(String token, HttpServletRequest request) {
		try {
			Algorithm algorithm = Algorithm.HMAC512(JWTAuthenticationFilter.TOKEN_PASSWORD);
			JWTVerifier verifier = JWT.require(algorithm).build();
			DecodedJWT decodedJWT = verifier.verify(token);

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
			throw new TokenExpiredException(e.getMessage());
		} catch (Exception e){
			request.setAttribute("otherException", e.getMessage());
			throw e;

		}

	}


}
