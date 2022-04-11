package com.user.api.user;

import com.user.api.user.model.UserRequestDTO;
import com.user.api.user.model.UserResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.net.URI;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping("/api")
public class UserController {
	public static final String API_URL = "/user";
	public static final String ID = "/{id}";

	@Autowired
	private UserService userService;

	@GetMapping(API_URL  + "/all")
	public ResponseEntity<Page<UserResponseDTO>> getAll(Pageable pageable) {
		return ResponseEntity.ok(userService.getAll(pageable));
	}

	@GetMapping(API_URL)
	public ResponseEntity<UserResponseDTO> getByEmailOrId(@RequestBody Map<String, String> fields) {
		UserResponseDTO user = userService.getByIdOrEmail(fields);
		return ResponseEntity.ok().body(user);
	}

	@GetMapping(API_URL + ID)
	public ResponseEntity<UserResponseDTO> getById(@PathVariable("id") Long id) {
		UserResponseDTO user = userService.getById(id);
		return ResponseEntity.ok().body(user);
	}

	@PostMapping(API_URL)
	public ResponseEntity<String> add(@RequestBody @Valid UserRequestDTO userRequest) {
		UserResponseDTO user = userService.add(userRequest);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(user.getId()).toUri();
		return ResponseEntity.created(uri).build();
	}

	@PatchMapping(API_URL + ID)
	public ResponseEntity<String> update(@PathVariable("id") Long id, @RequestBody Map<String, String> fields) {
		fields.put("id", String.valueOf(id));
		userService.update(fields);
		return ResponseEntity.ok().build();
	}

	@PatchMapping(API_URL)
	public ResponseEntity<String> updateByEmailOrId(@RequestBody Map<String, String> fields) {
		userService.update(fields);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping(API_URL)
	public ResponseEntity<UserResponseDTO> delete(@RequestBody Map<String, String> fields) {
		userService.deleteByIdOrEmail(fields);
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

	// TODO implement refresh token and refactor JWT Authorization using JWTTokenUtil.java
	@GetMapping("/token/refresh")
	public ResponseEntity<?>refreshToken(HttpServletRequest request, HttpServletResponse response){
		String authorizationHeader = request.getHeader(AUTHORIZATION);
		return ResponseEntity.ok().body("test");
	}


}
