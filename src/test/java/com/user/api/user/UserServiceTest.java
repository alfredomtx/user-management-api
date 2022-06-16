package com.user.api.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import com.user.api.email.EmailService;
import com.user.api.exceptions.InvalidUserDataException;
import com.user.api.exceptions.UserAlreadyExistsException;
import com.user.api.exceptions.UserNotFoundException;
import com.user.api.registration.RegistrationService;
import com.user.api.user.model.User;
import com.user.api.user.model.UserRequestDTO;
import com.user.api.user.model.UserResponseDTO;
import com.user.api.user.util.UserUtil;
import com.user.api.userProperties.UserPropertiesService;


/*
 * Test functions naming convention being used:
 * should[Action][What]_When_[WithSomething(optional)]_[functionName]
 *
 * @Examples:
 * - shouldReturnUser_When_getById
 * - shouldReturnUser_When_UsingEmail_getByIdOrEmail
 * - shouldThrowUserNotFoundException_When_getById
 * */

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
			service.getByIdOrEmail(new HashMap<>());
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

	@Test
	void shouldReturnUser_When_update() {
		when(userUtil.getUserObjectByIdOrEmailFromFields(anyMap())).thenReturn(user);
		when(repository.save(any())).thenReturn(user);

		UserResponseDTO response = service.update(getUserRequestObject());
		assertUserDtoResponse(response);
	}

	@Test
	void shouldThrowUserNotFoundException_When_update() {
		when(userUtil.getUserObjectByIdOrEmailFromFields(anyMap())).thenThrow(new UserNotFoundException(EMAIL));

		try {
			service.update(getUserRequestObject());
		} catch (Exception e) {
			assertEquals(UserNotFoundException.class, e.getClass());
			assertEquals(USER_NOT_FOUND_BY_EMAIL, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}

	// TODO implement test for used with invalid fields
	// currently it's not working probably because of BindingResult class being mocked
	/*
	@Test
	void shouldThrowObjectFieldsValidationException_When_update() {
		when(userUtil.getUserObjectByIdOrEmailFromFields(anyMap())).thenReturn(user);
		when(repository.save(any())).thenReturn(user);

		Map<String, String> userRequest = new HashMap<>();
		userRequest.put("password", null);

		try {
			service.update(userRequest);
		} catch (Exception e) {
			assertEquals(ObjectFieldsValidationException.class, e.getClass());
			// assertEquals(USER_NOT_FOUND_BY_EMAIL, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}
	*/

	@Test
	void shouldReturnVoid_When_UsingId_delete() {
		when(repository.findById(anyLong())).thenReturn(Optional.of(user));
		doNothing().when(repository).delete(any());

		service.delete(ID);
		verify(repository, times(1)).delete(any());
	}

	@Test
	void shouldReturnVoid_When_UsingEmail_delete() {
		when(repository.findByEmail(anyString())).thenReturn(Optional.of(user));
		doNothing().when(repository).delete(any());

		service.delete(EMAIL);
		verify(repository, times(1)).delete(any());
	}

	@Test
	void shouldReturnVoid_When_UsingEmailOrId_delete() {
		when(userUtil.getUserObjectByIdOrEmailFromFields(anyMap())).thenReturn(user);
		doNothing().when(repository).delete(any());

		service.delete(getUserRequestObject());
		verify(repository, times(1)).delete(any());
	}

	@Test
	void shouldThrowUserNotFoundException_When_UsingId_delete() {
		when(repository.findById(anyLong())).thenThrow(new UserNotFoundException(ID));
		try {
			service.delete(ID);
		} catch (Exception e) {
			assertEquals(UserNotFoundException.class, e.getClass());
			assertEquals(USER_NOT_FOUND_BY_ID, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}

	@Test
	void shouldThrowUserNotFoundException_When_UsingEmail_delete() {
		when(repository.findByEmail(anyString())).thenThrow(new UserNotFoundException(EMAIL));
		try {
			service.delete(EMAIL);
		} catch (Exception e) {
			assertEquals(UserNotFoundException.class, e.getClass());
			assertEquals(USER_NOT_FOUND_BY_EMAIL, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}

	@Test
	void shouldThrowUserNotFoundException_When_UsingEmailOrId_delete() {
		when(userUtil.getUserObjectByIdOrEmailFromFields(anyMap())).thenThrow(new UserNotFoundException(EMAIL));
		try {
			service.delete(getUserRequestObject());
		} catch (Exception e) {
			assertEquals(UserNotFoundException.class, e.getClass());
			assertEquals(USER_NOT_FOUND_BY_EMAIL, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}

	@Test
	void shouldReturnVoid_When_changeCurrentPassword() {
		when(userUtil.getUserObjectByIdOrEmailFromFields(anyMap())).thenReturn(user);
		when(repository.save(any())).thenReturn(user);

		Map<String, String> fields = new HashMap<>();
		fields.put("email", EMAIL);
		String newPassword = "12345";
		fields.put("newPassword", newPassword);
		fields.put("newPasswordConfirmation", newPassword);
		fields.put("currentPassword", PASSWORD);

		service.changeCurrentPassword(fields);
		verify(repository, times(1)).save(any());
	}

	@Test
	void shouldThrowInvalidUserDataException_When_WithCurrentPasswordNotSet_changeCurrentPassword() {
		String exceptionExpectedMessage = "[currentPassword] field not set.";
		try {
			service.changeCurrentPassword(new HashMap<>());
		} catch (Exception e) {
			assertEquals(InvalidUserDataException.class, e.getClass());
			assertEquals(exceptionExpectedMessage, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}

	@Test
	void shouldThrowInvalidUserDataException_When_WithNewPasswordNotSet_changeCurrentPassword() {
		Map<String, String> fields = new HashMap<>();
		fields.put("currentPassword", "");

		String exceptionExpectedMessage = "[newPassword] field not set.";
		try {
			service.changeCurrentPassword(fields);
		} catch (Exception e) {
			assertEquals(InvalidUserDataException.class, e.getClass());
			assertEquals(exceptionExpectedMessage, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}

	@Test
	void shouldThrowInvalidUserDataException_When_WithNewPasswordConfirmationNotSet_changeCurrentPassword() {
		Map<String, String> fields = new HashMap<>();
		fields.put("currentPassword", "");
		fields.put("newPassword", "");

		String exceptionExpectedMessage = "[newPasswordConfirmation] field not set.";
		try {
			service.changeCurrentPassword(fields);
		} catch (Exception e) {
			assertEquals(InvalidUserDataException.class, e.getClass());
			assertEquals(exceptionExpectedMessage, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}

	@Test
	void shouldThrowInvalidUserDataException_When_WithNewPasswordConfirmationNotMatch_changeCurrentPassword() {
		Map<String, String> fields = new HashMap<>();
		fields.put("currentPassword", "");
		fields.put("newPassword", "123");
		fields.put("newPasswordConfirmation", "321");

		String exceptionExpectedMessage = "new password confirmation does not match.";
		try {
			service.changeCurrentPassword(fields);
		} catch (Exception e) {
			assertEquals(InvalidUserDataException.class, e.getClass());
			assertEquals(exceptionExpectedMessage, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}

	@Test
	void shouldThrowInvalidUserDataException_When_WithPasswordBlank_changeCurrentPassword() {
		Map<String, String> fields = new HashMap<>();
		fields.put("currentPassword", "");
		fields.put("newPassword", " ");
		fields.put("newPasswordConfirmation", " ");

		String exceptionExpectedMessage = "Password is blank.";
		try {
			service.changeCurrentPassword(fields);
		} catch (Exception e) {
			assertEquals(InvalidUserDataException.class, e.getClass());
			assertEquals(exceptionExpectedMessage, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}

	@Test
	void shouldThrowInvalidUserDataException_When_WithPasswordTooShort_changeCurrentPassword() {
		Map<String, String> fields = new HashMap<>();
		fields.put("currentPassword", "");
		fields.put("newPassword", "123");
		fields.put("newPasswordConfirmation", "123");

		String exceptionExpectedMessage = "Password is too short, less than 4 characters.";
		try {
			service.changeCurrentPassword(fields);
		} catch (Exception e) {
			assertEquals(InvalidUserDataException.class, e.getClass());
			assertEquals(exceptionExpectedMessage, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}


	@Test
	void shouldThrowInvalidUserDataException_When_WithPasswordTooLong_changeCurrentPassword() {
		String newPassword = "StringWithMoreThan255Characters_____________________________________________" +
				"_________________________________________________________________________________________" +
				"___________________________________________________________________________________________";
		Map<String, String> fields = new HashMap<>();
		fields.put("currentPassword", "");
		fields.put("newPassword", newPassword);
		fields.put("newPasswordConfirmation", newPassword);

		assertEquals(newPassword.length(), 256);

		String exceptionExpectedMessage = "Password is too long, more than 255 characters.";
		try {
			service.changeCurrentPassword(fields);
		} catch (Exception e) {
			assertEquals(InvalidUserDataException.class, e.getClass());
			assertEquals(exceptionExpectedMessage, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}

	@Test
	void shouldThrowInvalidUserDataException_When_WithWrongCurrentPassword_changeCurrentPassword() {
		when(userUtil.getUserObjectByIdOrEmailFromFields(anyMap())).thenReturn(user);

		String newPassword = "1234";
		Map<String, String> fields = new HashMap<>();
		fields.put("email", EMAIL);
		fields.put("currentPassword", newPassword);
		fields.put("newPassword", newPassword);
		fields.put("newPasswordConfirmation", newPassword);

		assertNotEquals(user.getPassword(), fields.get("currentPassword"));

		String exceptionExpectedMessage = "Wrong current password.";
		try {
			service.changeCurrentPassword(fields);
		} catch (Exception e) {
			assertEquals(InvalidUserDataException.class, e.getClass());
			assertEquals(exceptionExpectedMessage, e.getMessage());
			return;
		}
		failedExceptionNotThrown();
	}

	private void assertUserDtoResponse(UserResponseDTO responseUser) {
		assertNotNull(responseUser);
		assertEquals(UserResponseDTO.class, responseUser.getClass());
		assertEquals(ID, responseUser.getId());
		assertEquals(EMAIL, responseUser.getEmail());
		assertEquals(FIRST_NAME, responseUser.getFirstName());
		assertEquals(LAST_NAME, responseUser.getLastName());
		assertFalse(responseUser.isActive());
	}

	private void failedExceptionNotThrown() {
		fail("Should reach this line, exception was not thrown.");
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
