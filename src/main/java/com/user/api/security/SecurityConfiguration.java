package com.user.api.security;

import com.user.api.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetailService userDetailService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder encoder;

	// configure spring security to use the project's classes as base classes of implementation
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailService).passwordEncoder(encoder);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// override exception handling of spring security to handle with AuthFailureHandler
		http.exceptionHandling().authenticationEntryPoint(new AuthFailureHandler());

		// create the instance to be able to change URL and other things
		JWTAuthenticationFilter customAuthenticationFilter = new JWTAuthenticationFilter(authenticationManager(), userRepository);
		customAuthenticationFilter.setFilterProcessesUrl("/api/login");

		http.csrf().disable();
		http.cors();

		http.authorizeRequests()

				/*
					URLs allowed for non-authenticated users (anyone on the internet)
				*/
				.antMatchers(HttpMethod.POST, "/api/login").permitAll()
				.antMatchers(HttpMethod.GET, "/api/token/refresh").permitAll()
				.antMatchers(HttpMethod.POST, "/api/user/requestResetPassword").permitAll()
				.antMatchers(HttpMethod.GET, "/api/user/resetPassword/**").permitAll()
				.antMatchers(HttpMethod.POST, "/api/registration/requestActivateAccountEmail").permitAll()
				.antMatchers(HttpMethod.GET, "/api/registration/activateAccount/**").permitAll()
				.antMatchers("/").permitAll()

				.antMatchers("/api/user/admin/**").hasRole("ADMIN")
				// .antMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
				/*
					everything else allowed only for Admin
				*/
				.antMatchers("/api/**").hasRole("ADMIN")

				.anyRequest().authenticated()
				.and()
				.addFilter(customAuthenticationFilter)
				.addFilterBefore(new JWTAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
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
