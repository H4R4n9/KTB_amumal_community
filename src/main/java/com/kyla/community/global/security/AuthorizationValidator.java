package com.kyla.community.global.security;

import com.kyla.community.global.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component // 리소스 소유자 기반 접근 권한 검증
public class AuthorizationValidator {

	// 리소스 소유자와 로그인 회원의 일치 여부 검증
	public void validateOwner(Long ownerId, Long loginUserId) {
		if (!Objects.equals(ownerId, loginUserId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "해당 리소스에 접근 권한이 없습니다.");
		}
	}
}
