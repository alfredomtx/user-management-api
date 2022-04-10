package com.user.api.resgistration;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.user.api.email.EmailService;
import com.user.api.email.model.Email;
import com.user.api.exceptions.AccountActivationException;
import com.user.api.exceptions.ResetPasswordTokenException;
import com.user.api.exceptions.UserNotFoundException;
import com.user.api.user.UserRepository;
import com.user.api.user.model.User;
import com.user.api.userProperties.UserPropertiesService;
import com.user.api.userProperties.model.UserProperties;
import com.user.api.util.JWTUtil;
import com.user.api.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RegistrationService {

	@Autowired
	private UserRepository userRepo;
	@Autowired
	private UserPropertiesService userPropsService;
	@Autowired
	private EmailService emailService;

	@Value("${project.api.domainUrl}")
	private String apiDomainUrl;

	public void requestActivateAccountEmail(Map<String, String> fields) {
		User user = UserUtil.getUserObjectByIdOrEmailFromFields(fields);
		sendActivationEmail(user);
	}

	public void sendActivationEmail(User user){
		checkUserAlreadyActive(user);
		String token = JWTUtil.createToken(user.getEmail(), 60, "");

		UserProperties userProps = userPropsService.getUserProperties(user);
		userProps.setActivateAccountToken(token);
		userPropsService.saveUserProperties(userProps);

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

		DecodedJWT decodedJWT;
		try {
			decodedJWT = JWTUtil.verifyToken(token);
		} catch (TokenExpiredException e) {
			sendActivationEmail(user);
			throw new AccountActivationException(e.getMessage());
		} catch (Exception e){
			throw new AccountActivationException(e.getMessage());
		}

		UserProperties userProps = userPropsService.getUserProperties(user);

		if (!token.equals(userProps.getActivateAccountToken())){
			sendActivationEmail(user);
			throw new ResetPasswordTokenException("Invalid account activation token, a new activation link has been sent to the email.");
		}

		user.setActive(true);
		userProps.setActivateAccountToken(null);
		userPropsService.saveUserProperties(userProps);
	}

	private void checkUserAlreadyActive(User user){
		if (user.isActive()){
			throw new AccountActivationException("User is already active.");
		}
	}


}
