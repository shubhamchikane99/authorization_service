package com.microservice.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse {

	private boolean error;

	private ErrorCode errorCode; // ← fixed (was errorcode)

	private String errorMessage;

	private Object data;

	public static ServiceResponse asFailure(ErrorCode code, String errorMessage) {
		return ServiceResponse.builder().data(null).errorCode(code).error(true).errorMessage(errorMessage).build();
	}

	public static ServiceResponse asSuccess(Object data) {
		return ServiceResponse.builder().data(data).error(false).errorCode(null).build();
	}
}