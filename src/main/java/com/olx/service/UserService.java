package com.olx.service;

import com.olx.controller.dto.UserDTO;
import com.olx.controller.dto.UserInsertDTO;
import com.olx.controller.dto.UserPasswordDTO;
import com.olx.exceptions.InvalidUserDataException;
import com.olx.exceptions.UserAlreadyExistsException;
import com.olx.model.User;

import java.util.List;
import java.util.Optional;


public interface UserService {

	UserDTO getById(Long id);
	
	UserDTO getByEmail(String email);

	List<UserDTO> getAll();

	UserDTO add(User user) throws UserAlreadyExistsException;

	void update(Long id, UserInsertDTO userRequest) throws InvalidUserDataException, UserAlreadyExistsException;

	void changePassword(Long id, UserPasswordDTO password);

	Boolean validateLogin(Optional<User> userRequest);
}
