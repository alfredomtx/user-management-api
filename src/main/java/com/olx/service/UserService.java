package com.olx.service;

import com.olx.model.User;
import com.olx.model.dto.UserDTO;
import com.olx.model.dto.UserInsertDTO;
import com.olx.model.dto.UserUpdateDTO;

import java.util.List;


public interface UserService {

	UserDTO getById(Long id);
	
	UserDTO getByEmail(String email);

	List<UserDTO> getAll();

	UserDTO add(UserInsertDTO user);

	UserDTO update(Long id, UserUpdateDTO userRequest);

	void changePassword(Long id, String password);

	Boolean validateLogin(User userRequest);

	void delete(Long id);
}
