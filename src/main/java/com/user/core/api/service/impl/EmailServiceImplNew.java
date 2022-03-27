package com.user.core.api.service.impl;

import com.user.core.api.config.EmailConfigSourceFactory;
import com.user.core.api.enums.StatusEmail;
import com.user.core.api.model.Email;
import com.user.core.api.model.dto.EmailDTO;
import com.user.core.api.repository.EmailRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Component
@PropertySource(factory = EmailConfigSourceFactory.class, value = "email_configuration.yml")
public class EmailServiceImplNew {

	@Autowired
	private EmailRepository emailRepository;

	@Autowired
	private  ModelMapper mapper;

	@Value("${mail.sender.email}")
	private String senderEmail;
	@Value("${mail.sender.name}")
	private String senderName;
	@Value("${spring.mail.protocol}")
	private String protocol;
	@Value("${spring.mail.host}")
	private String host;
	@Value("${spring.mail.port}")
	private String port;
	@Value("${spring.mail.username}")
	private String username;
	@Value("${spring.mail.password}")
	private String password;

	private Properties props;


	public EmailDTO sendEmail(Email email) {

		Properties props = new Properties();
		props.put("mail.transport.protocol", protocol);
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", port);

		Session session = Session.getDefaultInstance(props, null);
		session.setDebug(false);

		email.setSendDate(LocalDateTime.now());
		try {
			InternetAddress iaFrom = new InternetAddress(senderEmail, senderName);
			InternetAddress[] iaTo = new InternetAddress[1];
			InternetAddress[] iaReplyTo = new InternetAddress[1];

			iaReplyTo[0] = new InternetAddress(email.getAddressTo(), email.getRecipientName());
			iaTo[0] = new InternetAddress(email.getAddressTo(), email.getRecipientName());

			MimeMessage msg = new MimeMessage(session);

			if (iaReplyTo != null)
				msg.setReplyTo(iaReplyTo);
			if (iaFrom != null)
				msg.setFrom(iaFrom);
			if (iaTo.length > 0)
				msg.setRecipients(Message.RecipientType.TO, iaTo);
			msg.setSubject(email.getSubject());
			msg.setSentDate(new Date());

			msg.setContent(email.getBody(), "text/html");

			Transport tr = session.getTransport(protocol);
			tr.connect(host, username, password);

			msg.saveChanges();

			tr.sendMessage(msg, msg.getAllRecipients());
			tr.close();
			email.setStatus(StatusEmail.SENT);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			email.setStatus(StatusEmail.ERROR);
			email.setErrorDetails(e.getMessage());
		} catch (MessagingException e) {
			e.printStackTrace();
			email.setStatus(StatusEmail.ERROR);
			email.setErrorDetails(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			email.setStatus(StatusEmail.ERROR);
			email.setErrorDetails("[" + e.getClass() + "] " + e.getMessage());
		}

		emailRepository.save(email);

		EmailDTO emailDTO = new EmailDTO();
		BeanUtils.copyProperties(email, emailDTO);
		return emailDTO;
	}

	public List<EmailDTO> getAll() {
		List<Email> emails = emailRepository.findAll();
		return emails.stream()
				.map((email) -> mapper.map(email, EmailDTO.class)).collect(Collectors.toList());
	}
}
