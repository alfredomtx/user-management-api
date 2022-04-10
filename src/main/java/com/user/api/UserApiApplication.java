package com.user.api;

import com.user.api.exceptions.UserNotFoundException;
import com.user.api.user.UserRepository;
import com.user.api.user.UserService;
import com.user.api.user.enums.Role;
import com.user.api.user.model.User;
import com.user.api.userProperties.model.UserProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication()
public class UserApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserApiApplication.class, args);
	}

	@Bean
	public PasswordEncoder getPasswordEncoder() {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder;
	}

	@Bean
	CommandLineRunner run(UserService userService, UserRepository userRepository){
		return args -> {
			/*
			* Create default Admin and Test user by default
			* */
			try {
				userService.getByEmail("admin@admin.com");
			} catch (UserNotFoundException e){
				User user = new User();
				user.setFirstName("Admin");
				user.setLastName("Default");
				user.setActive(true);
				user.setEmail("admin@admin.com");
				user.setPassword(getPasswordEncoder().encode("admin"));
				user.setRole(Role.ROLE_ADMIN);

				UserProperties userProps = new UserProperties();
				userProps.setUser(user);
				user.setUserProperties(userProps);
				userProps.setUser(user);

				userRepository.save(user);
			}

			try {
				userService.getByEmail("test@test.com");
			} catch (UserNotFoundException e){
				User user = new User();
				user.setFirstName("User");
				user.setLastName("Test");
				user.setActive(true);
				user.setEmail("test@test.com");
				user.setPassword(getPasswordEncoder().encode("test"));
				user.setRole(Role.ROLE_USER);

				UserProperties userProps = new UserProperties();
				userProps.setUser(user);
				user.setUserProperties(userProps);
				userProps.setUser(user);

				userRepository.save(user);
			}
		};
	}

}
