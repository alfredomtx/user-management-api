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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Map;

@Service
public class RegistrationService {
	private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);

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
	@Value("${spring.security.jwt.tokenPassword}")
	private String tokenPassword;

	public void requestActivateAccountEmail(Map<String, String> fields) {
		User user = userUtil.getUserObjectByIdOrEmailFromFields(fields);
		sendActivationEmail(user);
	}

	@Transactional
	public void sendActivationEmail(User user){
		checkUserAlreadyActive(user);
		String token = JWTUtil.createToken(user.getEmail(), 60, "", tokenPassword);

		UserProperties userProps = userPropsService.getUserProperties(user);
		userProps.setActivateAccountToken(token);
		userPropsService.saveUserProperties(userProps);

		String activationUrl = apiDomainUrl
				+ "/api/registration/activateAccount?token=" + token
				+ "&email=" + user.getEmail();

		EmailDTO activationEmail = new EmailDTO();
		activationEmail.setAddressTo(user.getEmail());
		activationEmail.setSubject("Account Activation");
		activationEmail.setBody(buildEmail(user.getFirstName(), activationUrl));
		emailService.sendEmailToQueue(activationEmail);
	}

	public void activateAccount(String token, String email) {
		User user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

		DecodedJWT decodedJWT;
		try {
			decodedJWT = JWTUtil.verifyToken(token, tokenPassword);
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

	private String buildEmail(String name, String link) {
		return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
				"\n" +
				"<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
				"\n" +
				"  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
				"    <tbody><tr>\n" +
				"      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
				"        \n" +
				"        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
				"          <tbody><tr>\n" +
				"            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
				"                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
				"                  <tbody><tr>\n" +
				"                    <td style=\"padding-left:10px\">\n" +
				"                  \n" +
				"                    </td>\n" +
				"                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
				"                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirm your email</span>\n" +
				"                    </td>\n" +
				"                  </tr>\n" +
				"                </tbody></table>\n" +
				"              </a>\n" +
				"            </td>\n" +
				"          </tr>\n" +
				"        </tbody></table>\n" +
				"        \n" +
				"      </td>\n" +
				"    </tr>\n" +
				"  </tbody></table>\n" +
				"  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
				"    <tbody><tr>\n" +
				"      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
				"      <td>\n" +
				"        \n" +
				"                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
				"                  <tbody><tr>\n" +
				"                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
				"                  </tr>\n" +
				"                </tbody></table>\n" +
				"        \n" +
				"      </td>\n" +
				"      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
				"    </tr>\n" +
				"  </tbody></table>\n" +
				"\n" +
				"\n" +
				"\n" +
				"  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
				"    <tbody><tr>\n" +
				"      <td height=\"30\"><br></td>\n" +
				"    </tr>\n" +
				"    <tr>\n" +
				"      <td width=\"10\" valign=\"middle\"><br></td>\n" +
				"      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
				"        \n" +
				"            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Thank you for registering. Please click on the below link to activate your account: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">Activate Now</a> </p></blockquote>\n Link will expire in 60 minutes. <p>See you soon</p>" +
				"        \n" +
				"      </td>\n" +
				"      <td width=\"10\" valign=\"middle\"><br></td>\n" +
				"    </tr>\n" +
				"    <tr>\n" +
				"      <td height=\"30\"><br></td>\n" +
				"    </tr>\n" +
				"  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
				"\n" +
				"</div></div>";
	}



}
