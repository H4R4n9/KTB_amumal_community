package com.kyla.community.domain.auth;

import com.kyla.community.domain.auth.dto.PasswordResetDto;
import com.kyla.community.domain.auth.dto.VerificationCodeRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated // 메서드 파라미터 제약조건 검증
@RestController // 비밀번호 재설정 API 요청 처리
@RequestMapping("/auth")
@RequiredArgsConstructor
public class PasswordResetController {
	private final PasswordResetService passwordResetService;

	// 가입 이메일 존재 여부 확인
	@GetMapping("/emails/{email}/existence")
	public void checkEmailExists(@PathVariable @NotBlank @Email String email) {
		passwordResetService.checkEmailExists(email);
	}

	// 비밀번호 재설정용 인증번호 발급
	@PostMapping("/verification-code")
	@ResponseStatus(HttpStatus.CREATED)
	public void sendVerificationCode(@Valid @RequestBody VerificationCodeRequestDto request) {
		passwordResetService.sendVerificationCode(request);
	}

	// 이메일 인증 기반 비밀번호 재설정
	@PatchMapping("/password")
	public void resetPassword(@Valid @RequestBody PasswordResetDto request) {
		passwordResetService.resetPassword(request);
	}
}
