package com.microservice.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservice.auth.entity.LoginRequest;
import com.microservice.auth.exception.ServiceResponse;
import com.microservice.auth.service.PublicService;

@RestController
@RequestMapping("/public")
public class PublicController {

	private final PublicService authService;

	public PublicController(PublicService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public ServiceResponse login(@RequestBody LoginRequest loginRequest) {

		return ServiceResponse.asSuccess(authService.login(loginRequest.getUsername(), loginRequest.getPassword()));
	}

	@PostMapping("/create")
	public ServiceResponse createAccount(@RequestBody LoginRequest loginRequest) {

		return ServiceResponse.asSuccess(authService.createAccount(loginRequest));
	}
}