package com.user.api.resgistration;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.user.api.email.EmailService;
import com.user.api.email.model.Email;
import com.user.api.exceptions.AccountActivationException;
import com.user.api.exceptions.ResetPasswordTokenException;
import com.user.api.exceptions.UserNotFoundException;
import com.user.api.user.UserRepository;
import com.user.api.user.UserService;
import com.user.api.user.model.User;
import com.user.api.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegistrationService {

	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepo;
	private final UserService userService;
	private final ModelMapper mapper;
	private final Validator validator;
	private final EmailService emailService;

	@Value("${project.api.domainUrl}")
	private String apiDomainUrl;

	public void requestActivateAccountEmail(Map<String, String> fields) {
		User user = userService.getUserObjectByIdOrEmailFromFields(fields);
		checkUserAlreadyActive(user);
		sendActivationEmail(user);
	}

	private void sendActivationEmail(User user){
		String token = JWTUtil.createToken(user.getEmail(), 60, "");

		user.setActivateAccountToken(token);
		userRepo.save(user);

		String activationUrl = apiDomainUrl
				+ "/api/registration/activateAccount?token=" + token
				+ "&email=" + user.getEmail();

		Email activationEmail = new Email();
		activationEmail.setAddressTo(user.getEmail());
		activationEmail.setSubject("Account Activation");
		activationEmail.setBody("Click on this link to activate your account:<br>"
				+ activationUrl
				+ "<br><br>"
				+ "The link expires in 60 minutes."
		);

		emailService.sendEmail(activationEmail);

	}

	public void activateAccount(String token, String email) {
		User user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
		checkUserAlreadyActive(user);

		DecodedJWT decodedJWT;
		try {
			decodedJWT = JWTUtil.verifyToken(token);
		} catch (TokenExpiredException e) {
			sendActivationEmail(user);
			throw new AccountActivationException(e.getMessage());
		} catch (Exception e){
			throw new AccountActivationException(e.getMessage());
		}

		if (!token.equals(user.getActivateAccountToken())){
			sendActivationEmail(user);
			throw new ResetPasswordTokenException("Invalid account activation token, a new activation link has been sent to the email.");
		}

		user.setActive(true);
		user.setActivateAccountToken("");
		userRepo.save(user);
	}

	private void checkUserAlreadyActive(User user){
		if (user.isActive()){
			throw new AccountActivationException("User is already active.");
		}
	}


}
