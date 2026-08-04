package com.kyla.community.domain.goal.controller;

import com.kyla.community.domain.goal.dto.res.GoalLikeResponseDto;
import com.kyla.community.domain.goal.service.GoalLikeService;
import com.kyla.community.global.common.ApiResponse;
import com.kyla.community.global.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goals/{goalId}/like")
@RequiredArgsConstructor
public class GoalLikeController {
	private final GoalLikeService goalLikeService;

	@GetMapping
	public ResponseEntity<ApiResponse<GoalLikeResponseDto>> getStatus(
			@PathVariable Long goalId,
			@RequestAttribute(JwtAuthFilter.LOGIN_USER_ID_ATTRIBUTE) Long userId
	) {
		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				"좋아요 상태 조회 성공",
				goalLikeService.getStatus(goalId, userId)
		));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<GoalLikeResponseDto>> like(
			@PathVariable Long goalId,
			@RequestAttribute(JwtAuthFilter.LOGIN_USER_ID_ATTRIBUTE) Long userId
	) {
		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				"좋아요 등록 성공",
				goalLikeService.like(goalId, userId)
		));
	}

	@DeleteMapping
	public ResponseEntity<ApiResponse<GoalLikeResponseDto>> unlike(
			@PathVariable Long goalId,
			@RequestAttribute(JwtAuthFilter.LOGIN_USER_ID_ATTRIBUTE) Long userId
	) {
		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				"좋아요 취소 성공",
				goalLikeService.unlike(goalId, userId)
		));
	}
}
