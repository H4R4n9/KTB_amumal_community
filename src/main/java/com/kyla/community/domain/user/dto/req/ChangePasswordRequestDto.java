package com.kyla.community.domain.user.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 새 비밀번호와 확인값을 포함한 변경 요청
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChangePasswordRequestDto {
	@NotBlank
	@Pattern(
			regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,20}$",
			message = "비밀번호는 8자 이상 20자 이하이며, 대문자/소문자/숫자/특수문자를 각각 최소 1개 포함해야 합니다."
	)
	private String newPassword;
	@NotBlank
	private String newPasswordCheck;
}
