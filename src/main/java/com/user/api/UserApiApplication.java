package com.user.api;

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

	/*@Bean
	CommandLineRunner run(UserService userService, UserRepository userRepository){
		return args -> {
			*//*
			* Create default Admin and Test user by default
			* *//*
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
				userRepository.save(user);
			}

			try {
				userService.getByEmail("test@test.com");
			} catch (UserNotFoundException e){
				UserRequestDTO user = new UserRequestDTO();
				user.setFirstName("User");
				user.setLastName("Test");
				user.setEmail("test@test.com");
				user.setPassword(getPasswordEncoder().encode("test"));
				userService.add(user);
			}
		};
	}*/

}
