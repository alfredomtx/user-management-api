package com.user.core.api.service.impl;

import com.user.core.api.data.UserDetailData;
import com.user.core.api.model.User;
import com.user.core.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> user = userRepository.findByEmail(username);
		if (user.isEmpty()) {
			throw new UsernameNotFoundException("User [" + username + "] not found.");
		}
		return new UserDetailData(user.get());
	}

}
