package com.user.api.registration;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.user.api.email.EmailService;
import com.user.api.email.model.EmailDTO;
import com.user.api.exceptions.AccountActivationException;
import com.user.api.exceptions.ResetPasswordTokenException;
import com.user.api.exceptions.UserNotFoundException;
import com.user.api.security.util.JWTUtil;
import com.user.api.user.UserRepository;
import com.user.api.user.model.User;
import com.user.api.user.util.UserUtil;
import com.user.api.userProperties.UserPropertiesService;
import com.user.api.userProperties.model.UserProperties;
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
	@Autowired
	private UserUtil userUtil;

	@Value("${project.api.domainUrl}")
	private String apiDomainUrl;

	public void requestActivateAccountEmail(Map<String, String> fields) {
		User user = userUtil.getUserObjectByIdOrEmailFromFields(fields);
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

		EmailDTO activationEmail = new EmailDTO();
		activationEmail.setAddressTo(user.getEmail());
		activationEmail.setSubject("Account Activation");


		StringBuilder htmlButton = new StringBuilder();
		htmlButton.append("<a href='" + activationUrl + "' target='_blank' ");
			htmlButton.append("style='cursor: pointer; text-decoration: none; padding-top: 5px' ");
		htmlButton.append(">");
		htmlButton.append("<button class='button' ");
			htmlButton.append("style='padding: 10px; cursor: pointer; background-color: red; text-decoration: none; font-weight: bold' ");
		htmlButton.append(">");
		htmlButton.append("Click to Activate Account");
		htmlButton.append("</button>");
		htmlButton.append("</a>");

		StringBuilder activationUrlHtml = new StringBuilder();
		activationUrlHtml.append("<a href='" + activationUrl + "'>" + activationUrl + "</a></small>");

		StringBuilder emailHtml = new StringBuilder();
		emailHtml.append("Open the link to activate your account:<br>");
		emailHtml.append(htmlButton);
		emailHtml.append("<br><br>");
		emailHtml.append("<div style='color: gray'>");
			emailHtml.append("<small>");
			emailHtml.append("Alternatively you can copy and paste the link in your browser:<br>");
			emailHtml.append(activationUrlHtml);
			emailHtml.append("</small>");
			emailHtml.append("<br><br>");
			emailHtml.append("The link expires in 60 minutes.");
		emailHtml.append("</div>");

		activationEmail.setBody(emailHtml.toString());
		emailService.sendEmailToQueue(activationEmail);
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
