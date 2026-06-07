package com.kyla.community.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 프로필 이미지 업로드 경로 응답
@Getter
@AllArgsConstructor
public class ProfileUploadResponseDto {
	private String filePath;
}
