package com.microservice.auth.service;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.microservice.auth.entity.ErrorMessage;
import com.microservice.auth.entity.LoginRequest;
import com.microservice.auth.entity.Tokens;
import com.microservice.auth.entity.Users;
import com.microservice.auth.repository.TokensRepository;
import com.microservice.auth.repository.UsersRepository;
import com.microservice.auth.security.JwtTokenProvider;
import com.microservice.auth.util.TenantContext;

@Service
public class PublicService {

	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;
	private final UsersRepository userRepository;
	private final TokensRepository tokenRepository;
	private final PasswordEncoder passwordEncoder; // ← Added this

	public PublicService(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
			UsersRepository userRepository, TokensRepository tokenRepository, PasswordEncoder passwordEncoder) {
		this.authenticationManager = authenticationManager;
		this.jwtTokenProvider = jwtTokenProvider;
		this.userRepository = userRepository;
		this.tokenRepository = tokenRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public ErrorMessage login(String userName, String password) {

		ErrorMessage errorMessage = new ErrorMessage();
		errorMessage.setErrorMessage(password);
		errorMessage.setStatusCode(500);
		errorMessage.setError(true);

		// 1. Authenticate using Spring Security (This handles BCrypt automatically)

		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
		} catch (BadCredentialsException e) {
			errorMessage.setErrorMessage("Invalid username or password");
			errorMessage.setStatusCode(401);
			errorMessage.setError(true);
			return errorMessage;
		} catch (Exception e) {
			errorMessage.setErrorMessage("Authentication failed");
			errorMessage.setStatusCode(401);
			errorMessage.setError(true);
			return errorMessage;
		}

		// 2. Get user with tenant
		String tenantId = TenantContext.getCurrentTenant();
		Users user = userRepository.findByUsernameAndTenantId(userName, tenantId)
				.orElseThrow(() -> new RuntimeException("User not found"));

		// 3. Generate tokens
		String accessToken = jwtTokenProvider.generateAccessToken(user);
//		String refreshToken = jwtTokenProvider.generateRefreshToken(user);

		// 4. Save new tokens to DB
		saveToken(accessToken, user, jwtTokenProvider.getTenantIdFromToken(accessToken)); // 15 min

//		saveToken(refreshToken, user, jwtTokenProvider.getTenantIdFromToken(refreshToken),
//				System.currentTimeMillis() + 604800000L); // 7 days

		errorMessage.setErrorMessage("Login successful");
		errorMessage.setStatusCode(200);
		errorMessage.setError(false);
		errorMessage.setToken("Bearer " + accessToken);

		return errorMessage;
	}

	private void saveToken(String tokenStr, Users user, String tenantId) {
		Tokens token = new Tokens();
		token.setTenantKey(tokenStr);
		token.setUserId(user.getId());
		token.setExpiryDate(LocalDateTime.now().plusMinutes(15));

		tokenRepository.save(token);
	}

	public Object createAccount(LoginRequest loginRequest) {
		// create user

		String hashedPassword = passwordEncoder.encode(loginRequest.getPassword()); // BCrypt

		Users user = new Users();
		user.setUserName(loginRequest.getUsername());
		user.setPassword(hashedPassword); // ← Save HASHED password
		user.setTenantId("tenant1");
		// ... other fields
		userRepository.save(user);

		return null;
	}
}
