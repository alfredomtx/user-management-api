package com.user.api.user;

import java.net.URI;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.user.api.user.model.UserRequestDTO;
import com.user.api.user.model.UserResponseDTO;

@RequestMapping(path = "/api"
		, produces = MediaType.APPLICATION_JSON_VALUE
		, consumes = MediaType.APPLICATION_JSON_VALUE
)
@RestController
public class UserController {
	public static final String API_URL = "/user";
	public static final String ID = "/{id}";

	@Autowired
	private UserService userService;

	@GetMapping(API_URL  + "/all")
	public ResponseEntity<Page<UserResponseDTO>> getAll(Pageable pageable) {
		return ResponseEntity.ok(userService.getAll(pageable));
	}

	@GetMapping(value = {API_URL, API_URL + ID})
	public ResponseEntity<UserResponseDTO> getByEmailOrIdQuery(@RequestParam Map<String, String> pathVarsMap) {
		return ResponseEntity.ok().body(userService.getByIdOrEmail(pathVarsMap));
	}

	@PostMapping(API_URL)
	public ResponseEntity<String> add(@RequestBody @Valid UserRequestDTO userRequest) {
		UserResponseDTO user = userService.add(userRequest);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(user.getId()).toUri();
		return ResponseEntity.created(uri).build();
	}

	@PostMapping(API_URL + "/admin")
	public ResponseEntity<String> addUserAlreadyActive(@RequestBody @Valid UserRequestDTO userRequest) {
		UserResponseDTO user = userService.addUserAlreadyActive(userRequest);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(user.getId()).toUri();
		return ResponseEntity.created(uri).build();
	}

	@PatchMapping(API_URL)
	public ResponseEntity<String> updateByEmailOrId(@RequestBody Map<String, String> fields) {
		userService.update(fields);
		return ResponseEntity.ok().build();
	}

	@PatchMapping(API_URL + ID)
	public ResponseEntity<String> update(@PathVariable("id") Long id, @RequestBody Map<String, String> fields) {
		fields.put("id", String.valueOf(id));
		userService.update(fields);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping(API_URL)
	public ResponseEntity<UserResponseDTO> deleteByEmailOrId(@RequestBody Map<String, String> fields) {
		userService.delete(fields);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping(API_URL + ID)
	public ResponseEntity<UserResponseDTO> delete(@PathVariable("id") Long id) {
		userService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping(API_URL + "/changePassword")
	public ResponseEntity<String> changePassword(@RequestBody Map<String, String> fields) {
		userService.changeCurrentPassword(fields);
		return ResponseEntity.ok().body("User password changed with success.");
	}

	@PostMapping(API_URL + "/requestResetPassword")
	public ResponseEntity<String> requestResetPassword(@RequestBody Map<String, String> fields) {
		userService.requestResetPasswordEmail(fields);
		return ResponseEntity.ok().body("A reset password confirmation link has been sent to the email.");
	}

	@GetMapping(API_URL + "/resetPassword")
	public ResponseEntity<String> resetPassword(@RequestParam("token") String token) {
		userService.resetPassword(token);
		return ResponseEntity.ok().body("User password reset with success, a new password has been sent to the email.");
	}

}
