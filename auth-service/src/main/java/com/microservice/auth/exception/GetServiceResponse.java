package com.microservice.auth.exception;

import lombok.Data;

@Data
public class GetServiceResponse {

	private boolean error;

	private ErrorCode errorCode;

	private String errorMessage;

	private Object data;
}
