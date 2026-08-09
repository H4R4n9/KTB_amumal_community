package com.kyla.community.global.config.controller;

import com.kyla.community.domain.image.dto.req.CompleteImageProcessingRequestDto;
import com.kyla.community.domain.image.dto.req.ImageUploadRequestDto;
import com.kyla.community.domain.image.dto.res.ImageResponseDto;
import com.kyla.community.domain.image.dto.res.ImageUploadResponseDto;
import com.kyla.community.domain.image.service.ImageProcessingFacade;
import com.kyla.community.domain.image.service.ImageService;
import com.kyla.community.global.common.ApiResponse;
import com.kyla.community.global.filter.JwtAuthFilter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {
	private final ImageService imageService;
	private final ImageProcessingFacade imageProcessingFacade;

	@PostMapping("/goals/presigned-url")
	public ResponseEntity<ApiResponse<ImageUploadResponseDto>> issueGoalUploadUrl(
			@RequestAttribute(JwtAuthFilter.LOGIN_USER_ID_ATTRIBUTE) Long userId,
			@Valid @RequestBody ImageUploadRequestDto request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
				HttpStatus.CREATED.value(),
				"목표 이미지 업로드 URL 발급 성공",
				imageService.issueGoalUpload(userId, request)
		));
	}

	@PostMapping("/profiles/presigned-url")
	public ResponseEntity<ApiResponse<ImageUploadResponseDto>> issueProfileUploadUrl(
			@Valid @RequestBody ImageUploadRequestDto request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
				HttpStatus.CREATED.value(),
				"프로필 이미지 업로드 URL 발급 성공",
				imageService.issueProfileUpload(request)
		));
	}

	@PostMapping("/lambda/complete")
	public ResponseEntity<ApiResponse<Void>> completeProcessing(
			@RequestHeader("X-Image-Callback-Secret") String callbackSecret,
			@Valid @RequestBody CompleteImageProcessingRequestDto request
	) {
		imageProcessingFacade.complete(callbackSecret, request.objectKey());
		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				"이미지 Lambda 처리 완료 반영 성공",
				null
		));
	}

	@GetMapping("/status")
	public ResponseEntity<ApiResponse<ImageResponseDto>> getStatus(
			@RequestParam String objectKey
	) {
		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				"이미지 처리 상태 조회 성공",
				imageService.getStatus(objectKey)
		));
	}
}
