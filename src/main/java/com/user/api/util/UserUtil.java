package com.user.api.util;

import com.user.api.exceptions.InvalidUserDataException;
import com.user.api.exceptions.UserNotFoundException;
import com.user.api.user.UserRepository;
import com.user.api.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class UserUtil {

	@Autowired
	private static UserRepository userRepo;

	public static User getUserObjectByIdOrEmailFromFields(Map<String, String> fields){
		String email = fields.get("email");
		String idString = fields.get("id");

		if (email == null && idString == null)
			throw new InvalidUserDataException("[email] or [id] field must be set.");

		User user = null;
		if (email != null) {
			user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
		} else if (idString != null){
			Long id;
			try {
				id = Long.valueOf(idString);
			} catch (Exception e){
				throw new InvalidUserDataException("[id] field is invalid.");
			}
			user = userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
		}
		return user;
	}

}
