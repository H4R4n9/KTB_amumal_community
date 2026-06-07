package com.kyla.community.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 이메일 인증 기반 비밀번호 재설정 요청
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PasswordResetDto {
	@NotBlank @Email
	private String email;
	@NotBlank
	private String verificationCode;
	@NotBlank
	@Pattern(
			regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,20}$",
			message = "비밀번호는 8자 이상 20자 이하이며, 대문자/소문자/숫자/특수문자를 각각 최소 1개 포함해야 합니다."
	)
	private String newPassword;
	@NotBlank
	private String newPasswordCheck;
}
