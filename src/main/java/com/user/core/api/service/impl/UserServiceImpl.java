package com.user.core.api.service.impl;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.core.api.exceptions.InvalidUserDataException;
import com.user.core.api.exceptions.UserAlreadyExistsException;
import com.user.core.api.exceptions.UserNotFoundException;
import com.user.core.api.model.User;
import com.user.core.api.model.dto.UserInsertDTO;
import com.user.core.api.model.dto.UserResponseDTO;
import com.user.core.api.repository.UserRepository;
import com.user.core.api.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService {

	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepo;
	private final ModelMapper mapper;

	@Autowired
	private Validator validator;

	@Autowired
	private ObjectMapper objectMapper;


	public UserServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepo, ModelMapper mapper) {
		this.passwordEncoder = passwordEncoder;
		this.userRepo = userRepo;
		this.mapper = mapper;
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
	public UserResponseDTO add(UserInsertDTO user) {
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


		User userRequest = null;
		try {
			userRequest = objectMapper.updateValue(user , fields);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		}


		updateUser(userRequest);
		System.out.println(userRequest);
		return mapper.map(user, UserResponseDTO.class);

		/*
		 if the "email" of userRequest is different of the current user email in db
		 check if there is another user with the new e-mail before updating
		*//*
		if (!user.getEmail().equals(userRequest.getEmail())) {
			Optional<User> anotherUserExists = userRepo.findByEmail(userRequest.getEmail());
			if (anotherUserExists.isPresent())
				throw new UserAlreadyExistsException(anotherUserExists.get().getEmail());
		}

		user.setEmail(userRequest.getEmail());
		user.setFirstName(userRequest.getFirstName());
		user.setLastName(userRequest.getLastName());

		return mapper.map(userRepo.save(user), UserResponseDTO.class);*/
	}

	// TODO create field validation exception to send a json with fields and error messages
	private void updateUser(User user){

		BindingResult result = new BeanPropertyBindingResult(user, "user");

		validator.validate(user, result);

		if(result.hasErrors()){
			List<FieldError> errors = result.getFieldErrors();
			List<String> message = new ArrayList<>();
			for (FieldError e : errors){
				message.add("@" + e.getField() + ": " + e.getDefaultMessage());
			}
			throw new InvalidUserDataException(message.toString());
		}


		System.out.println(user);
		userRepo.save(user);

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
