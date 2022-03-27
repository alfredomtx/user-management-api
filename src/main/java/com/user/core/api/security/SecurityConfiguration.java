package com.user.core.api.security;

import com.user.core.api.repository.UserRepository;
import com.user.core.api.service.impl.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.sql.DataSource;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetailServiceImpl userDetailService;

	@Autowired
	private PasswordEncoder encoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DataSource dataSource;

	// configure spring security to use the project's classes as base classes of implementation
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailService).passwordEncoder(encoder);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// override exception handling of spring security to handle with AuthFailureHandler
		http.exceptionHandling().authenticationEntryPoint(new AuthFailureHandler());

		http.csrf().disable();
		// http.csrf().ignoringAntMatchers("/api/**");

		http.authorizeRequests()
				.antMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
				.antMatchers("/api/**").hasRole("ADMIN")

				//.antMatchers("/", "static/css", "static/js").permitAll()
				.antMatchers(HttpMethod.POST, "/login").permitAll()
				.antMatchers(HttpMethod.GET, "/ping").permitAll()
				.antMatchers("/").permitAll()

				.anyRequest().authenticated()
				.and()
				.addFilter(new JWTAuthenticateFilter(authenticationManager(), userRepository))
				.addFilter(new JWTValidateFilter(authenticationManager()))
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();
		source.registerCorsConfiguration("/**", corsConfiguration);
		return source;
	}


}
