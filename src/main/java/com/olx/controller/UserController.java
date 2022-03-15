package com.olx.controller;

import com.olx.model.User;
import com.olx.model.dto.UserDTO;
import com.olx.model.dto.UserInsertDTO;
import com.olx.model.dto.UserUpdateDTO;
import com.olx.service.UserService;
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
	public ResponseEntity<List<UserDTO>> getAll() {
		return ResponseEntity.ok(userService.getAll());
	}

	@GetMapping(ID)
	public ResponseEntity<UserDTO> getById(@PathVariable("id") Long id) {
		UserDTO user = userService.getById(id);
		return ResponseEntity.ok().body(user);
	}

	@PostMapping("/")
	public ResponseEntity<UserDTO> add(@RequestBody @Valid UserInsertDTO userRequest) {
		UserDTO user = userService.add(userRequest);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(user.getId()).toUri();
		return ResponseEntity.created(uri).build();
	}

	@PutMapping(ID)
	public ResponseEntity<String> update(@PathVariable("id") @NotBlank Long id,
			@Valid @RequestBody UserUpdateDTO userRequest) {

		userService.update(id, userRequest);
		return ResponseEntity.ok().build();
	}

	@PutMapping(ID + "/changePassword")
	public ResponseEntity<String> changePassword(@PathVariable("id") @NotBlank Long id,
			@Valid @RequestBody String password) {

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

	@DeleteMapping(ID)
	public ResponseEntity<UserDTO> delete(@PathVariable("id") Long id) {
		userService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
