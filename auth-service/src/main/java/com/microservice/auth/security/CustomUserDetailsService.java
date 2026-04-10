package com.microservice.auth.security;

import com.microservice.auth.entity.Users;
import com.microservice.auth.repository.UsersRepository;
import com.microservice.auth.util.TenantContext;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UsersRepository usersRepository;

	public CustomUserDetailsService(UsersRepository usersRepository) {
		this.usersRepository = usersRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		// Fetch user from central auth DB
		Users userEntity = usersRepository.findByUserName(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

		TenantContext.setCurrentTenant(userEntity.getTenantId());

		// Convert to Spring Security UserDetails
		return org.springframework.security.core.userdetails.User.builder().username(userEntity.getUsername())
				.password(userEntity.getPassword()) // Must be BCrypt hash
				.authorities("ROLE_USER") // adjust if you have roles
				.build();
	}
}
