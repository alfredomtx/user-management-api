package com.user.api.user;

import com.user.api.user.model.User;
import com.user.api.user.model.UserRequestDTO;
import com.user.api.user.model.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;


public interface UserService {

	Page<UserResponseDTO> getAll(Pageable pageable);

	UserResponseDTO getById(Long id);

	UserResponseDTO getByEmail(String email);

	UserResponseDTO add(UserRequestDTO user);

	UserResponseDTO update(Map<String, String> fields);

	void delete(Long id);

	void changePassword(Map<String, String> fields);


	Boolean validateLogin(User userRequest);

}
