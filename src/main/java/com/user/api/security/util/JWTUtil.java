package com.user.api.security.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;
import java.util.List;

public class JWTUtil {

	public static DecodedJWT verifyToken(String token, String tokenPassword){
		Algorithm algorithm = Algorithm.HMAC512(tokenPassword);
		JWTVerifier verifier = JWT.require(algorithm).build();
		DecodedJWT decodedJWT = verifier.verify(token);
		return decodedJWT;
	}

	public static String createToken(String username, int tokenExpirationMinutes, String issuer, String tokenPassword){
		Algorithm algorithm = Algorithm.HMAC512(tokenPassword);
		String token = JWT.create()
				.withSubject(username)
				.withExpiresAt(getTokenExpiration(tokenExpirationMinutes))
				.withIssuer(issuer)
				.sign(algorithm);
		return token;
	}

	public static String createTokenLogin(String username, List<String> roles, int tokenExpirationMinutes
			, String issuer, String tokenPassword){
		Algorithm algorithm = Algorithm.HMAC512(tokenPassword);

		String token = JWT.create()
				.withSubject(username)
				.withClaim("roles", roles)
				.withIssuer(issuer)
				.withExpiresAt(getTokenExpiration(tokenExpirationMinutes))
				.sign(algorithm);
		return token;
	}

	private static Date getTokenExpiration(int tokenExpirationMinutes){
		return new Date(System.currentTimeMillis() + ((tokenExpirationMinutes * 60) * 1000));
	}

}
