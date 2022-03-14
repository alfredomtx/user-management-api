package com.olx.controller;

import com.olx.exceptions.UserNotFoundException;
import com.olx.model.dto.UserDTO;
import com.olx.repository.UserRepository;
import com.olx.service.UserService;
import com.olx.service.impl.UserDetailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@WebMvcTest
class UserControllerTest {

	private final String API_URL = "/api/user";

	@Autowired
	private UserController userController;

	@MockBean
	private UserRepository ur;
	@MockBean
	private UserService userService;
	@MockBean
	private UserDetailServiceImpl udsi;

	// setup context dependencies for the tests
	@BeforeEach
	public void setup() {
		// RestAssuredMockMvc.standaloneSetup(userController);
	}

	@Test
	public void shouldReturnListOfUsers_WhenRequesting() {

		List<UserDTO> userList = new ArrayList<>();

		userList.add(new UserDTO());

		when(userService.getAll()).thenReturn(userList);
		//given().accept(ContentType.JSON).when().get(API_URL + "/").then().statusCode(HttpStatus.OK.value());
		
	}

	@Test
	public void shouldReturnSuccess_WhenSearchingUser() {
		Long userId = 1L;

		UserDTO user = new UserDTO();
		user.setId(userId);
		user.setEmail("test@test.com");
		user.setFirstName("John");
		user.setLastName("Snow");

		when(userService.getById(userId)).thenReturn(user);

		// given().accept(ContentType.JSON).when().get(API_URL + "/{id}", userId).then().statusCode(HttpStatus.OK.value());
	}

	@Test
	public void shouldReturnNotFound_WhenSearchingUser() {
		Long userId = 1L;

		when(userService.getById(userId)).thenThrow(new UserNotFoundException(""));

		// given().accept(ContentType.JSON).when().get(API_URL + "/{id}", userId).then().statusCode(HttpStatus.NOT_FOUND.value());
	}
	
	

}
