package com.olx.service.impl;

import com.olx.exceptions.UserAlreadyExistsException;
import com.olx.exceptions.UserNotFoundException;
import com.olx.model.User;
import com.olx.model.dto.UserDTO;
import com.olx.model.dto.UserInsertDTO;
import com.olx.model.dto.UserPasswordDTO;
import com.olx.model.dto.UserUpdateDTO;
import com.olx.repository.UserRepository;
import com.olx.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private ModelMapper mapper;


	@Override
	public List<UserDTO> getAll() {
		List<User> users = userRepo.findAll();

		// convert each "User" to a "UserDTO" class
		return users.stream().map((user -> mapper.map(user, UserDTO.class))).collect(Collectors.toList());
	}

	@Override
	public UserDTO getById(Long id) {
		User user = userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(userNotFoundMessage(id)));
		return mapper.map(user, UserDTO.class);
	}

	@Override
	public UserDTO getByEmail(String email) {
		User user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException(userNotFoundMessage(email)));
		return mapper.map(user, UserDTO.class);
	}

	@Override
	public UserDTO add(UserInsertDTO user) {
		// validate if user already exists by email
		Optional<User> userExists = userRepo.findByEmail(user.getEmail());
		if (userExists.isPresent())
			throw new UserAlreadyExistsException(userAlreadyExistsMessage(userExists.get().getEmail()));

		// create new User object to add on database to avoid Web Parameter Tampering
		User userAdd = mapper.map(user, User.class);

		userAdd.setPassword(passwordEncoder.encode(userAdd.getPassword()));

		userRepo.save(userAdd);

		return mapper.map(user, UserDTO.class);
	}

	@Override
	public void update(Long id, UserUpdateDTO userRequest) {
		User user = userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(userNotFoundMessage(id)));

		/*
		 if the "email" of userRequest is different of the current user email in db
		 check if there is another user with the new e-mail before updating
		*/
		if (!user.getEmail().equals(userRequest.getEmail())) {
			Optional<User> anotherUserExists = userRepo.findByEmail(userRequest.getEmail());
			if (anotherUserExists.isPresent())
				throw new UserAlreadyExistsException(userAlreadyExistsMessage(anotherUserExists.get().getEmail()));
		}

		user.setEmail(userRequest.getEmail());
		user.setFirstName(userRequest.getFirstName());
		user.setLastName(userRequest.getLastName());

		userRepo.save(user);
	}

	@Override
	public void changePassword(Long id, UserPasswordDTO password) {
		User user = userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(userNotFoundMessage(id)));

		user.setPassword(passwordEncoder.encode(password.getPassword()));

		userRepo.save(user);
	}

	@Override
	public Boolean validateLogin(User userRequest) {

		System.out.println(userRequest);
		String requestEmail = userRequest.getEmail();
		String requestPassword = userRequest.getPassword();

		Optional<User> userExists = userRepo.findByEmail(requestEmail);
		if (userExists.isEmpty())
			return false;

		return passwordEncoder.matches(requestPassword, userExists.get().getPassword());
	}

	@Override
	public void delete(Long id) {
		userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(userNotFoundMessage(id)));

		userRepo.deleteById(id);
	}

	public String userNotFoundMessage(Long id) {
		return "User with id [" + id + "] not found.";
	}

	public String userNotFoundMessage(String email) {
		return "User with email [" + email + "] not found.";
	}

	public String userAlreadyExistsMessage(String email) {
		return "User with e-mail [" + email + "] already exists.";
	}

}
