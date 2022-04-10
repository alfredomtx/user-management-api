package com.user.api.userProperties;

import com.user.api.exceptions.ObjectFieldsValidationException;
import com.user.api.user.model.User;
import com.user.api.userProperties.model.UserProperties;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;


@Service
public class UserPropertiesService {

	@Autowired
	private UserPropertiesRepository userPropsRepo;
	@Autowired
	private Validator validator;

	public UserProperties getUserProperties(User user){

		UserProperties userProps = user.getUserProperties();

		if (userProps == null){
			userProps = new UserProperties();
			userProps.setUser(user);
			saveUserProperties(userProps);
		}

		return userProps;
	}

	public void saveUserProperties(UserProperties userProperties){
		validateUserPropertiesData(userProperties);

		userPropsRepo.save(userProperties);
	}

	@SneakyThrows
	private void validateUserPropertiesData(UserProperties userProperties)  {
		BindingResult result = new BeanPropertyBindingResult(userProperties, "userProperties");

		validator.validate(userProperties, result);

		if (result.hasErrors()) {
			throw new ObjectFieldsValidationException(result.getFieldErrors());
		}
	}
}
