package com.kyla.community.global.exception;

import org.springframework.http.HttpStatus;

// HTTP 상태와 메시지를 포함한 비즈니스 예외
public class ApiException extends RuntimeException {
	private final HttpStatus status;

	public ApiException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
