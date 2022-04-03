package com.user.api.user;

import com.user.api.user.model.User;
import com.user.api.user.model.UserRequestDTO;
import com.user.api.user.model.UserResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
	public static final String ID = "/{id}";
	public static final String EMAIL = "/{email}";

	@Autowired
	private UserService userService;

	@GetMapping("/")
	public ResponseEntity<Page<UserResponseDTO>> getAll(Pageable pageable) {
		return ResponseEntity.ok(userService.getAll(pageable));
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

	@PatchMapping(path = "/")
	public ResponseEntity<String> update(@RequestBody Map<String, String> fields) {
		userService.update(fields);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping(ID)
	public ResponseEntity<UserResponseDTO> delete(@PathVariable("id") Long id) {
		userService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/changePassword")
	public ResponseEntity<String> changePassword(@RequestBody Map<String, String> fields) {
		userService.changePassword(fields);
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
