package com.user.core.api.controller;

import com.user.core.api.model.User;
import com.user.core.api.model.dto.UserResponse;
import com.user.core.api.model.dto.UserInsertDTO;
import com.user.core.api.model.dto.UserUpdateDTO;
import com.user.core.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
	public static final String ID = "/{id}";

	@Autowired
	private UserService userService;


	@GetMapping("/")
	public ResponseEntity<List<UserResponse>> getAll() {
		return ResponseEntity.ok(userService.getAll());
	}

	@GetMapping(ID)
	public ResponseEntity<UserResponse> getById(@PathVariable("id") Long id) {
		UserResponse user = userService.getById(id);
		return ResponseEntity.ok().body(user);
	}

	@PostMapping("/")
	public ResponseEntity<String> add(@RequestBody @Valid UserInsertDTO userRequest) {
		UserResponse user = userService.add(userRequest);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(user.getId()).toUri();
		return ResponseEntity.created(uri).build();
	}

	@PutMapping(ID)
	public ResponseEntity<String> update(@PathVariable("id") @NotBlank Long id,
										 @Valid @RequestBody UserUpdateDTO userRequest) {

		userService.update(id, userRequest);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping(ID)
	public ResponseEntity<UserResponse> delete(@PathVariable("id") Long id) {
		userService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping(ID + "/changePassword")
	public ResponseEntity<String> changePassword(@PathVariable("id") @NotBlank Long id,
												 @RequestBody String password) {

		userService.changePassword(id, password);
		return ResponseEntity.ok().body("User password updated with success.");
	}

	@PostMapping("/validateLogin")
	public ResponseEntity<Boolean> validateLogin(@RequestBody User userRequest) {
		if (!userService.validateLogin(userRequest)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
		}
		return ResponseEntity.ok().body(true);
	}

}
