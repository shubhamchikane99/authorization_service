package com.microservice.auth.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.auth.entity.Tokens;
import com.microservice.auth.repository.TokensRepository;
import com.microservice.auth.util.TenantContext;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserDetailsService userDetailsService;
	private final TokensRepository tokenRepository;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService,
			TokensRepository tokenRepository) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.userDetailsService = userDetailsService;
		this.tokenRepository = tokenRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String path = request.getServletPath();
		if (path.startsWith("/public") || path.startsWith("/health")) {
			filterChain.doFilter(request, response);
			return;
		}

		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authHeader.substring(7);

		try {
			String userId = jwtTokenProvider.getUserIdFromToken(token);
			String tenantIdFromToken = jwtTokenProvider.getTenantIdFromToken(token);
			String username = jwtTokenProvider.getUsernameFromToken(token);

			if (userId == null || SecurityContextHolder.getContext().getAuthentication() != null) {
				filterChain.doFilter(request, response);
				return;
			}

			// Database token validation
			Optional<Tokens> tokenEntityOpt = tokenRepository.findByUserTokenByUserIdAndToken(userId, token);
			if (tokenEntityOpt.isEmpty()) {
				sendErrorResponse(response, "Token not found");
				return;
			}

//			Tokens tokenEntity = tokenEntityOpt.get();
//			if (!isTokenValidInDb(tokenEntity)) {
//				sendErrorResponse(response, "Token expired or revoked");
//				return;
//			}

			// Valid token → set authentication
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);

			if (jwtTokenProvider.validateToken(token, userDetails)) {
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
						null, userDetails.getAuthorities());
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authToken);

				TenantContext.setCurrentTenant(tenantIdFromToken);
			}

			filterChain.doFilter(request, response);

		} catch (ExpiredJwtException ex) {
			sendErrorResponse(response, "Token expired");
			return;
		} catch (Exception ex) {
			sendErrorResponse(response, "Invalid token");
			return;
		}
	}

	private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json");

		// Create response exactly as you want
		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("error", false); // as per your requirement
		errorResponse.put("message", message); // pass the actual message (not null)
		errorResponse.put("statusCode", 401);

		ObjectMapper mapper = new ObjectMapper();
		String jsonResponse = mapper.writeValueAsString(errorResponse);

		response.getWriter().write(jsonResponse);
		response.getWriter().flush();
	}

//	private boolean isTokenValidInDb(Tokens tokenEntity) {
//		if (tokenEntity.getIsActive() != 1) {
//			return false;
//		}
//		if (tokenEntity.getExpiryDate() != null) {
//			return tokenEntity.getExpiryDate().isAfter(LocalDateTime.now());
//		}
//		return true;
//	}
}