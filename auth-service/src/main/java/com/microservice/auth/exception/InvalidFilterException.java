package com.microservice.auth.exception;

public class InvalidFilterException extends Exception {

	public InvalidFilterException() {
		super();
	}

	public InvalidFilterException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidFilterException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidFilterException(String message) {
		super(message);
	}

	public InvalidFilterException(Throwable cause) {
		super(cause);
	}

}
