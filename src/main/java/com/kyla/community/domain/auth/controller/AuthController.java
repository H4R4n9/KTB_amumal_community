package com.kyla.community.domain.auth.controller;

import com.kyla.community.domain.auth.serevice.AuthService;
import com.kyla.community.domain.auth.dto.LoginRequestDto;
import com.kyla.community.domain.auth.dto.LoginResponseDto;
import com.kyla.community.domain.auth.dto.TokenRefreshRequestDto;
import com.kyla.community.domain.auth.dto.TokenRefreshResponseDto;
import com.kyla.community.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated // 메서드 파라미터 제약조건 검증
@RestController // 로그인과 토큰 API 요청 처리
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;

	// 로그인 및 액세스·리프레시 토큰 발급
	@PostMapping
	public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
		LoginResponseDto response = authService.login(request);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "로그인 성공", response));
	}

	// 리프레시 토큰 기반 액세스 토큰 재발급
	@PostMapping("/token")
	public ResponseEntity<ApiResponse<TokenRefreshResponseDto>> refreshAccessToken(
			@Valid @RequestBody TokenRefreshRequestDto request
	) {
		TokenRefreshResponseDto response = authService.refreshAccessToken(request);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "access token 재발급 성공", response));
	}

	// 리프레시 토큰 폐기를 통한 로그아웃
	@DeleteMapping
	public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody TokenRefreshRequestDto request) {
		authService.logout(request.getRefreshToken());
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "로그아웃 성공", null));
	}
}
