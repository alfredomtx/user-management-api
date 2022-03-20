package com.user.core.api.controller;

import com.user.core.api.exceptions.InvalidUserDataException;
import com.user.core.api.exceptions.UserAlreadyExistsException;
import com.user.core.api.exceptions.UserNotFoundException;
import com.user.core.api.model.User;
import com.user.core.api.model.dto.UserResponse;
import com.user.core.api.repository.UserRepository;
import com.user.core.api.service.UserService;
import com.user.core.api.service.impl.UserDetailServiceImpl;
import com.user.core.api.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WebMvcTest(UserController.class)
public class UserControllerMvcTest {

	@Autowired
	private MockMvc mockMvc;

	// need to use the real implementation of ModelMapper in UserServiceImpl
	// private final ModelMapper mapper = new ModelMapper();

	@MockBean
	private UserRepository repository;
	@Mock
	private UserServiceImpl servicesImpl;
	@MockBean
	private UserService service;
	@MockBean
	private UserDetailServiceImpl userDetailServiceImpl;

	public static final String API_URL = "/api/user/";
	// User with valid token to login for Spring Security
	public static final Long ID = 1L;
	public static final String EMAIL = "test@test.com";
	public static final String PASSWORD = "test";
	public static final String FIRST_NAME = "John";
	public static final String LAST_NAME = "Snow";

	// exception messages constants
	public static final String USER_NOT_FOUND_BY_ID = "User with id [" + ID + "] not found.";
	public static final String USER_ALREADY_EXISTS_BY_EMAIL = "User with e-mail [" + EMAIL + "] already exists.";

	private User user;
	private UserResponse userResponse;

	@BeforeEach
	void setUp() {
		startUser();
	}

	private void startUser() {

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
	public void shouldReturnListOfUser_WhenGetAll() throws Exception {
		List<UserResponse> userList = new ArrayList<>();
		userList.add(userResponse);

		when(service.getAll()).thenReturn(userList);

		this.mockMvc.perform(get(API_URL)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL))
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()", is(1)))
				.andExpect(jsonPath("$[0].id", is(ID.intValue())))
				.andExpect(jsonPath("$[0].email", is(EMAIL)))
				.andExpect(jsonPath("$[0].firstName", is(FIRST_NAME)))
				.andExpect(jsonPath("$[0].lastName", is(LAST_NAME)))
				.andReturn();
	}

	@Test
	public void shouldReturnUser_WhenGetById() throws Exception {
		when(service.getById(anyLong())).thenReturn(userResponse);

		this.mockMvc.perform(get(API_URL + ID)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL))
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(ID.intValue())))
				.andExpect(jsonPath("$.email", is(EMAIL)))
				.andExpect(jsonPath("$.firstName", is(FIRST_NAME)))
				.andExpect(jsonPath("$.lastName", is(LAST_NAME)));
	}

	@Test
	void shouldThrowUserNotFoundException_WhenGetById() throws Exception {
		when(service.getById(anyLong())).thenThrow(new UserNotFoundException(ID));

		String exceptionExpectedMessage = "User with id [" + ID + "] not found.";
		this.mockMvc.perform(get(API_URL + ID)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL))
				)
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", is(exceptionExpectedMessage)));
	}

	@Test
	void shouldReturnCreatedAndUri_WhenAddUser() throws Exception {
		when(service.add(any())).thenReturn(userResponse);

		String expectedRedirectUrl = "http://localhost" + API_URL + ID;

		this.mockMvc.perform(post(API_URL)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL))
						.contentType(MediaType.APPLICATION_JSON)
						.content(getUserJsonBody())
				)
				.andExpect(status().isCreated())
				.andExpect(redirectedUrl(expectedRedirectUrl))
				.andExpect(header().string("location", expectedRedirectUrl));
	}

	@Test
	void shouldThrowUserAlreadyExistsException_WhenUpdateUser() throws Exception {
		when(service.update(anyLong(), any())).thenThrow(new UserAlreadyExistsException(EMAIL));

		this.mockMvc.perform(put(API_URL + ID)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL))
						.contentType(MediaType.APPLICATION_JSON)
						.content(getUserJsonBody())
				)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error", is(USER_ALREADY_EXISTS_BY_EMAIL)));
	}

	@Test
	void shouldReturnVoid_WhenDeleteUser_WithSuccess() throws Exception {
		doNothing().when(service).delete(anyLong());

		this.mockMvc.perform(delete(API_URL + ID)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL))
				)
				.andExpect(status().isNoContent());
	}

	@Test
	void shouldReturnVoid_WhenUpdatePassword_WithSuccess() throws Exception {
		doNothing().when(service).changePassword(anyLong(), anyString());

		this.mockMvc.perform(post(API_URL + ID + "/changePassword")
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL))
						.contentType(MediaType.APPLICATION_JSON)
						.content("\"password\": \"123\"")
				)
				.andExpect(status().isOk())
				.andExpect(content().string("User password updated with success."));
	}

	@Test
	void shouldThrowUserNotFoundException_WhenDeleteById() throws Exception {
		doThrow(new UserNotFoundException(ID)).when(service).delete(anyLong());

		this.mockMvc.perform(delete(API_URL + ID)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL))
				)
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", is(USER_NOT_FOUND_BY_ID)));
	}

	@Test
	void shouldThrowInvalidUserDataException_WhenUpdatePassword() throws Exception {
		String exceptionExpectedMessage = "Invalid password.";
		doThrow(new InvalidUserDataException(exceptionExpectedMessage)).when(service).changePassword(anyLong(), anyString());

		this.mockMvc.perform(post(API_URL + ID + "/changePassword")
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL))
						.contentType(MediaType.APPLICATION_JSON)
						.content("\"password\": \"123\"")
				)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error", is(exceptionExpectedMessage)));
	}

	@Test
	void shouldReturnTrueAndOk_WhenValidateLogin() throws Exception {
		when(service.validateLogin(any())).thenReturn(true);

		this.mockMvc.perform(post(API_URL + "/validateLogin")
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL))
						.contentType(MediaType.APPLICATION_JSON)
						.content(getUserJsonBody())
				)
				.andExpect(status().isOk())
				.andExpect(content().string("true"));
	}

	@Test
	void shouldReturnFalseAndUnauthorized_WhenValidateLogin() throws Exception {
		when(service.validateLogin(any())).thenReturn(false);

		this.mockMvc.perform(post(API_URL + "/validateLogin")
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL))
						.contentType(MediaType.APPLICATION_JSON)
						.content(getUserJsonBody())
				)
				.andExpect(status().isUnauthorized())
				.andExpect(content().string("false"));
	}

	// TODO test send user with invalid fields

	private String getUserJsonBody() {
		return String.format("{" +
				"\"email\": \"%s\"" +
				", \"password\": \"%s\"" +
				", \"firstName\": \"%s\"" +
				", \"lastName\": \"%s\"" +
				"}", EMAIL, PASSWORD, FIRST_NAME, LAST_NAME);
	}
}