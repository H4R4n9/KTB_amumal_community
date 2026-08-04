package com.kyla.community.domain.user.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 닉네임과 선택 프로필 이미지를 포함한 회원 수정 요청
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UpdateUserRequestDto {
	@NotBlank
	@Size(max = 10)
	private String nickname;
	private String profileImageObjectKey;
	private String profileImageContentType;
	private Long profileImageFileSize;
}
