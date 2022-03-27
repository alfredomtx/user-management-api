package com.user.api.service.impl;

import com.user.api.exceptions.InvalidUserDataException;
import com.user.api.exceptions.UserNotFoundException;
import com.user.api.repository.UserRepository;
import com.user.api.service.UserService;
import com.user.api.exceptions.UserAlreadyExistsException;
import com.user.api.exceptions.ObjectFieldsValidationException;
import com.user.api.model.User;
import com.user.api.model.dto.UserRequestDTO;
import com.user.api.model.dto.UserResponseDTO;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService {

	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepo;
	private final ModelMapper mapper;
	private final Validator validator;

	public UserServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepo, ModelMapper mapper, Validator validator) {
		this.passwordEncoder = passwordEncoder;
		this.userRepo = userRepo;
		this.mapper = mapper;
		this.validator = validator;
	}

	@Override
	public List<UserResponseDTO> getAll() {
		List<User> users = userRepo.findAll();

		// convert each "User" to a "UserDTO" class
		return users.stream().map((user -> mapper.map(user, UserResponseDTO.class))).collect(Collectors.toList());
	}

	@Override
	public UserResponseDTO getById(Long id) {
		User user = userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
		return mapper.map(user, UserResponseDTO.class);
	}

	@Override
	public UserResponseDTO getByEmail(String email) {
		User user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
		return mapper.map(user, UserResponseDTO.class);
	}

	@Override
	public UserResponseDTO add(UserRequestDTO user) {
		// validate if user already exists by email
		Optional<User> userExists = userRepo.findByEmail(user.getEmail());
		if (userExists.isPresent())
			throw new UserAlreadyExistsException(userExists.get().getEmail());

		// create new User object to add on database to avoid Web Parameter Tampering
		User userAdd = mapper.map(user, User.class);

		userAdd.setPassword(passwordEncoder.encode(userAdd.getPassword()));

		return mapper.map(userRepo.save(userAdd), UserResponseDTO.class);
	}

	@Override
	public UserResponseDTO update(Long id, Map<String, Object> fields) {
		User user = userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));

		User userRequest = mapper.map(fields, User.class);

		validateUserData(userRequest);
		/*
		 if the "email" of userRequest is different of the current user email in db
		 check if there is another user with the new e-mail before updating
		*/
		if (!user.getEmail().equals(userRequest.getEmail())) {
			Optional<User> anotherUserExists = userRepo.findByEmail(userRequest.getEmail());
			if (anotherUserExists.isPresent())
				throw new UserAlreadyExistsException(anotherUserExists.get().getEmail());
		}
		/*
		 * Set each field of the "user" object to save manually to avoid web tampering
		 * And also to trigger the "creationDate" field in User object
		 * */
		user.setEmail(userRequest.getEmail());
		user.setFirstName(userRequest.getFirstName());
		user.setLastName(userRequest.getLastName());

		return mapper.map(userRepo.save(user), UserResponseDTO.class);
	}

	@SneakyThrows
	private void validateUserData(User user)  {
		BindingResult result = new BeanPropertyBindingResult(user, "user");

		validator.validate(user, result);

		if (result.hasErrors()) {
			throw new ObjectFieldsValidationException(result.getFieldErrors());
		}
	}

	@Override
	public void delete(Long id) {
		userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
		userRepo.deleteById(id);
	}

	@Override
	public void changePassword(Long id, String password) {
		validatePassword(password);

		User user = userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
		user.setPassword(passwordEncoder.encode(password));
		userRepo.save(user);
	}

	private void validatePassword(String password) throws InvalidUserDataException {
		if (password.trim().isEmpty())
			throw new InvalidUserDataException("Password is blank.");
		if (password.length() < 4)
			throw new InvalidUserDataException("Password is too short, less than 4 characters.");
		if (password.length() > 255)
			throw new InvalidUserDataException("Password is too big, more than 255 characters.");
	}

	@Override
	public Boolean validateLogin(User userRequest) {
		Optional<User> userExists = userRepo.findByEmail(userRequest.getEmail());
		if (userExists.isEmpty())
			return false;

		return passwordEncoder.matches(userRequest.getPassword(), userExists.get().getPassword());
	}

}
