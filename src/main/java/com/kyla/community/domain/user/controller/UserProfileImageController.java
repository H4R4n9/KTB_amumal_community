package com.kyla.community.domain.user.controller;

import com.kyla.community.domain.user.dto.res.ProfileUploadResponseDto;
import com.kyla.community.domain.user.service.UserService;
import com.kyla.community.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users/upload/profile-image")
@RequiredArgsConstructor
public class UserProfileImageController {
	private final UserService userService;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<ProfileUploadResponseDto>> uploadProfileImage(
			@RequestPart("profileImage") MultipartFile profileImage
	) {
		ProfileUploadResponseDto response = userService.uploadProfileImage(profileImage);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(HttpStatus.CREATED.value(), "파일 업로드 성공", response));
	}
}
