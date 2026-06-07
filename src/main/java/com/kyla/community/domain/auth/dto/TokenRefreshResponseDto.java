package com.kyla.community.domain.auth.dto;

// 재발급된 액세스 토큰 응답
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenRefreshResponseDto {
	private String accessToken;
	private String tokenType;
}
