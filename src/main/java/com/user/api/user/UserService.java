package com.user.api.user;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

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

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.user.api.email.EmailService;
import com.user.api.email.model.EmailDTO;
import com.user.api.exceptions.InvalidUserDataException;
import com.user.api.exceptions.ObjectFieldsValidationException;
import com.user.api.exceptions.ResetPasswordTokenException;
import com.user.api.exceptions.UserAlreadyExistsException;
import com.user.api.exceptions.UserNotFoundException;
import com.user.api.registration.RegistrationService;
import com.user.api.security.util.JWTUtil;
import com.user.api.user.enums.Role;
import com.user.api.user.model.User;
import com.user.api.user.model.UserRequestDTO;
import com.user.api.user.model.UserResponseDTO;
import com.user.api.user.util.UserUtil;
import com.user.api.userProperties.UserPropertiesService;
import com.user.api.userProperties.model.UserProperties;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;


@Service
@RequiredArgsConstructor
public class UserService {

	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepo;
	private final UserPropertiesService userPropsService;
	private final RegistrationService registrationService;
	private final ModelMapper mapper;
	private final Validator validator;
	private final EmailService emailService;
	private final UserUtil userUtil;
	

	@Value("${project.api.domainUrl}")
	private String apiDomainUrl;
	@Value("${spring.security.jwt.tokenPassword}")
	private String tokenPassword;

	public Page<UserResponseDTO> getAll(Pageable pageable) {
		Page<User> users = userRepo.findAll(pageable);
		return users.map(user -> mapper.map(user, UserResponseDTO.class));
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
		User user = userUtil.getUserObjectByIdOrEmailFromFields(fields);
		return mapper.map(user, UserResponseDTO.class);
	}

	@Transactional
	public UserResponseDTO add(UserRequestDTO user) {
		validateUserAlreadyExistsByEmail(user);

		User userAdd = createUserAddObject(user, false);
		User userSaved = userRepo.save(userAdd);

		registrationService.sendActivationEmail(userSaved);
		return mapper.map(userSaved, UserResponseDTO.class);
	}

	@Transactional
	public UserResponseDTO addUserAlreadyActive(UserRequestDTO user) {
		validateUserAlreadyExistsByEmail(user);

		User userAdd = createUserAddObject(user, true);
		User userSaved = userRepo.save(userAdd);

		return mapper.map(userSaved, UserResponseDTO.class);
	}

	private User createUserAddObject(UserRequestDTO user, boolean active){
		// create new User object to add on database to avoid Web Parameter Tampering
		User userAdd = mapper.map(user, User.class);

		userAdd.setPassword(passwordEncoder.encode(userAdd.getPassword()));
		userAdd.setActive(active);
		userAdd.setRole(Role.ROLE_USER);

		UserProperties userProps = new UserProperties();
		userProps.setUser(userAdd);
		userAdd.setUserProperties(userProps);
		userProps.setUser(userAdd);

		return userAdd;
	}

	private Optional<User> validateUserAlreadyExistsByEmail(UserRequestDTO user) throws UserAlreadyExistsException {
		Optional<User> userExists = userRepo.findByEmail(user.getEmail());
		if (userExists.isPresent())
			throw new UserAlreadyExistsException(userExists.get().getEmail());
		return userExists;
	}

	public UserResponseDTO update(Map<String, String> fields) {
		User user = userUtil.getUserObjectByIdOrEmailFromFields(fields);
		User userRequest = mapper.map(fields, User.class);

		validateUserData(userRequest);
		/*
		 * Set each field of the "user" object to save manually to avoid web tampering
		 * And also to trigger the "creationDate" field in User object
		 * */
		user.setFirstName(userRequest.getFirstName());
		user.setLastName(userRequest.getLastName());

		return mapper.map(userRepo.save(user), UserResponseDTO.class);
	}

	// TODO implement change email process
	/*
	 CHANGE EMAIL PROCESS:
	 if the "email" of userRequest is different of the current user email in db
	 check if there is another user with the new e-mail before updating
	*/
	/*
	public void changeUserEmail(){

		if (!user.getEmail().equals(userRequest.getEmail())) {
			Optional<User> anotherUserExists = userRepo.findByEmail(userRequest.getEmail());
			if (anotherUserExists.isPresent())
				throw new UserAlreadyExistsException(anotherUserExists.get().getEmail());
		}

	}*/

	@SneakyThrows
	private void validateUserData(User user)  {
		BindingResult result = new BeanPropertyBindingResult(user, "user");

		validator.validate(user, result);

		if (result.hasErrors()) {
			throw new ObjectFieldsValidationException(result.getFieldErrors());
		}
	}

	public void delete(Long id) {
		User user = userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
		userRepo.delete(user);
	}

	public void delete(String email) {
		User user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
		userRepo.delete(user);
	}

	public void delete(Map<String, String> fields) {
		User user = userUtil.getUserObjectByIdOrEmailFromFields(fields);
		userRepo.delete(user);
	}

	@Transactional
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

		User user = userUtil.getUserObjectByIdOrEmailFromFields(fields);
		if (!passwordEncoder.matches(currentPassword, user.getPassword())){
			throw new InvalidUserDataException("Wrong current password.");
		}

		updatePassword(user, newPassword);
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
			throw new InvalidUserDataException("Password is too long, more than 255 characters.");
	}

	@Transactional
	public void requestResetPasswordEmail(Map<String, String> fields) {
		User user = userUtil.getUserObjectByIdOrEmailFromFields(fields);

		String token = JWTUtil.createToken(user.getEmail(), 60, "", tokenPassword);

		UserProperties userProps = userPropsService.getUserProperties(user);
		userProps.setResetPasswordToken(token);
		userPropsService.saveUserProperties(userProps);

		String resetPasswordUrl = apiDomainUrl + "/api/user/resetPassword?token=" + token;

		EmailDTO passwordEmail = new EmailDTO();
		passwordEmail.setAddressTo(user.getEmail());
		passwordEmail.setSubject("Reset User Password Confirmation");
		passwordEmail.setBody("Click on this link to reset your password:<br>"
				+ resetPasswordUrl
				+ "<br><br>"
				+ "The link expires in 60 minutes."
		);

		emailService.sendEmailToQueue(passwordEmail);
	}

	@Transactional
	public void resetPassword(String token) {
		DecodedJWT decodedJWT;
		try {
			decodedJWT = JWTUtil.verifyToken(token, tokenPassword);
		} catch (TokenExpiredException e) {
			throw new ResetPasswordTokenException(e.getMessage());
		} catch (Exception e){
			throw new ResetPasswordTokenException(e.getMessage());
		}

		String email = decodedJWT.getSubject();
		User user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
		UserProperties userProps = userPropsService.getUserProperties(user);

		if (!token.equals(userProps.getResetPasswordToken())){
			throw new ResetPasswordTokenException("Invalid token for password reset, request to reset your password again.");
		}

		String randomPassword = generateCommonLangPassword();

		user.setPassword(passwordEncoder.encode(randomPassword));
		userProps.setResetPasswordToken(null);
		userPropsService.saveUserProperties(userProps);

		EmailDTO passwordEmail = new EmailDTO();
		passwordEmail.setAddressTo(user.getEmail());
		passwordEmail.setSubject("Password Reset");
		passwordEmail.setBody("Your password has been reset.<br>The new password is: " + randomPassword);

		emailService.sendEmailToQueue(passwordEmail);
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
