package com.user.core.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JWTValidateFilter extends BasicAuthenticationFilter {

	public static final String HEADER_ATTRIBUTE = "Authorization";
	public static final String ATTRIBUTE_PREFIX = "Bearer ";

	public JWTValidateFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
	}

	// override method to intercept request header
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException, TokenExpiredException {

		String attribute = request.getHeader(HEADER_ATTRIBUTE);
		if (attribute == null) {
			request.setAttribute("noAuthorizationHeader", "\"Authorization\" header is missing.");
			chain.doFilter(request, response);
			return;
		}
		if (!attribute.startsWith(ATTRIBUTE_PREFIX)) {
			request.setAttribute("noBearerTokenHeader", "Authorization header value \"Bearer $token\" is missing.");
			chain.doFilter(request, response);
			return;
		}

		String token = attribute.replace(ATTRIBUTE_PREFIX, "");
		UsernamePasswordAuthenticationToken authToken = getAuthenticationToken(token, request);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		chain.doFilter(request, response);
	}

	// reads the token and return the user data to ensure it's a valid user
	private UsernamePasswordAuthenticationToken getAuthenticationToken(String token, HttpServletRequest request) {
		String userTokenInfo;
		try {
			userTokenInfo = JWT.require(Algorithm.HMAC512(JWTAuthenticateFilter.TOKEN_PASSWORD))
					.build()
					.verify(token)
					.getSubject();
		} catch (TokenExpiredException e) {
			// setting header to be able to identify the error and send a custom response on AuthFailureHandler
			request.setAttribute("expired", e.getMessage());
			throw new TokenExpiredException(e.getMessage());
		}

		if (userTokenInfo == null) {
			return null;
		}

		// splitting the token subject that will come as "email,ROLE"
		String[] result = userTokenInfo.split(",");

		String email = (result[0] != null) ? result[0] : "";
		String role = (result[1] != null) ? result[1] : null;

		List<GrantedAuthority> listRole = new ArrayList<GrantedAuthority>();
		if (role != null){
			listRole.add(new SimpleGrantedAuthority(role));
		}

		return new UsernamePasswordAuthenticationToken(userTokenInfo, null, listRole);
	}


}
