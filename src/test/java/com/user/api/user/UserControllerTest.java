package com.user.api.user;

import com.user.api.email.EmailRepository;
import com.user.api.exceptions.ObjectFieldsValidationException;
import com.user.api.exceptions.UserAlreadyExistsException;
import com.user.api.exceptions.UserNotFoundException;
import com.user.api.security.UserDetailService;
import com.user.api.user.model.User;
import com.user.api.user.model.UserResponseDTO;
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
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WebMvcTest(UserController.class)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	// need to use the real implementation of ModelMapper in UserService
	// private final ModelMapper mapper = new ModelMapper();

	@Mock
	private UserService servicesImpl;
	@MockBean
	private UserRepository repository;
	@MockBean
	private EmailRepository emailRepository;
	@MockBean
	private UserService service;
	@MockBean
	private UserDetailService userDetailService; // for Spring Security

	public static final String API_URL = "/api/user/";
	public static final Long ID = 1L;
	public static final String EMAIL = "test@test.com";
	public static final String PASSWORD = "test";
	public static final String FIRST_NAME = "John";
	public static final String LAST_NAME = "Snow";

	// exception messages constants
	public static final String USER_NOT_FOUND_BY_ID = "User with id [" + ID + "] not found.";
	public static final String USER_ALREADY_EXISTS_BY_EMAIL = "User with e-mail [" + EMAIL + "] already exists.";

	private User user;
	private UserResponseDTO userResponseDTO;

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

		userResponseDTO = new UserResponseDTO();
		userResponseDTO.setId(ID);
		userResponseDTO.setEmail(EMAIL);
		userResponseDTO.setFirstName(FIRST_NAME);
		userResponseDTO.setLastName(LAST_NAME);
	}

	@Test
	public void shouldReturnListOfUser_WhenGetAll() throws Exception {
		List<UserResponseDTO> userList = new ArrayList<>();
		userList.add(userResponseDTO);

		//when(service.getAll()).thenReturn(userList);

		this.mockMvc.perform(get(API_URL)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL))
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()", is(1)))
				.andExpect(jsonPath("$[0].id", is(ID.intValue())))
				.andExpect(jsonPath("$[0].email", is(EMAIL)))
				.andExpect(jsonPath("$[0].firstName", is(FIRST_NAME)))
				.andExpect(jsonPath("$[0].lastName", is(LAST_NAME)))
				.andExpect(jsonPath("$[0].password").doesNotExist())
				.andReturn();
	}

	@Test
	public void shouldReturnUser_WhenGetById() throws Exception {
		when(service.getById(anyLong())).thenReturn(userResponseDTO);

		this.mockMvc.perform(get(API_URL + ID)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL))
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(ID.intValue())))
				.andExpect(jsonPath("$.email", is(EMAIL)))
				.andExpect(jsonPath("$.firstName", is(FIRST_NAME)))
				.andExpect(jsonPath("$.lastName", is(LAST_NAME)))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andReturn();

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
		when(service.add(any())).thenReturn(userResponseDTO);

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
		when(service.update(any())).thenThrow(new UserAlreadyExistsException(EMAIL));

		this.mockMvc.perform(patch(API_URL)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL))
						.contentType(MediaType.APPLICATION_JSON)
						.content(getUserJsonBody())
				)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error", is(USER_ALREADY_EXISTS_BY_EMAIL)));
	}

	@Test
	void shouldThrowUserFieldsValidationException_WhenUpdateUser() throws Exception {
		List<FieldError> errors = new ArrayList<>();
		String errorMessage = "invalid email";
		FieldError error = new FieldError("user", "email", errorMessage);
		errors.add(error);

		when(service.update(any())).thenAnswer(invocation -> {
			throw new ObjectFieldsValidationException(errors);
		});

		this.mockMvc.perform(patch(API_URL)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL))
						.contentType(MediaType.APPLICATION_JSON)
						.content(getUserJsonBody())
				)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", hasSize(1)))
				.andExpect(jsonPath("$.message[0].email", is(errorMessage)));
	}

	@Test
	void shouldReturnVoid_WhenDeleteUser_WithSuccess() throws Exception {
		doNothing().when(service).delete(anyLong());

		this.mockMvc.perform(delete(API_URL + ID)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL))
				)
				.andExpect(status().isNoContent());
	}

	/*@Test
	void shouldReturnVoid_WhenUpdatePassword_WithSuccess() throws Exception {
		doNothing().when(service).changePassword(anyLong(), anyString());

		this.mockMvc.perform(post(API_URL + ID + "/changePassword")
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL))
						.contentType(MediaType.APPLICATION_JSON)
						.content("\"password\": \"123\"")
				)
				.andExpect(status().isOk())
				.andExpect(content().string("User password updated with success."));
	}*/

	@Test
	void shouldThrowUserNotFoundException_WhenDeleteById() throws Exception {
		doThrow(new UserNotFoundException(ID)).when(service).delete(anyLong());

		this.mockMvc.perform(delete(API_URL + ID)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL))
				)
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", is(USER_NOT_FOUND_BY_ID)));
	}

	/*@Test
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
	}*/

	/*
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
	*/

	// TODO test send user with invalid fields

	private String getUserJsonBody() {
		return String.format("{" +
				"\"email\": \"%s\"" +
				", \"password\": \"%s\"" +
				", \"firstName\": \"%s\"" +
				", \"lastName\": \"%s\"" +
				", \"anInvalidField\": \"%s\"" +
				"}", EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, "this is an invalid field");
	}
}