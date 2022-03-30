package com.user.api.service;

import com.user.api.model.User;
import com.user.api.model.dto.UserRequestDTO;
import com.user.api.model.dto.UserResponseDTO;

import java.util.List;
import java.util.Map;


public interface UserService {

	List<UserResponseDTO> getAll();

	UserResponseDTO getById(Long id);

	UserResponseDTO getByEmail(String email);

	UserResponseDTO add(UserRequestDTO user);

	UserResponseDTO update(Map<String, String> fields);

	void delete(Long id);

	void changePassword(Map<String, String> fields);


	Boolean validateLogin(User userRequest);

}
