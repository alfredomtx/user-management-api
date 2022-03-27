package com.user.core.api.controller;

import com.user.core.api.model.Email;
import com.user.core.api.model.dto.EmailDTO;
import com.user.core.api.service.impl.EmailServiceImplNew;
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

	@Autowired
	private EmailServiceImplNew emailService;

	@GetMapping("/")
	public ResponseEntity<List<EmailDTO>> getAll(){
		return ResponseEntity.ok(emailService.getAll());
	}

	/*@GetMapping("/")
	public ResponseEntity<Page<Email>> getAll(@PageableDefault(page = 0, size = 5, sort = "emailId", direction = Sort.Direction.DESC) Pageable pageable){
		return new ResponseEntity<>(emailService.findAll(pageable), HttpStatus.OK);
	}*/

	@PostMapping("/send")
	public ResponseEntity<EmailDTO> sendEmail(@RequestBody @Valid EmailDTO emailDTO) {
		Email email = new Email();
		BeanUtils.copyProperties(emailDTO, email);
		EmailDTO emailSent = emailService.sendEmail(email);

		return new ResponseEntity<>(emailSent, HttpStatus.CREATED);
	}


	/*@GetMapping("/{emailId}")
	public ResponseEntity<Object> getById(@PathVariable("emailId") Long emailId){
		Optional<Email> emailOptional = emailService.findById(emailId);
		if(emailOptional.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email with id [" + emailId + "]not found.");
		}
		return ResponseEntity.status(HttpStatus.OK).body(emailOptional.get());
	}*/
}