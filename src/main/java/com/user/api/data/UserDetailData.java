package com.user.api.data;

import com.user.api.enums.Role;
import com.user.api.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserDetailData implements UserDetails {

	private final User user;

	public UserDetailData(User user) {
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> listRole = new ArrayList<GrantedAuthority>();

		String role = user.getRole().name();
		// assign default role in case it's empty, otherwise exception will be thrown by spring
		if (role.trim().isEmpty())
			role = Role.ROLE_USER.name();

		listRole.add(new SimpleGrantedAuthority(role));
		return listRole;
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled() {
		return user.isActive();
	}

}
