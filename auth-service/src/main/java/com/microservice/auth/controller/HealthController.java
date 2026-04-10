package com.microservice.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservice.auth.exception.ServiceResponse;

@RestController
@RequestMapping("/health")
public class HealthController {

	@GetMapping("/ok")
	public ServiceResponse helthCheck() {

		return ServiceResponse.asSuccess("helth is ok");
	}
}
