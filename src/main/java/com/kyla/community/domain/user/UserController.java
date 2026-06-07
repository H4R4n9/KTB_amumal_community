package com.kyla.community.domain.user;

import com.kyla.community.domain.user.dto.*;
import com.kyla.community.global.common.ApiResponse;
import com.kyla.community.global.filter.JwtAuthFilter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Validated // 메서드 파라미터 제약조건 검증
@RestController // JSON 기반 회원 API 요청 처리
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	// 로그인 본인의 회원정보 조회
	@GetMapping("/{userId}")
	public ResponseEntity<ApiResponse<UserResponseDto>> getUser(
			@PathVariable Long userId,
			@RequestAttribute(JwtAuthFilter.LOGIN_USER_ID_ATTRIBUTE) Long loginUserId
	) {
		UserResponseDto response = userService.getUser(userId, loginUserId);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "회원정보 조회 성공", response));
	}

	// 회원 프로필 정보 수정
	@PutMapping("/{userId}")
	public ResponseEntity<ApiResponse<Void>> updateProfile(
			@PathVariable Long userId,
			@RequestAttribute(JwtAuthFilter.LOGIN_USER_ID_ATTRIBUTE) Long loginUserId,
			@Valid @RequestBody UpdateUserRequestDto request
	) {
		userService.updateProfile(userId, loginUserId, request);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "회원정보 수정 성공", null));
	}

	// 로그인 회원의 비밀번호 변경
	@PatchMapping("/password")
	public ResponseEntity<ApiResponse<Void>> changePassword(
			@RequestAttribute(JwtAuthFilter.LOGIN_USER_ID_ATTRIBUTE) Long userId,
			@Valid @RequestBody ChangePasswordRequestDto request
	) {
		userService.changePassword(userId, request);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "비밀번호 수정 성공", null));
	}

	// 회원 프로필 이미지 업로드
	@PostMapping(value = "/{userId}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<ProfileUploadResponseDto>> uploadProfileImage(
			@PathVariable Long userId,
			@RequestAttribute(JwtAuthFilter.LOGIN_USER_ID_ATTRIBUTE) Long loginUserId,
			@RequestPart("profileImage") MultipartFile profileImage
	) {
		ProfileUploadResponseDto response = userService.uploadProfileImage(userId, loginUserId, profileImage);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(HttpStatus.CREATED.value(), "파일 업로드 성공", response));
	}

	// 회원 계정의 소프트 삭제
	@DeleteMapping("/{userId}")
	public ResponseEntity<ApiResponse<Void>> deleteUser(
			@PathVariable Long userId,
			@RequestAttribute(JwtAuthFilter.LOGIN_USER_ID_ATTRIBUTE) Long loginUserId
	) {
		userService.delete(userId, loginUserId);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "회원정보 삭제 성공", null));
	}
}
