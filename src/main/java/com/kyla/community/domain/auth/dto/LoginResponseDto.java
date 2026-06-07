package com.kyla.community.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// 회원 정보와 JWT를 포함한 로그인 응답
@Getter
@AllArgsConstructor
public class LoginResponseDto {
	private Long userId;
	private String email;
	private String nickname;
	private String profileImagePath;
	private String accessToken;
	private String refreshToken;
	private String tokenType;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
