package com.olx.service.impl;

import com.olx.exceptions.InvalidUserDataException;
import com.olx.exceptions.UserAlreadyExistsException;
import com.olx.exceptions.UserNotFoundException;
import com.olx.model.User;
import com.olx.model.dto.UserDTO;
import com.olx.model.dto.UserInsertDTO;
import com.olx.model.dto.UserUpdateDTO;
import com.olx.repository.UserRepository;
import com.olx.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepo;
	private final ModelMapper mapper;

	public UserServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepo, ModelMapper mapper) {
		this.passwordEncoder = passwordEncoder;
		this.userRepo = userRepo;
		this.mapper = mapper;
	}

	@Override
	public List<UserDTO> getAll() {
		List<User> users = userRepo.findAll();

		// convert each "User" to a "UserDTO" class
		return users.stream().map((user -> mapper.map(user, UserDTO.class))).collect(Collectors.toList());
	}

	@Override
	public UserDTO getById(Long id) {
		User user = userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
		return mapper.map(user, UserDTO.class);
	}

	@Override
	public UserDTO getByEmail(String email) {
		User user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
		return mapper.map(user, UserDTO.class);
	}

	@Override
	public UserDTO add(UserInsertDTO user) {
		// validate if user already exists by email
		Optional<User> userExists = userRepo.findByEmail(user.getEmail());
		if (userExists.isPresent())
			throw new UserAlreadyExistsException(userExists.get().getEmail());

		// create new User object to add on database to avoid Web Parameter Tampering
		User userAdd = mapper.map(user, User.class);

		userAdd.setPassword(passwordEncoder.encode(userAdd.getPassword()));

		return mapper.map(userRepo.save(userAdd), UserDTO.class);
	}

	@Override
	public UserDTO update(Long id, UserUpdateDTO userRequest) {
		User user = userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));

		/*
		 if the "email" of userRequest is different of the current user email in db
		 check if there is another user with the new e-mail before updating
		*/
		if (!user.getEmail().equals(userRequest.getEmail())) {
			Optional<User> anotherUserExists = userRepo.findByEmail(userRequest.getEmail());
			if (anotherUserExists.isPresent())
				throw new UserAlreadyExistsException(anotherUserExists.get().getEmail());
		}

		user.setEmail(userRequest.getEmail());
		user.setFirstName(userRequest.getFirstName());
		user.setLastName(userRequest.getLastName());

		return mapper.map(userRepo.save(user), UserDTO.class);
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
	public void delete(Long id) {
		userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
		userRepo.deleteById(id);
	}

	@Override
	public Boolean validateLogin(User userRequest) {
		Optional<User> userExists = userRepo.findByEmail(userRequest.getEmail());
		if (userExists.isEmpty())
			return false;

		return passwordEncoder.matches(userRequest.getPassword(), userExists.get().getPassword());
	}

}
