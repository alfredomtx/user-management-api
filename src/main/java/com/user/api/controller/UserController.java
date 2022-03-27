package com.user.api.controller;

import com.user.api.service.UserService;
import com.user.api.model.User;
import com.user.api.model.dto.UserRequestDTO;
import com.user.api.model.dto.UserResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
	public static final String ID = "/{id}";

	@Autowired
	private UserService userService;

	@GetMapping("/")
	public ResponseEntity<List<UserResponseDTO>> getAll() {
		return ResponseEntity.ok(userService.getAll());
	}

	@GetMapping(ID)
	public ResponseEntity<UserResponseDTO> getById(@PathVariable("id") Long id) {
		UserResponseDTO user = userService.getById(id);
		return ResponseEntity.ok().body(user);
	}

	@PostMapping("/")
	public ResponseEntity<String> add(@RequestBody @Valid UserRequestDTO userRequest) {
		UserResponseDTO user = userService.add(userRequest);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(user.getId()).toUri();
		return ResponseEntity.created(uri).build();
	}

	@PatchMapping(path = ID)
	public ResponseEntity<String> update(@PathVariable("id") @NotBlank Long id,
										 @RequestBody Map<String, Object> fields) {
		userService.update(id, fields);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping(ID)
	public ResponseEntity<UserResponseDTO> delete(@PathVariable("id") Long id) {
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
