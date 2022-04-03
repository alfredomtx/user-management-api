package com.user.api.email;

import com.user.api.email.model.Email;
import com.user.api.email.model.EmailDTO;
import com.user.api.enums.StatusEmail;
import com.user.api.exceptions.EmailNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

@Service
public class EmailServiceImpl implements EmailService {

	@Autowired
	private EmailRepository emailRepository;

	@Autowired
	private ModelMapper mapper;

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
	@Value("${spring.mail.sender_email}")
	private String senderEmail;
	@Value("${spring.mail.sender_name}")
	private String senderName;

	@Override
	public Page<EmailDTO> getAll(Pageable pageable) {
		Page<Email> emails = emailRepository.findAll(pageable);
		Page<EmailDTO> emailsList = emails.map(email -> mapper.map(email, EmailDTO.class));
		return emailsList;
	}

	@Override
	public EmailDTO getById(Long id) {
		Email email = emailRepository.findById(id).orElseThrow(() -> new EmailNotFoundException(id));
		return mapper.map(email, EmailDTO.class);
	}

	@Override
	public List<EmailDTO> getByAddressTo(String emailAddress) {
		List<Email> emails = emailRepository.findByAddressTo(emailAddress);
		return emails.stream()
				.map((email) -> mapper.map(email, EmailDTO.class)).collect(Collectors.toList());
	}

	@Override
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
			msg.setReplyTo(iaReplyTo);
			msg.setFrom(iaFrom);
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

}
