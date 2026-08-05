package com.kyla.community.domain.goal.controller;

import com.kyla.community.domain.goal.dto.res.GoalImageUploadResponseDto;
import com.kyla.community.domain.goal.service.GoalImageService;
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
@RequestMapping("/goals/images")
@RequiredArgsConstructor
public class GoalImageController {
	private final GoalImageService goalImageService;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<GoalImageUploadResponseDto>> upload(
			@RequestPart("image") MultipartFile image
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
				HttpStatus.CREATED.value(),
				"목표 이미지 업로드 성공",
				goalImageService.upload(image)
		));
	}
}
