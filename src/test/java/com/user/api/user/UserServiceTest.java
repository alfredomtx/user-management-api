package com.user.api.user;

import com.user.api.email.EmailService;
import com.user.api.exceptions.InvalidUserDataException;
import com.user.api.exceptions.UserAlreadyExistsException;
import com.user.api.exceptions.UserNotFoundException;
import com.user.api.resgistration.RegistrationService;
import com.user.api.user.model.User;
import com.user.api.user.model.UserRequestDTO;
import com.user.api.user.model.UserResponseDTO;
import com.user.api.user.util.UserUtil;
import com.user.api.userProperties.UserPropertiesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.Validator;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

	// use the real implementation of ModelMapper in UserService
	@Autowired
	private final ModelMapper mapper = new ModelMapper();
	@Autowired
	private final Validator validator = mock(Validator.class);
	@MockBean
	private final UserRepository repository = mock(UserRepository.class);
	@MockBean
	private final UserPropertiesService userPropsService = mock(UserPropertiesService.class);
	@MockBean
	private final RegistrationService registrationService = mock(RegistrationService.class);
	@MockBean
	private final EmailService emailService = mock(EmailService.class);
	@MockBean
	private final UserUtil userUtil = mock(UserUtil.class);

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

	@Autowired
	private final UserService service = new UserService(passwordEncoder,
			repository,
			userPropsService,
			registrationService,
			mapper,
			validator,
			emailService,
			userUtil
		);

	public static final Long ID = 1L;
	public static final String EMAIL = "test@test.com";
	public static final String PASSWORD = "test";
	public static final String FIRST_NAME = "John";
	public static final String LAST_NAME = "Snow";

	// exception messages constants
	public static final String USER_NOT_FOUND_BY_ID = "User with id [" + ID + "] not found.";
	public static final String USER_NOT_FOUND_BY_EMAIL = "User with email [" + EMAIL + "] not found.";
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
		userResponseDTO.setFirstName(FIRST_NAME);
		userResponseDTO.setLastName(LAST_NAME);
	}

	@Test
	void shouldReturnPageOfUser_When_getAll() {
		List<User> userList = new ArrayList<>();
		userList.add(user);
		Page<User> userListPage = new PageImpl(userList);

		Mockito.when(repository.findAll(any(Pageable.class))).thenReturn(userListPage);

		Page<UserResponseDTO> response = service.getAll(Mockito.mock(Pageable.class));
		assertNotNull(response);
		assertEquals(1, response.getSize());
		// list should return only 1 user
		assertEquals(1, response.getContent().size());
		assertEquals(1, response.getTotalPages());
		// do the rest of validations for the first user of the list
		assertUserDtoResponse(response.getContent().get(0));
	}

	@Test
	void shouldReturnUser_When_getById() {
		when(repository.findById(anyLong())).thenReturn(Optional.of(user));
		UserResponseDTO response = service.getById(ID);
		assertUserDtoResponse(response);
	}

	@Test
	void shouldThrowUserNotFoundException_When_getById() {
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
	void shouldReturnUser_When_getByEmail() {
		when(repository.findByEmail(anyString())).thenReturn(Optional.of(user));
		UserResponseDTO response = service.getByEmail(EMAIL);
		assertUserDtoResponse(response);
	}

	@Test
	void shouldThrowUserNotFoundException_When_getByEmail() {
		when(repository.findByEmail(anyString())).thenThrow(new UserNotFoundException(EMAIL));

		try {
			service.getByEmail(EMAIL);
		} catch (Exception e) {
			assertEquals(UserNotFoundException.class, e.getClass());
			assertEquals(USER_NOT_FOUND_BY_EMAIL, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}

	@Test
	void shouldReturnUser_When_UsingEmail_getByIdOrEmail() {
		when(repository.findByEmail(anyString())).thenReturn(Optional.of(user));
		when(userUtil.getUserObjectByIdOrEmailFromFields(anyMap())).thenReturn(user);

		Map<String, String> fields = new HashMap<>();
		fields.put("email", EMAIL);
		UserResponseDTO response = service.getByIdOrEmail(fields);
		assertUserDtoResponse(response);
	}

	@Test
	void shouldThrowUserNotFoundException_When_UsingEmail_getByIdOrEmail() {
		when(userUtil.getUserObjectByIdOrEmailFromFields(anyMap())).thenThrow(new UserNotFoundException(EMAIL));

		Map<String, String> fields = new HashMap<>();
		fields.put("email", EMAIL);
		try {
			service.getByIdOrEmail(fields);
		} catch (Exception e) {
			assertEquals(UserNotFoundException.class, e.getClass());
			assertEquals(USER_NOT_FOUND_BY_EMAIL, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}

	@Test
	void shouldThrowInvalidUserDataException_When_getByIdOrEmail() {
		String exceptionExpectedMessage = "[email] or [id] field must be set.";

		when(userUtil.getUserObjectByIdOrEmailFromFields(anyMap())).thenThrow(new InvalidUserDataException(exceptionExpectedMessage));
		try {
			service.getByIdOrEmail(new HashMap<String, String>());
		} catch (Exception e) {
			assertEquals(InvalidUserDataException.class, e.getClass());
			assertEquals(exceptionExpectedMessage, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}

	@Test
	void shouldReturnUser_When_add() {
		UserRequestDTO userInsert = mapper.map(user, UserRequestDTO.class);
		when(repository.save(any())).thenReturn(user);
		UserResponseDTO response = service.add(userInsert);
		assertUserDtoResponse(response);
	}

	@Test
	void shouldReturnUserAlreadyExistsException_When_add() {
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

	/*@Test
	void shouldReturnUser_WhenUpdateUser_WithSuccess() {
		Map<String, String> userUpdate = getUserRequestObject();
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
	}*/

	/*@Test
	void shouldThrowUserAlreadyExistsException_WhenUpdateUser_WithEmailAlreadyInUse() {
		Map<String, String> userRequest = getUserRequestObject();
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
	}*/

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

	/*@Test
	void shouldReturnVoid_WhenUpdatePassword_WithSuccess() {
		when(repository.findById(anyLong())).thenReturn(Optional.of(user));
		when(repository.save(any())).thenReturn(user);

		service.changePassword(ID, "123test");
		verify(repository, times(1)).save(any());
	}*/

	/*@Test
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
	}*/

	/*@Test
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
	}*/

	/*@Test
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
	}*/

	/*
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
	*/

	private void assertUserDtoResponse(UserResponseDTO responseUser) {
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

	private Map<String, String> getUserRequestObject() {
		Map<String, String> userRequest = new HashMap<>();
		userRequest.put("email", EMAIL);
		userRequest.put("password", PASSWORD);
		userRequest.put("firstName", FIRST_NAME);
		userRequest.put("lastName", LAST_NAME);
		return userRequest;
	}
}
