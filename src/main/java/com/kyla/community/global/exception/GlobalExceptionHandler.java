package com.kyla.community.global.exception;

import com.kyla.community.global.common.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice // API 예외의 공통 응답 변환
public class GlobalExceptionHandler {

	// 비즈니스 예외의 지정 HTTP 상태 응답
	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException exception) {
		HttpStatus status = exception.getStatus();
		return ResponseEntity
				.status(status)
				.body(ApiResponse.fail(status.value(), exception.getMessage()));
	}

	// 요청값 검증과 필수 헤더 오류의 400 응답
	@ExceptionHandler({
			MethodArgumentNotValidException.class,
			ConstraintViolationException.class,
			HandlerMethodValidationException.class,
			MissingRequestHeaderException.class,
			IllegalArgumentException.class
	})
	public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception exception) {
		return ResponseEntity
				.badRequest()
				.body(ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), exception.getMessage()));
	}

	// 처리되지 않은 예외의 500 응답
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception) {
		return ResponseEntity
				.internalServerError()
				.body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
	}
}
