package com.user.api.service.impl;

import com.user.api.exceptions.InvalidUserDataException;
import com.user.api.exceptions.UserAlreadyExistsException;
import com.user.api.exceptions.UserNotFoundException;
import com.user.api.model.User;
import com.user.api.model.dto.UserRequestDTO;
import com.user.api.model.dto.UserResponseDTO;
import com.user.api.repository.UserRepository;
import com.user.api.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.Validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

	// use the real implementation of ModelMapper in UserServiceImpl
	@Autowired
	private final ModelMapper mapper = new ModelMapper();
	@Autowired
	private final Validator validator = mock(Validator.class);
	@MockBean
	private final UserRepository repository = mock(UserRepository.class);

	// overriding PasswordEncoder methods to be able to use to validate login
	@Autowired
	private final PasswordEncoder passwordEncoder = new PasswordEncoder() {
		@Override
		public String encode(CharSequence rawPassword) {
			return rawPassword.toString();
		}
		@Override
		public boolean matches(CharSequence rawPassword, String encodedPassword) {
			return rawPassword.equals(encodedPassword);
		}
	};

	private final UserServiceImpl service = new UserServiceImpl(passwordEncoder, repository, mapper, validator);

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
		MockitoAnnotations.openMocks(this);
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
		userResponseDTO.setPassword(PASSWORD);
		userResponseDTO.setFirstName(FIRST_NAME);
		userResponseDTO.setLastName(LAST_NAME);
	}

	@Test
	void shouldReturnListOfUser_WhenGetAll() {
		when(repository.findAll()).thenReturn(List.of(user));

		List<UserResponseDTO> response = service.getAll();
		assertNotNull(response);
		// list should return only 1 user
		assertEquals(1, response.size());
		// do the rest of validations for the first user of the list
		assertDtoResponse(response.get(0));
	}

	@Test
	void shouldReturnUser_WhenGetById() {
		when(repository.findById(anyLong())).thenReturn(Optional.of(user));
		UserResponseDTO response = service.getById(ID);
		assertDtoResponse(response);
	}

	@Test
	void shouldThrowUserNotFoundException_WhenGetById() {
		when(repository.findById(anyLong())).thenThrow(new UserNotFoundException(ID));

		String exceptionExpectedMessage = "User with id [" + ID + "] not found.";
		try {
			service.getById(ID);
		} catch (Exception e) {
			assertEquals(UserNotFoundException.class, e.getClass());
			assertEquals(exceptionExpectedMessage, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}

	@Test
	void shouldThrowUserNotFoundException_WhenGetByEmail() {
		when(repository.findByEmail(anyString())).thenThrow(new UserNotFoundException(EMAIL));

		String exceptionExpectedMessage = "User with email [" + EMAIL + "] not found.";
		try {
			service.getByEmail(EMAIL);
		} catch (Exception e) {
			assertEquals(UserNotFoundException.class, e.getClass());
			assertEquals(exceptionExpectedMessage, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}

	@Test
	void shouldReturnUser_WhenGetByEmail() {
		when(repository.findByEmail(anyString())).thenReturn(Optional.of(user));
		UserResponseDTO response = service.getByEmail(EMAIL);
		assertDtoResponse(response);
	}

	@Test
	void shouldReturnUser_WhenAddUser() {
		UserRequestDTO userInsert = mapper.map(user, UserRequestDTO.class);
		when(repository.save(any())).thenReturn(user);
		UserResponseDTO response = service.add(userInsert);
		assertDtoResponse(response);
	}

	@Test
	void shouldReturnUserAlreadyExistsException_WhenAddUser() {
		UserRequestDTO userInsert = new UserRequestDTO();
		userInsert.setEmail(EMAIL);

		when(repository.findByEmail(anyString())).thenReturn(Optional.of(user));
		try {
			service.add(userInsert);
		} catch (Exception e) {
			assertEquals(UserAlreadyExistsException.class, e.getClass());
			assertEquals(USER_ALREADY_EXISTS_BY_EMAIL, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}

	@Test
	void shouldReturnUser_WhenUpdateUser_WithSuccess() {
		Map<String, Object> userUpdate = getUserRequestObject();
		String newEmail = "testupdate@test.com";
		userUpdate.put("email", newEmail);

		when(repository.findById(anyLong())).thenReturn(Optional.of(user));
		when(repository.save(any())).thenReturn(user);

		UserResponseDTO response = service.update(ID, userUpdate);

		assertNotNull(response);
		assertEquals(UserResponseDTO.class, response.getClass());
		assertEquals(ID, response.getId());
		// email should be different from default
		assertEquals(newEmail, response.getEmail());
		assertEquals(FIRST_NAME, response.getFirstName());
		assertEquals(LAST_NAME, response.getLastName());
	}

	@Test
	void shouldThrowUserAlreadyExistsException_WhenUpdateUser_WithEmailAlreadyInUse() {
		Map<String, Object> userRequest = getUserRequestObject();
		String newEmail = "new_email@test.com";
		userRequest.put("email", newEmail);

		// creating another user with the same e-mail the current user want to update
		User anotherExistingUser = mapper.map(userRequest, User.class);
		anotherExistingUser.setEmail(newEmail);
		anotherExistingUser.setId(ID + 1);

		// new email must be different of the current one
		assertNotEquals(userRequest.get("email"), user.getEmail());
		// ensure the new email is the same email of another user
		assertEquals(userRequest.get("email"), anotherExistingUser.getEmail());

		when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
		when(repository.findByEmail(newEmail)).thenReturn(Optional.of(anotherExistingUser));
		when(repository.findById(anyLong())).thenReturn(Optional.of(user));

		String exceptionExpectedMessage = "User with e-mail [" + newEmail + "] already exists.";
		try {
			service.update(ID, userRequest);
		} catch (Exception e) {
			assertEquals(UserAlreadyExistsException.class, e.getClass());
			assertEquals(exceptionExpectedMessage, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}

	@Test
	void shouldReturnVoid_WhenDeleteUser_WithSuccess() {
		when(repository.findById(anyLong())).thenReturn(Optional.of(user));
		doNothing().when(repository).deleteById(anyLong());
		service.delete(ID);
		verify(repository, times(1)).deleteById(anyLong());
	}

	@Test
	void shouldThrowUserNotFoundException_WhenDeleteById() {
		when(repository.findById(anyLong())).thenThrow(new UserNotFoundException(ID));
		try {
			service.getById(ID);
		} catch (Exception e) {
			assertEquals(UserNotFoundException.class, e.getClass());
			assertEquals(USER_NOT_FOUND_BY_ID, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}

	@Test
	void shouldReturnVoid_WhenUpdatePassword_WithSuccess() {
		when(repository.findById(anyLong())).thenReturn(Optional.of(user));
		when(repository.save(any())).thenReturn(user);

		service.changePassword(ID, "123test");
		verify(repository, times(1)).save(any());
	}

	@Test
	void shouldThrowInvalidUserDataException_WhenUpdatePassword_WithPasswordBlank() {
		String newPassword = "";
		user.setPassword(newPassword);

		String exceptionExpectedMessage = "Password is blank.";
		try {
			service.changePassword(ID, newPassword);
		} catch (Exception e) {
			assertEquals(InvalidUserDataException.class, e.getClass());
			assertEquals(exceptionExpectedMessage, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}

	@Test
	void shouldThrowInvalidUserDataException_WhenUpdatePassword_WithPasswordTooShort() {
		String newPassword = "a";
		user.setPassword(newPassword);

		assertEquals(newPassword.length(), 1);

		String exceptionExpectedMessage = "Password is too short, less than 4 characters.";
		try {
			service.changePassword(ID, newPassword);
		} catch (Exception e) {
			assertEquals(InvalidUserDataException.class, e.getClass());
			assertEquals(exceptionExpectedMessage, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}

	@Test
	void shouldThrowInvalidUserDataException_WhenUpdatePassword_WithPasswordTooLong() {
		String newPassword = "StringWithMoreThan255Characters_____________________________________________" +
				"_________________________________________________________________________________________" +
				"___________________________________________________________________________________________";
		user.setPassword(newPassword);
		assertEquals(newPassword.length(), 256);

		String exceptionExpectedMessage = "Password is too big, more than 255 characters.";
		try {
			service.changePassword(ID, newPassword);
		} catch (Exception e) {
			assertEquals(InvalidUserDataException.class, e.getClass());
			assertEquals(exceptionExpectedMessage, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}

	@Test
	void shouldReturnTrue_WhenValidateLogin() {
		when(repository.findByEmail(anyString())).thenReturn(Optional.of(user));
		boolean response = service.validateLogin(user);
		assertTrue(response);
	}

	@Test
	void shouldReturnFalse_WhenValidateLogin_WithWrongPassword() {
		User userWithWrongPass = new User();
		userWithWrongPass.setEmail(EMAIL);
		userWithWrongPass.setPassword("abc");

		assertNotEquals(userWithWrongPass.getPassword(), user.getPassword());

		when(repository.findByEmail(anyString())).thenReturn(Optional.of(user));
		boolean response = service.validateLogin(userWithWrongPass);
		assertFalse(response);
	}

	@Test
	void shouldReturnFalse_WhenValidateLogin_WhenUserDoesNotExist() {
		when(repository.findByEmail(anyString())).thenReturn(Optional.empty());
		boolean response = service.validateLogin(user);
		assertFalse(response);
	}

	private void assertDtoResponse(UserResponseDTO responseUser) {
		assertNotNull(responseUser);
		assertEquals(UserResponseDTO.class, responseUser.getClass());
		assertEquals(ID, responseUser.getId());
		assertEquals(EMAIL, responseUser.getEmail());
		assertEquals(FIRST_NAME, responseUser.getFirstName());
		assertEquals(LAST_NAME, responseUser.getLastName());
	}

	private void failedExceptionNotThrown() {
		fail("Should reach this line, exception was not thrown");
	}

	private Map<String, Object> getUserRequestObject() {
		Map<String, Object> userRequest = new HashMap<>();
		userRequest.put("email", EMAIL);
		userRequest.put("password", PASSWORD);
		userRequest.put("firstName", FIRST_NAME);
		userRequest.put("lastName", LAST_NAME);
		return userRequest;
	}
}
