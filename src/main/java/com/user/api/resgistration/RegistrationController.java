package com.user.api.resgistration;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {

	@Autowired
	private RegistrationService registrationService;

	@PostMapping("/requestActivateAccountEmail")
	public ResponseEntity<String> requestActivateAccountEmail(@RequestBody Map<String, String> fields) {
		registrationService.requestActivateAccountEmail(fields);
		return ResponseEntity.ok().body("An account activation link has been sent to the email.");
	}

	@GetMapping("/activateAccount")
	public ResponseEntity<String> activateAccount(@RequestParam("token") String token
			, @RequestParam("email") String email) {
		registrationService.activateAccount(token, email);
		return ResponseEntity.ok().body("Account activated with success.");
	}

}
