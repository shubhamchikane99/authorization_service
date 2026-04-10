package com.microservice.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.microservice.auth.entity.Users;

import java.util.Date;

@Component
public class JwtTokenProvider {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.access-token-expiration}")
	private long accessExpiration;

	@Value("${jwt.refresh-token-expiration}")
	private long refreshExpiration;

	private JwtParser getParser() {
		return Jwts.parser().setSigningKey(secret.getBytes()) // or better: Keys.hmacShaKeyFor(secret.getBytes())
				.build();
	}

	// ==================== Token Generation (unchanged) ====================
	public String generateAccessToken(Users user) {
		return generateToken(user, accessExpiration);
	}

	public String generateRefreshToken(Users user) {
		return generateToken(user, refreshExpiration);
	}

	private String generateToken(Users user, long expiration) {
		return Jwts.builder().setSubject(user.getUsername()).claim("tenantId", user.getTenantId())
				.claim("userId", user.getId().toString()).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + expiration))
				.signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS512).compact();
	}

	// ==================== NEW: Safe parsing without throwing on expiration
	// ====================
	/**
	 * Parses token and returns Claims even if expired. Only throws on signature
	 * mismatch or malformed token.
	 */
	private Claims getClaims(String token) {
		try {
			return getParser().parseSignedClaims(token).getPayload();
		} catch (ExpiredJwtException e) {
			// This is the key: return claims even if expired
			return e.getClaims();
		} catch (JwtException | IllegalArgumentException e) {
			// Signature invalid, malformed, etc.
			throw new RuntimeException("Invalid JWT token", e);
		}
	}

	// Extract userId (works even if token expired)
	public String getUserIdFromToken(String token) {
		try {
			Claims claims = getClaims(token);
			return claims.get("userId", String.class);
		} catch (Exception e) {
			return null;
		}
	}

	// Extract username
	public String getUsernameFromToken(String token) {
		try {
			Claims claims = getClaims(token);
			return claims.getSubject();
		} catch (Exception e) {
			return null;
		}
	}

	// Extract tenantId
	public String getTenantIdFromToken(String token) {
		try {
			Claims claims = getClaims(token);
			return claims.get("tenantId", String.class);
		} catch (Exception e) {
			return null;
		}
	}

	// Check if token is expired according to JWT (for reference only)
	public boolean isJwtExpired(String token) {
		try {
			Claims claims = getClaims(token);
			return claims.getExpiration().before(new Date());
		} catch (Exception e) {
			return true;
		}
	}

	// Validate token signature + username (we still keep this, but DB will be final
	// authority)
	public boolean validateToken(String token, UserDetails userDetails) {
		try {
			String tokenUsername = getUsernameFromToken(token);
			return tokenUsername != null && tokenUsername.equals(userDetails.getUsername());
			// We removed !isTokenExpired(token) because we trust DB now
		} catch (Exception e) {
			return false;
		}
	}
}