package com.user.api.user;

import com.user.api.email.EmailRepository;
import com.user.api.exceptions.InvalidUserDataException;
import com.user.api.exceptions.UserAlreadyExistsException;
import com.user.api.exceptions.UserNotFoundException;
import com.user.api.security.UserDetailService;
import com.user.api.user.model.User;
import com.user.api.user.model.UserResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WebMvcTest(UserController.class)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private UserService service;
	@MockBean
	private UserRepository repository;
	@MockBean
	private EmailRepository emailRepository;
	@MockBean
	private UserDetailService userDetailService; // for Spring Security


	public static final String API_URL = "/api/user";

	public static final Long ID = 1L;
	public static final String EMAIL = "test@test.com";
	public static final String PASSWORD = "test";
	public static final String FIRST_NAME = "John";
	public static final String LAST_NAME = "Snow";

	// exception messages constants
	public static final String USER_NOT_FOUND_BY_ID = "User with id [" + ID + "] not found.";
	public static final String USER_NOT_FOUND_BY_EMAIL = "User with email [" + EMAIL + "] not found.";
	public static final String USER_ALREADY_EXISTS_BY_EMAIL = "User with e-mail [" + EMAIL + "] already exists.";

	private static final String EMAIL_JSON = String.format("{\"email\": \"%s\"}", EMAIL);

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
	public void shouldReturnPageOfUser_When_getAll() throws Exception {
		List<UserResponseDTO> userList = new ArrayList<>();
		userList.add(userResponseDTO);
		Page<UserResponseDTO> userListPage = new PageImpl(userList);

		when(service.getAll(any())).thenReturn(userListPage);

		this.mockMvc.perform(get(API_URL + "/all")
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("ADMIN"))
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.size()", is(1)))
				.andExpect(jsonPath("$.content.[0].id", is(ID.intValue())))
				.andExpect(jsonPath("$.content.[0].email", is(EMAIL)))
				.andExpect(jsonPath("$.content.[0].firstName", is(FIRST_NAME)))
				.andExpect(jsonPath("$.content.[0].lastName", is(LAST_NAME)))
				.andExpect(jsonPath("$.content.[0].password").doesNotExist())
				.andReturn();
	}

	@Test
	public void shouldReturnUser_When_getById() throws Exception {
		when(service.getById(anyLong())).thenReturn(userResponseDTO);

		this.mockMvc.perform(get(API_URL + "/" + ID)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("ADMIN"))
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
	void shouldThrowUserNotFoundException_When_getById() throws Exception {
		when(service.getById(anyLong())).thenThrow(new UserNotFoundException(ID));

		String exceptionExpectedMessage = "User with id [" + ID + "] not found.";
		this.mockMvc.perform(get(API_URL + "/" + ID)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("ADMIN"))
				)
				//.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code", is(404)))
				.andExpect(jsonPath("$.message", is(exceptionExpectedMessage)));
	}

	@Test
	public void shouldReturnUser_When_UsingEmail_getByIdOrEmail() throws Exception {
		when(service.getByIdOrEmail(any())).thenReturn(userResponseDTO);

		this.mockMvc.perform(get(API_URL)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("ADMIN"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(EMAIL_JSON)
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
	void shouldThrowUserNotFoundException_When_UsingEmail_getByIdOrEmail() throws Exception {
		when(service.getByIdOrEmail(any())).thenThrow(new UserNotFoundException(EMAIL));

		String exceptionExpectedMessage = "User with email [" + EMAIL + "] not found.";
		this.mockMvc.perform(get(API_URL)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("ADMIN"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(EMAIL_JSON)
				)
				//.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message", is(exceptionExpectedMessage)));
	}

	@Test
	void shouldReturnCreatedAndUri_When_add() throws Exception {
		when(service.add(any())).thenReturn(userResponseDTO);

		String expectedRedirectUrl = "http://localhost" + API_URL + "/" + ID;
		this.mockMvc.perform(post(API_URL)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("ADMIN"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(getUserJsonBody())
				)
				.andExpect(status().isCreated())
				.andExpect(redirectedUrl(expectedRedirectUrl))
				.andExpect(header().string("location", expectedRedirectUrl));
	}

	@Test
	void shouldThrowUserAlreadyExistsException_When_add() throws Exception {
		when(service.add(any())).thenThrow(new UserAlreadyExistsException(EMAIL));

		this.mockMvc.perform(post(API_URL)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("ADMIN"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(getUserJsonBody())
				)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message", is(USER_ALREADY_EXISTS_BY_EMAIL)));
	}

	@Test
	void shouldReturnOK_When_UsingId_update() throws Exception {
		when(service.update(any())).thenReturn(userResponseDTO);

		this.mockMvc.perform(patch(API_URL + "/" + ID)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("ADMIN"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(getUserJsonBody())
				)
				.andExpect(status().isOk());
	}

	// TODO make this work, currently it's failing when throwing the ObjectFieldsValidationException
	/*@Test
	void shouldThrowUserFieldsValidationException_When_UsingId_update() throws Exception {
		List<FieldError> errors = new ArrayList<>();
		String errorMessage = "invalid email";
		FieldError error = new FieldError("user", "email", errorMessage);
		errors.add(error);

		when(service.update(any())).thenAnswer(invocation -> {
			throw new ObjectFieldsValidationException(errors);
		});

		this.mockMvc.perform(patch(API_URL + "/" + ID)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("ADMIN"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(getUserJsonBody())
				)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", hasSize(1)))
				.andExpect(jsonPath("$.message[0].email", is(errorMessage)));
	}*/

	@Test
	void shouldReturnOK_When_UsingEmail_updateByEmailOrId() throws Exception {
		when(service.update(any())).thenReturn(userResponseDTO);

		this.mockMvc.perform(patch(API_URL)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("ADMIN"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(getUserJsonBody())
				)
				.andExpect(status().isOk());
	}

	@Test
	void shouldReturnVoid_When_UsingId_delete() throws Exception {
		doNothing().when(service).delete(anyLong());

		this.mockMvc.perform(delete(API_URL + "/" + ID)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("ADMIN"))
				)
				.andExpect(status().isNoContent());
	}

	@Test
	void shouldThrowUserNotFoundException_When_UsingId_delete() throws Exception {
		doThrow(new UserNotFoundException(ID)).when(service).delete(anyLong());

		this.mockMvc.perform(delete(API_URL + "/" + ID)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("ADMIN"))
				)
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message", is(USER_NOT_FOUND_BY_ID)));
	}

	@Test
	void shouldReturnVoid_When_UsingEmail_deleteByIdOrEmail() throws Exception {
		doNothing().when(service).delete(anyMap());

		this.mockMvc.perform(delete(API_URL)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("ADMIN"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(EMAIL_JSON)
				)
				.andExpect(status().isNoContent());
	}

	@Test
	void shouldThrowUserNotFoundException_When_UsingEmail_deleteByIdOrEmail() throws Exception {
		doThrow(new UserNotFoundException(EMAIL)).when(service).delete(anyMap());

		this.mockMvc.perform(delete(API_URL)
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("ADMIN"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(EMAIL_JSON)
				)
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message", is(USER_NOT_FOUND_BY_EMAIL)));
	}

	@Test
	void shouldReturnVoid_When_changePassword() throws Exception {
		doNothing().when(service).changeCurrentPassword(anyMap());

		this.mockMvc.perform(post(API_URL + "/changePassword")
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("ADMIN"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(EMAIL_JSON)
				)
				.andExpect(status().isOk())
				.andExpect(content().string("User password changed with success."));
	}

	@Test
	void shouldThrowInvalidUserDataException_changePassword() throws Exception {
		String exceptionExpectedMessage = "Invalid password.";
		doThrow(new InvalidUserDataException(exceptionExpectedMessage)).when(service).changeCurrentPassword(anyMap());

		this.mockMvc.perform(post(API_URL + "/changePassword")
						.with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("ADMIN"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(EMAIL_JSON)
				)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is(exceptionExpectedMessage)));
	}

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