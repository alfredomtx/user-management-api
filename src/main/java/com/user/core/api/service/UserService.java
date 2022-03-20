package com.user.core.api.service;

import com.user.core.api.model.User;
import com.user.core.api.model.dto.UserResponse;
import com.user.core.api.model.dto.UserInsertDTO;
import com.user.core.api.model.dto.UserUpdateDTO;

import java.util.List;


public interface UserService {

	List<UserResponse> getAll();
	UserResponse getById(Long id);
	UserResponse getByEmail(String email);
	UserResponse add(UserInsertDTO user);
	UserResponse update(Long id, UserUpdateDTO userRequest);
	void delete(Long id);
	void changePassword(Long id, String password);
	Boolean validateLogin(User userRequest);
}
