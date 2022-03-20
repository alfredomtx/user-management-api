package com.user.core.api.service;

import com.user.core.api.model.User;
import com.user.core.api.model.dto.UserRequestDTO;
import com.user.core.api.model.dto.UserResponseDTO;

import java.util.List;
import java.util.Map;


public interface UserService {

	List<UserResponseDTO> getAll();

	UserResponseDTO getById(Long id);

	UserResponseDTO getByEmail(String email);

	UserResponseDTO add(UserRequestDTO user);

	UserResponseDTO update(Long id, Map<String, Object> fields);

	void delete(Long id);

	void changePassword(Long id, String password);

	Boolean validateLogin(User userRequest);
}
