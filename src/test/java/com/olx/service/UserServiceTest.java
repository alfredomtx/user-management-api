package com.olx.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;

import com.olx.controller.dto.UserDTO;
import com.olx.model.User;
import com.olx.repository.UserRepository;
import com.olx.service.impl.UserServiceImpl;

class UserServiceTest {

	@TestConfiguration
	static class UserServiceTestConfiguration {

		@Bean
		public UserServiceImpl userService() {
			return new UserServiceImpl();
		}

	}

	@Autowired
	private UserServiceImpl userService;

	@MockBean
	private UserRepository userRepository;

	private UserDTO defaultUserDTO;
	private User defaultUser;

	// setup context dependencies for the tests
	@BeforeEach
	public void setup() {

		Long id = 1L;
		String email = "test@test.com";
		String firstName = "John";
		String lastName = "Snow";

		UserDTO defaultUserDTO = new UserDTO();
		defaultUserDTO.setId(id);
		defaultUserDTO.setEmail(email);
		defaultUserDTO.setFirstName(firstName);
		defaultUserDTO.setLastName(lastName);

		User defaultUser = new User();
		defaultUser.setId(id);
		defaultUser.setEmail(email);
		defaultUser.setFirstName(firstName);
		defaultUser.setLastName(lastName);
		

		Mockito.when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(defaultUser));

	}

	@Test
	void shouldReturnUser_WhenRequestByEmail() {

		
		String email = "test@test.com";

	
		
		UserDTO user = userService.getByEmail(email);

		Assertions.assertEquals(user.getId(), 1L);

	}

}
