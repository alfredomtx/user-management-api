package com.user.core.api.controller;

import com.user.core.api.model.User;
import com.user.core.api.model.dto.UserInsertDTO;
import com.user.core.api.model.dto.UserResponse;
import com.user.core.api.model.dto.UserUpdateDTO;
import com.user.core.api.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserControllerTest {

	@InjectMocks
	private UserController controller;

	// need to use the real implementation of ModelMapper in UserServiceImpl
	@Autowired
	private final ModelMapper mapper = new ModelMapper();

	@Mock
	private UserServiceImpl service;

	public static final Long ID = 1L;
	public static final String EMAIL = "test@test.com";
	public static final String PASSWORD = "test";
	public static final String FIRST_NAME = "John";
	public static final String LAST_NAME = "Snow";

	private User user;
	private UserResponse userResponse;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		startUser();
	}

	private void startUser(){

		user = new User();
		user.setId(ID);
		user.setEmail(EMAIL);
		user.setPassword(PASSWORD);
		user.setFirstName(FIRST_NAME);
		user.setLastName(LAST_NAME);

		userResponse = new UserResponse();
		userResponse.setId(ID);
		userResponse.setEmail(EMAIL);
		userResponse.setPassword(PASSWORD);
		userResponse.setFirstName(FIRST_NAME);
		userResponse.setLastName(LAST_NAME);
	}

	@Test
	void shouldReturnListOfUser_WhenGetAll() {
		List<UserResponse> userList = new ArrayList<>();
		userList.add(userResponse);

		when(service.getAll()).thenReturn(userList);

		ResponseEntity<List<UserResponse>> response = controller.getAll();

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(ArrayList.class, response.getBody().getClass());
		// list should return only 1 user
		assertEquals(1, response.getBody().size());
		// do the rest of validations for the first user of the list
		assertUserResponse(response.getBody().get(0));
	}

	@Test
	void shouldReturnUser_WhenGetById() {
		when(service.getById(anyLong())).thenReturn(userResponse);

		ResponseEntity<UserResponse> response = controller.getById(ID);

		assertResponseEntity(response, 200);

		assertNotNull(response.getBody());
		assertUserResponse(response.getBody());
	}

	@Test
	void shouldReturnUri_WhenAddUser() {
		UserInsertDTO userInsert = mapper.map(user, UserInsertDTO.class);

		when(service.add(any())).thenReturn(userResponse);

		ResponseEntity<String> response = controller.add(userInsert);

		assertResponseEntity(response, 201);

		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/" + ID).buildAndExpand(ID).toUri();
		assertEquals(response.getHeaders().getLocation(), uri);
	}

	@Test
	void shouldReturnOk_WhenUpdateUser_WithSuccess() {
		UserUpdateDTO userUpdate = mapper.map(user, UserUpdateDTO.class);

		when(service.update(anyLong(), any())).thenReturn(userResponse);

		ResponseEntity<String> response = controller.update(ID, userUpdate);
		assertResponseEntity(response, 200);
	}

	@Test
	void shouldReturnOk_WhenChangePassword() {
		doNothing().when(service).changePassword(anyLong(), anyString());

		ResponseEntity<String> response = controller.changePassword(ID, "123test");

		verify(service, times(1)).changePassword(anyLong(), anyString());
		assertResponseEntity(response, 200);
		assertEquals(response.getBody(), "User password updated with success.");
	}

	@Test
	void shouldReturnVoid_WhenDeleteUser() {
		doNothing().when(service).delete(anyLong());

		ResponseEntity<UserResponse> response = controller.delete(ID);

		verify(service, times(1)).delete(anyLong());
		assertResponseEntity(response, 204);
	}

	@Test
	void shouldReturnTrue_WhenValidateLogin() {
		when(service.validateLogin(user)).thenReturn(true);

		ResponseEntity<Boolean> response = controller.validateLogin(user);

		assertResponseEntity(response, 200);
		assertNotNull(response.getBody());
		assertEquals(response.getBody(), true);
	}

	@Test
	void shouldReturnFalse_WhenValidateLogin() {
		when(service.validateLogin(user)).thenReturn(false);

		ResponseEntity<Boolean> response = controller.validateLogin(user);

		assertResponseEntity(response, 401);
		assertNotNull(response.getBody());
		assertEquals(response.getBody(), false);
	}

	private void assertResponseEntity(ResponseEntity<?> response, int desiredHttpStatusCode){
		assertNotNull(response);

		switch (desiredHttpStatusCode){
			case 200: assertEquals(HttpStatus.OK, response.getStatusCode());
				break;
			case 201: assertEquals(HttpStatus.CREATED, response.getStatusCode());
				break;
			case 204: assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
				break;
			case 500: assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
				break;
		}

		assertEquals(ResponseEntity.class, response.getClass());
	}

	private void assertUserResponse(UserResponse responseUser){
		assertNotNull(responseUser);
		assertEquals(UserResponse.class, responseUser.getClass());
		assertEquals(ID, responseUser.getId());
		assertEquals(EMAIL, responseUser.getEmail());
		assertEquals(FIRST_NAME, responseUser.getFirstName());
		assertEquals(LAST_NAME, responseUser.getLastName());
	}
}
