package com.olx.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.olx.controller.dto.UserDTO;
import com.olx.controller.dto.UserInsertDTO;
import com.olx.controller.dto.UserPasswordDTO;
import com.olx.exceptions.InvalidUserDataException;
import com.olx.exceptions.UserAlreadyExistsException;
import com.olx.model.User;
import com.olx.repository.UserRepository;
import com.olx.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepo;

	@Override
	public UserDTO getById(Long id) throws EntityNotFoundException {
		User user = userRepo.findById(id).orElseThrow(EntityNotFoundException::new);
		return UserDTO.converter(user);
	}

	@Override
	public UserDTO getByEmail(String email) throws EntityNotFoundException {
		User user = userRepo.findByEmail(email).orElseThrow(EntityNotFoundException::new);
		return UserDTO.converter(user);
	}

	@Override
	public List<UserDTO> getAll() {
		List<User> users = userRepo.findAll();

		// convert each "User" to a "UserDTO" class
		List<UserDTO> userList = users.stream().map((user) -> UserDTO.converter(user)).collect(Collectors.toList());

		return userList;
	}

	@Override
	public UserDTO add(User user) throws UserAlreadyExistsException {
		// validate if user already exists
		Optional<User> userExists = userRepo.findByEmail(user.getEmail());
		if (!userExists.isEmpty()) {
			throw new UserAlreadyExistsException("User '" + user.getEmail() + "' already exists.");
		}

		// create new User object to add on database to avoid Web Parameter Tampering
		User userAdd = new User();

		userAdd.setPassword(passwordEncoder.encode(user.getPassword()));
		userAdd.setEmail(user.getEmail());
		userAdd.setFirstName(user.getFirstName());
		userAdd.setLastName(user.getLastName());

		userRepo.save(userAdd);

		return UserDTO.converter(userAdd);
	}

	@Override
	public void update(Long id, UserInsertDTO userRequest)
			throws EntityNotFoundException, InvalidUserDataException, UserAlreadyExistsException {

		User user = userRepo.findById(id).orElseThrow(EntityNotFoundException::new);

		// validate fields
		if (userRequest.getEmail() == null) {
			throw new InvalidUserDataException("Email is blank.");
		}

		// check if the e-mail of userRequest is the same of database
		// if the email is different, check if there is already another user with the
		// new e-mail
		if (!user.getEmail().equals(userRequest.getEmail())) {
			Optional<User> userExists = userRepo.findByEmail(userRequest.getEmail());
			if (!userExists.isEmpty()) {
				throw new UserAlreadyExistsException(
						"There is already an user with the e-mail '" + userRequest.getEmail() + "'");
			}
		}

		user.setEmail(userRequest.getEmail());
		user.setFirstName(userRequest.getFirstName());
		user.setLastName(userRequest.getLastName());

		userRepo.save(user);
	}

	@Override
	public void changePassword(Long id, UserPasswordDTO password) throws EntityNotFoundException {

		User user = userRepo.findById(id).orElseThrow(EntityNotFoundException::new);

		user.setPassword(passwordEncoder.encode(password.getPassword()));

		userRepo.save(user);
	}

	@Override
	public Boolean validateLogin(Optional<User> userRequest) {

		if (userRequest.isEmpty()) {
			return false;
		}

		String requestEmail = userRequest.get().getEmail();
		String requestPassword = userRequest.get().getPassword();

		Optional<User> UserExists = userRepo.findByEmail(requestEmail);
		if (UserExists.isEmpty()) {
			return false;
		}

		User user = UserExists.get();

		if (passwordEncoder.matches(requestPassword, user.getPassword()) == false)
			return false;

		return true;
	}

}
