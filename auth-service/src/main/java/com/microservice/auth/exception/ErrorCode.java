package com.microservice.auth.exception;

public enum ErrorCode {
	NOT_FOUND("NOT FOUND"), UNAUTHORIZED("UNAUTHORIZED USER"), FORBIDDEN("ACCESS FORBIDDEN"),
	INTERNAL_SERVER_ERROR("INTERNAL SERVER ERROR"), EXPECTATION_FAILED("ENTITY PARAMETER ERROR"),
	RUNTIME_ERROR("Runtime Exception"), METHOD_NOT_ALLOWED("METHOD NOT ALLOWED");

	String message;

	ErrorCode(String message) {
		this.message = message;
	}
}