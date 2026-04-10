package com.microservice.auth.entity;

import lombok.Data;

@Data
public class ErrorMessage {

	private boolean error;

	private int statusCode;

	private String errorMessage;

	private String token;
}
