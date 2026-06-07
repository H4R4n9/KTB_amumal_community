package com.kyla.community.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 신규 회원 식별자 응답
@Getter
@AllArgsConstructor
public class SignupResponseDto {
	private Long userId;
}
