package com.kyla.community.domain.user.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 계정 정보와 선택 프로필 이미지를 포함한 회원가입 요청
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SignupRequestDto {
	@NotBlank @Email
	private String email;
	@NotBlank
	@Pattern(
			regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,20}$",
			message = "비밀번호는 8자 이상 20자 이하이며, 대문자/소문자/숫자/특수문자를 각각 최소 1개 포함해야 합니다."
	)
	private String password;
	@NotBlank
	@Size(max = 10)
	private String nickname;
	private String profileImageObjectKey;
}
