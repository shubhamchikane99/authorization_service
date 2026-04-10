package com.microservice.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/check")
public class Check {

	@GetMapping("/ok")
	public String helthCheck() { 

		return "helth is ok";
	}
}
