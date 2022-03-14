package com.olx.service.impl;

import com.olx.model.User;
import com.olx.model.dto.UserDTO;
import com.olx.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;



class UserServiceImplTest {

	public static final Long ID = 1L;
	public static final String EMAIL = "test@test.com";
	public static final String PASSWORD = "test";
	public static final String FIRST_NAME = "John";
	public static final String LAST_NAME = "Snow";

	@InjectMocks
	private UserServiceImpl service = mock(UserServiceImpl.class);;

	@Mock
	private ModelMapper mapper;

	@Mock
	private UserRepository repository = mock(UserRepository.class);


	private User user;
	private UserDTO userDTO;
	private Optional<User> optionalUser;

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

		// userDTO = mapper.map(user, UserDTO.class);
		userDTO = new UserDTO();
		userDTO.setId(ID);
		userDTO.setEmail(EMAIL);
		userDTO.setPassword(PASSWORD);
		userDTO.setFirstName(FIRST_NAME);
		userDTO.setLastName(LAST_NAME);

		optionalUser = Optional.of(user);



	}

	@Test
	void getAll() {
	}

	@Test
	@DisplayName("shouldReturnUser_WhenGetById")
	void getById() {


		when(service.getById(anyLong())).thenReturn(userDTO);
		when(repository.findById(anyLong())).thenReturn(optionalUser);

		UserDTO response = service.getById(ID);
		System.out.println(response);

		assertNotNull(response);
		assertEquals(UserDTO.class, response.getClass());
		assertEquals(ID, response.getId());
		assertEquals(EMAIL, response.getEmail());


	}

	@Test
	void getByEmail() {
	}

	@Test
	void add() {
	}

	@Test
	void update() {
	}

	@Test
	void changePassword() {
	}

	@Test
	void validateLogin() {
	}

	@Test
	void delete() {
	}
}