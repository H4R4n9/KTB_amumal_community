package com.kyla.community.global.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

// 성공 여부와 HTTP 상태를 통일한 API 공통 응답
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {
	private boolean success;
	private int status;
	private String message;
	private T data;

	// 성공 응답 생성
	public static <T> ApiResponse<T> success(int status, String message, T data) {
		return new ApiResponse<>(true, status, message, data);
	}

	// 실패 응답 생성
	public static <T> ApiResponse<T> fail(int status, String message) {
		return new ApiResponse<>(false, status, message, null);
	}
}
