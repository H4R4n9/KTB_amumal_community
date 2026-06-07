package com.kyla.community.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 액세스 토큰 재발급용 리프레시 토큰 요청
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TokenRefreshRequestDto {
	@NotBlank(message = "refreshToken은 필수입니다.")
	private String refreshToken;
}
