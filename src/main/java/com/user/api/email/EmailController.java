package com.user.api.email;

import com.user.api.email.model.EmailDTO;
import com.user.api.email.model.Email;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/email")
public class EmailController {
	public static final String ID = "/id/{id}";

	@Autowired
	private EmailService emailService;


	@GetMapping("/")
	public ResponseEntity<Page<EmailDTO>> getAll(Pageable pageable) {
		return ResponseEntity.ok(emailService.getAll(pageable));
	}

	@GetMapping(ID)
	public ResponseEntity<EmailDTO> getById(@PathVariable("id") Long id) {
		EmailDTO email = emailService.getById(id);
		return ResponseEntity.ok().body(email);
	}

	@GetMapping("/to/{emailAddress}")
	public ResponseEntity<Page<EmailDTO>> getByAddressTo(Pageable pageable, @PathVariable("emailAddress") String emailAddress) {
		return ResponseEntity.ok().body(emailService.getByAddressTo(pageable, emailAddress));
	}

	@PostMapping("/send")
	public ResponseEntity<EmailDTO> sendEmail(@RequestBody @Valid EmailDTO emailDTO) {
		Email email = new Email();
		BeanUtils.copyProperties(emailDTO, email);
		EmailDTO emailSent = emailService.sendEmail(email);
		return new ResponseEntity<>(emailSent, HttpStatus.CREATED);
	}


}