package com.kyla.community.domain.user.controller;

import com.kyla.community.domain.user.service.UserService;
import com.kyla.community.domain.user.dto.req.SignupRequestDto;
import com.kyla.community.domain.user.dto.res.SignupResponseDto;
import com.kyla.community.global.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated // 메서드 파라미터 제약조건 검증
@RestController // 회원가입과 중복 확인 API 요청 처리
@RequestMapping("/users")
@RequiredArgsConstructor
public class SignUpController {
	private final UserService userService;

	// 신규 회원 등록
	@PostMapping
	public ResponseEntity<ApiResponse<SignupResponseDto>> signUp(@Valid @RequestBody SignupRequestDto request) {
		SignupResponseDto response = userService.signUp(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(HttpStatus.CREATED.value(), "회원가입 성공", response));
	}

	// 이메일 중복 확인
	@GetMapping("/email")
	public ResponseEntity<ApiResponse<Void>> checkEmail(@RequestParam @NotBlank @Email String email) {
		userService.validateEmailAvailable(email);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "사용 가능한 이메일입니다.", null));
	}

	// 닉네임 중복 확인
	@GetMapping("/nickname")
	public ResponseEntity<ApiResponse<Void>> checkNickname(@RequestParam @NotBlank String nickname) {
		userService.validateNicknameAvailable(nickname);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "사용 가능한 닉네임입니다.", null));
	}
}
