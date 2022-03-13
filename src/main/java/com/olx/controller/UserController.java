package com.olx.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.olx.controller.dto.UserPasswordDTO;
import com.olx.controller.dto.UserDTO;
import com.olx.controller.dto.UserInsertDTO;
import com.olx.exceptions.InvalidUserDataException;
import com.olx.exceptions.UserAlreadyExistsException;
import com.olx.model.User;
import com.olx.repository.UserRepository;
import com.olx.service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {

	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepo;
	private final UserService userService;

	public UserController(UserRepository userRepo, PasswordEncoder passwordEncoder, UserService userService) {
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
		this.userService = userService;
	}

	@GetMapping("/")
	public ResponseEntity<List<UserDTO>> getAll() {
		return ResponseEntity.ok(userService.getAll());
	}

	@GetMapping("/ping")
	public ResponseEntity<?> ping() {
		return ResponseEntity.ok().body("pong");
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getById(@PathVariable("id") Long id) {
		UserDTO user;
		try {
			user = userService.getById(id);
		} catch (EntityNotFoundException e) {
			return userNotFoundResponse(id);
		}

		return ResponseEntity.ok().body(user);
	}

	@PostMapping("/")
	public ResponseEntity<?> add(@RequestBody @Valid User userRequest) {

		UserDTO user;
		try {
			user = userService.add(userRequest);
		} catch (UserAlreadyExistsException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(respBody(e.getMessage()));
		}

		return ResponseEntity.ok(user);
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> update(@PathVariable("id") @NotBlank Long id,
			@Valid @RequestBody UserInsertDTO userRequest) {

		try {
			userService.update(id, userRequest);
		} catch (EntityNotFoundException e) {
			return userNotFoundResponse(id);
		} catch (InvalidUserDataException e) {
			return ResponseEntity.badRequest().body(respBody(e.getMessage()));
		} catch (ConstraintViolationException e) {
			return ResponseEntity.internalServerError().body(respBody(e.getMessage()));
		} catch (Exception e) {
			// e.printStackTrace();
			return ResponseEntity.badRequest().body(respBody(e.getMessage()));
		}

		return ResponseEntity.ok().build();
	}

	@PutMapping("/{id}/changePassword")
	public ResponseEntity<?> changePassword(@PathVariable("id") @NotBlank Long id,
			@Valid @RequestBody UserPasswordDTO password) {

		try {
			userService.changePassword(id, password);
		} catch (EntityNotFoundException e) {
			return userNotFoundResponse(id);
		}

		return ResponseEntity.ok().body(respBody("User password updated with success."));
	}

	@PostMapping("/validateLogin")
	public ResponseEntity<Boolean> validateLogin(@RequestBody Optional<User> userRequest) {
		if (userService.validateLogin(userRequest) == false) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
		}
		return ResponseEntity.ok().body(true);
	}

	// override bad request response to show the error messages in a cleaner and
	// better way
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Map<String, String> handleValidationException(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();

		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();

			errors.put(fieldName, errorMessage);
		});

		return errors;
	}

	public Map<String, String> respBody(String message) {
		Map<String, String> error = new HashMap<>();
		error.put("messsage", message);
		return error;
	}

	public ResponseEntity<?> userNotFoundResponse(Long id) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respBody("Use with id " + id + " not found."));
	}

}
