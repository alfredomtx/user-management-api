package com.olx.controller;

import com.olx.model.User;
import com.olx.model.dto.UserDTO;
import com.olx.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class UserControllerMvcTest {

	@Autowired
	private MockMvc mvc;

	private UserController controller;

	@Mock
	private UserService userService;

	public static final Long ID = 1L;
	public static final String EMAIL = "test@test.com";
	public static final String PASSWORD = "test";
	public static final String FIRST_NAME = "John";
	public static final String LAST_NAME = "Snow";

	private User user;
	private UserDTO userDTO;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}


	@Test
	void getAll() throws Exception {
		mvc.perform(get("/api/user/"))
				.andExpect(status().isOk());
	}

	@Test
	void getById() {
	}

	@Test
	void add() {
	}

	@Test
	void update() {
	}

	@Test
	void delete() {
	}

	@Test
	void changePassword() {
	}

	@Test
	void validateLogin() {
	}
}