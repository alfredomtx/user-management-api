package com.olx.service.impl;

import com.olx.data.UserDetailData;
import com.olx.model.User;
import com.olx.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserDetailServiceImpl implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		Optional<User> user = userRepository.findByEmail(username);
		if (user.isEmpty()) {
			throw new UsernameNotFoundException("User [" + username + "] not found.");
		}
		return new UserDetailData(user);
	}

}
