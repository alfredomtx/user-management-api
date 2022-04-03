package com.user.api.user;

import com.user.api.email.model.Email;
import com.user.api.email.model.EmailDTO;
import com.user.api.enums.Role;
import com.user.api.exceptions.InvalidUserDataException;
import com.user.api.exceptions.ObjectFieldsValidationException;
import com.user.api.exceptions.UserAlreadyExistsException;
import com.user.api.exceptions.UserNotFoundException;
import com.user.api.user.model.User;
import com.user.api.user.model.UserRequestDTO;
import com.user.api.user.model.UserResponseDTO;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
	public Page<UserResponseDTO> getAll(Pageable pageable) {
		Page<User> users = userRepo.findAll(pageable);
		// convert each "User" to a "UserDTO" class
		Page<UserResponseDTO> usersList = users.map(user -> mapper.map(user, UserResponseDTO.class));
		return usersList;
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
		userAdd.setActive(true);
		userAdd.setRole(Role.ROLE_USER);

		return mapper.map(userRepo.save(userAdd), UserResponseDTO.class);
	}

	@Override
	public UserResponseDTO update(Map<String, String> fields) {

		User user = getUserObjectByIdOrEmailFromFields(fields);

		User userRequest = mapper.map(fields, User.class);

		validateUserData(userRequest);
		/*
		 CHANGE EMAIL PROCESS:
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
		//user.setEmail(userRequest.getEmail()); // make a separated process to update email in the future(with confirmation, etc)
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
	public void changePassword(Map<String, String> fields) {

		String password = fields.get("password");
		if (password == null)
			throw new InvalidUserDataException("[password] field not set.");

		validatePassword(password);

		User user = getUserObjectByIdOrEmailFromFields(fields);

		updatePassword(user, password);
	}

	private User getUserObjectByIdOrEmailFromFields(Map<String, String> fields){
		String email = fields.get("email");
		String idString = fields.get("id");

		if (email == null && idString == null)
			throw new InvalidUserDataException("[email] or [id] field must be set.");

		User user = null;
		if (email != null) {
			user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
		} else if (idString != null){
			Long id = Long.valueOf(idString);
			user = userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
		}
		return user;
	}

	private void updatePassword(User user, String password){
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
