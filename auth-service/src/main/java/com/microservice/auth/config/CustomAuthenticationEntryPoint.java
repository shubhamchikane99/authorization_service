package com.microservice.auth.config;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.auth.exception.ErrorCode;
import com.microservice.auth.exception.ServiceResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {

		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401

		ServiceResponse errorResponse = ServiceResponse.asFailure(ErrorCode.UNAUTHORIZED,
				authException.getMessage() != null && !authException.getMessage().isEmpty() ? authException.getMessage()
						: "Invalid or missing authorization token");

		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}