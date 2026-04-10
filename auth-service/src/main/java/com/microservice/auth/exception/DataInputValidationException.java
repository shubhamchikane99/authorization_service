package com.microservice.auth.exception;

public class DataInputValidationException extends Exception {

	public DataInputValidationException(final String msg) {
		super(msg);
	}

	public DataInputValidationException() {
		super();
	}
}
