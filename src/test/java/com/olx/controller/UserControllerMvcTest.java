package com.olx.controller;

import com.olx.model.User;
import com.olx.model.dto.UserDTO;
import com.olx.repository.UserRepository;
import com.olx.service.UserService;
import com.olx.service.impl.UserDetailServiceImpl;
import com.olx.service.impl.UserServiceImpl;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;

@WebMvcTest(UserController.class)
class UserControllerMvcTest {

	@Autowired
	private MockMvc mockMvc;

	private UserController controller;

	@MockBean
	private UserRepository userRepository;


	@Mock
	private UserServiceImpl services;

	@MockBean
	private UserService service;




	@MockBean
	private UserDetailServiceImpl userDetailServiceImpl;


	public static final Long ID = 1L;
	public static final String EMAIL = "test@test.com";
	public static final String PASSWORD = "test";
	public static final String FIRST_NAME = "John";
	public static final String LAST_NAME = "Snow";

	private User user;
	private UserDTO userDTO;

	@BeforeEach
	public void setup(){
		MockitoAnnotations.openMocks(this);
		RestAssuredMockMvc.mockMvc(mockMvc);
	}

	@Test
	void getAll() throws Exception {
		List<UserDTO> userList = new ArrayList<>();
		userList.add(userDTO);

		Mockito.when(service.getAll()).thenReturn(userList);

		RestAssuredMockMvc
			.given()
				.auth().none()
			.when()
				.get("/api/user")
			.then()
				.statusCode(HttpStatus.OK.value())
				.body("$.size()", equalTo(1))
				.body("[0].id", equalTo(1));


		// mvc.perform(get("/api/user/")
			//.with(SecurityMockMvcRequestPostProcessors.user("teste@teste.com"))
		//)
		//.andExpect(status().isOk());
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