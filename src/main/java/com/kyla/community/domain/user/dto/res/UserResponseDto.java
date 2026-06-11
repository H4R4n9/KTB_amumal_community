package com.kyla.community.domain.user.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// 프로필 이미지와 생성·수정 시각을 포함한 회원 응답
@Getter
@AllArgsConstructor
public class UserResponseDto {
	private Long userId;
	private String email;
	private String nickname;
	private String profileImagePath;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
