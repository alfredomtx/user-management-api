package com.user.api.controller;

import com.user.api.model.Email;
import com.user.api.model.dto.EmailDTO;
import com.user.api.service.impl.EmailServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/email")
public class EmailController {
	public static final String ID = "/id/{id}";

	@Autowired
	private EmailServiceImpl emailService;


	@GetMapping("/")
	public ResponseEntity<List<EmailDTO>> getAll() {
		return ResponseEntity.ok(emailService.getAll());
	}

	@GetMapping(ID)
	public ResponseEntity<EmailDTO> getById(@PathVariable("id") Long id) {
		EmailDTO email = emailService.getById(id);
		return ResponseEntity.ok().body(email);
	}

	@GetMapping("/to/{emailAddress}")
	public ResponseEntity<List<EmailDTO>> getByAddressTo(@PathVariable("emailAddress") String emailAddress) {
		return ResponseEntity.ok().body(emailService.getByAddressTo(emailAddress));
	}

	@PostMapping("/send")
	public ResponseEntity<EmailDTO> sendEmail(@RequestBody @Valid EmailDTO emailDTO) {
		Email email = new Email();
		BeanUtils.copyProperties(emailDTO, email);
		EmailDTO emailSent = emailService.sendEmail(email);

		return new ResponseEntity<>(emailSent, HttpStatus.CREATED);
	}


}