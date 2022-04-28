package com.user.api.email;

import com.user.api.email.model.EmailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping(path = "/api/email"
		, produces = MediaType.APPLICATION_JSON_VALUE
		, consumes = MediaType.APPLICATION_JSON_VALUE
)
@RestController
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
		return ResponseEntity.ok().body(emailService.getById(id));
	}

	@GetMapping("/to/{emailAddress}")
	public ResponseEntity<Page<EmailDTO>> getByAddressTo(Pageable pageable, @PathVariable("emailAddress") String emailAddress) {
		return ResponseEntity.ok().body(emailService.getByAddressTo(pageable, emailAddress));
	}

	@PostMapping("/send")
	public ResponseEntity<EmailDTO> sendEmail(@RequestBody @Valid EmailDTO emailDto) {
		return new ResponseEntity<>(emailService.sendEmail(emailDto), HttpStatus.CREATED);
	}

	@PostMapping("/sendToQueue")
	public ResponseEntity<String> sendEmailToQueue(@RequestBody @Valid EmailDTO emailDto) {
		emailService.sendEmailToQueue(emailDto);
		return ResponseEntity.ok().build();
	}


}