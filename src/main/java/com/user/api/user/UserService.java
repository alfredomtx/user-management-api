package com.user.api.user;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.user.api.email.EmailService;
import com.user.api.email.model.Email;
import com.user.api.exceptions.*;
import com.user.api.user.enums.Role;
import com.user.api.user.model.User;
import com.user.api.user.model.UserRequestDTO;
import com.user.api.user.model.UserResponseDTO;
import com.user.api.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserService {

	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepo;
	private final ModelMapper mapper;
	private final Validator validator;
	private final EmailService emailService;


	@Value("${project.api.domainUrl}")
	private String apiDomainUrl;

	public Page<UserResponseDTO> getAll(Pageable pageable) {
		Page<User> users = userRepo.findAll(pageable);
		Page<UserResponseDTO> usersList = users.map(user -> mapper.map(user, UserResponseDTO.class));
		return usersList;
	}

	public UserResponseDTO getById(Long id) {
		User user = userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
		return mapper.map(user, UserResponseDTO.class);
	}

	public UserResponseDTO getByEmail(String email) {
		User user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
		return mapper.map(user, UserResponseDTO.class);
	}

	public UserResponseDTO getByIdOrEmail(Map<String, String> fields) {
		User user = getUserObjectByIdOrEmailFromFields(fields);
		return mapper.map(user, UserResponseDTO.class);
	}

	public UserResponseDTO add(UserRequestDTO user) {
		// validate if user already exists by email
		Optional<User> userExists = userRepo.findByEmail(user.getEmail());
		if (userExists.isPresent())
			throw new UserAlreadyExistsException(userExists.get().getEmail());

		// create new User object to add on database to avoid Web Parameter Tampering
		User userAdd = mapper.map(user, User.class);

		userAdd.setPassword(passwordEncoder.encode(userAdd.getPassword()));
		userAdd.setActive(false);
		userAdd.setRole(Role.ROLE_USER);

		return mapper.map(userRepo.save(userAdd), UserResponseDTO.class);
	}

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

	public void delete(Long id) {
		userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
		userRepo.deleteById(id);
	}

	public void changeCurrentPassword(Map<String, String> fields) {
		String currentPassword = fields.get("currentPassword");
		if (currentPassword == null)
			throw new InvalidUserDataException("[currentPassword] field not set.");

		String newPassword = fields.get("newPassword");
		if (newPassword == null)
			throw new InvalidUserDataException("[newPassword] field not set.");

		String newPasswordConfirmation = fields.get("newPasswordConfirmation");
		if (newPasswordConfirmation == null)
			throw new InvalidUserDataException("[newPasswordConfirmation] field not set.");

		if (!newPassword.equals(newPasswordConfirmation)){
			throw new InvalidUserDataException("new password confirmation does not match.");
		}

		validatePassword(newPassword);

		User user = getUserObjectByIdOrEmailFromFields(fields);
		if (!passwordEncoder.matches(currentPassword, user.getPassword())){
			throw new InvalidUserDataException("Wrong current password.");
		}

		updatePassword(user, newPassword);
	}

	public User getUserObjectByIdOrEmailFromFields(Map<String, String> fields){
		String email = fields.get("email");
		String idString = fields.get("id");

		if (email == null && idString == null)
			throw new InvalidUserDataException("[email] or [id] field must be set.");

		User user = null;
		if (email != null) {
			user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
		} else if (idString != null){
			Long id;
			try {
				id = Long.valueOf(idString);
			} catch (Exception e){
				throw new InvalidUserDataException("[id] field is invalid.");
			}
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

	public void requestResetPasswordEmail(Map<String, String> fields) {
		User user = getUserObjectByIdOrEmailFromFields(fields);

		String token = JWTUtil.createToken(user.getEmail(), 60, "");

		user.setResetPasswordToken(token);
		userRepo.save(user);

		String resetPasswordUrl = apiDomainUrl + "/api/user/resetPassword?token=" + token;

		Email passwordEmail = new Email();
		passwordEmail.setAddressTo(user.getEmail());
		passwordEmail.setSubject("Reset User Password Confirmation");
		passwordEmail.setBody("Click on this link to reset your password:<br>"
				+ resetPasswordUrl
				+ "<br><br>"
				+ "The link expires in 60 minutes."
		);

		emailService.sendEmail(passwordEmail);
	}

	public void resetPassword(String token) {
		DecodedJWT decodedJWT;
		try {
			decodedJWT = JWTUtil.verifyToken(token);
		} catch (TokenExpiredException e) {
			throw new ResetPasswordTokenException(e.getMessage());
		} catch (Exception e){
			throw new ResetPasswordTokenException(e.getMessage());
		}

		String email = decodedJWT.getSubject();
		User user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

		if (!token.equals(user.getResetPasswordToken())){
			throw new ResetPasswordTokenException("Invalid token for password reset, request to reset your password again.");
		}

		String randomPassword = generateCommonLangPassword();

		user.setPassword(passwordEncoder.encode(randomPassword));
		user.setResetPasswordToken("");
		userRepo.save(user);

		Email passwordEmail = new Email();
		passwordEmail.setAddressTo(user.getEmail());
		passwordEmail.setSubject("Password Reset");
		passwordEmail.setBody("Your password has been reset.<br>The new password is: " + randomPassword);

		emailService.sendEmail(passwordEmail);
	}

	public String generateCommonLangPassword() {
		String upperCaseLetters = RandomStringUtils.random(2, 65, 90, true, true);
		String lowerCaseLetters = RandomStringUtils.random(2, 97, 122, true, true);
		String numbers = RandomStringUtils.randomNumeric(2);
		String specialChar = RandomStringUtils.random(2, 33, 47, false, false);
		String totalChars = RandomStringUtils.randomAlphanumeric(2);
		String combinedChars = upperCaseLetters.concat(lowerCaseLetters)
				.concat(numbers)
				.concat(specialChar)
				.concat(totalChars);
		List<Character> pwdChars = combinedChars.chars()
				.mapToObj(c -> (char) c)
				.collect(Collectors.toList());
		Collections.shuffle(pwdChars);
		String password = pwdChars.stream()
				.collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
				.toString();
		return password;
	}


}
