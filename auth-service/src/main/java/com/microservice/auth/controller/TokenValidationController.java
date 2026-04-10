package com.microservice.auth.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservice.auth.entity.Tokens;
import com.microservice.auth.repository.TokensRepository;
import com.microservice.auth.security.JwtTokenProvider;

@RestController
@RequestMapping("/public/token")
public class TokenValidationController {

	private final TokensRepository tokenRepository;
	private final JwtTokenProvider jwtTokenProvider;

	public TokenValidationController(TokensRepository tokenRepository, JwtTokenProvider jwtTokenProvider) {
		this.tokenRepository = tokenRepository;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@PostMapping("/validate")
	public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("valid", false, "message", "Missing or invalid Authorization header"));
		}

		String token = authHeader.substring(7);

		try {
			String userId = jwtTokenProvider.getUserIdFromToken(token);
			String tenantId = jwtTokenProvider.getTenantIdFromToken(token);
			String username = jwtTokenProvider.getUsernameFromToken(token);

			if (userId == null || username == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("valid", false, "message", "Invalid token"));
			}

			Optional<Tokens> tokenOpt = tokenRepository.findByUserTokenByUserIdAndToken(userId, token);

			if (tokenOpt.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("valid", false, "message", "Token not found in database"));
			}

			Tokens tokenEntity = tokenOpt.get();

			if (tokenEntity.getIsActive() != 1) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("valid", false, "message", "Token is inactive"));
			}

			if (tokenEntity.getExpiryDate() != null && tokenEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("valid", false, "message", "Token expired in database"));
			}

			return ResponseEntity.ok(Map.of("valid", true, "userId", userId, "tenantId", tenantId, "tenantKey",
					tokenEntity.getTenantKey() != null ? tokenEntity.getTenantKey() : tenantId, "username", username));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("valid", false, "message", "Token validation failed: " + e.getMessage()));
		}
	}
}
