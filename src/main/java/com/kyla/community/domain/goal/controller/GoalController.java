package com.kyla.community.domain.goal.controller;

import com.kyla.community.domain.goal.dto.req.CreateGoalRequestDto;
import com.kyla.community.domain.goal.dto.req.UpdateGoalRequestDto;
import com.kyla.community.domain.goal.dto.res.GoalCursorPageResponseDto;
import com.kyla.community.domain.goal.dto.res.GoalDetailResponseDto;
import com.kyla.community.domain.goal.dto.res.GoalIdResponseDto;
import com.kyla.community.domain.goal.service.GoalService;
import com.kyla.community.global.common.ApiResponse;
import com.kyla.community.global.filter.JwtAuthFilter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goals")
@RequiredArgsConstructor
public class GoalController {
	private final GoalService goalService;

	@GetMapping
	public ResponseEntity<ApiResponse<GoalCursorPageResponseDto>> getGoals(
			@RequestParam(required = false) String cursor,
			@RequestParam(defaultValue = "20") int limit
	) {
		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				"목표 목록 조회 성공",
				goalService.getList(cursor, limit)
		));
	}

	@GetMapping("/search")
	public ResponseEntity<ApiResponse<GoalCursorPageResponseDto>> searchGoals(
			@RequestParam String keyword,
			@RequestParam(required = false) String cursor,
			@RequestParam(defaultValue = "20") int limit
	) {
		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				"목표 검색 성공",
				goalService.search(keyword, cursor, limit)
		));
	}

	@GetMapping("/{goalId}")
	public ResponseEntity<ApiResponse<GoalDetailResponseDto>> getGoal(@PathVariable Long goalId) {
		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				"목표 조회 성공",
				goalService.getDetail(goalId)
		));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<GoalIdResponseDto>> createGoal(
			@RequestAttribute(JwtAuthFilter.LOGIN_USER_ID_ATTRIBUTE) Long userId,
			@Valid @RequestBody CreateGoalRequestDto request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
				HttpStatus.CREATED.value(),
				"목표 생성 성공",
				goalService.create(userId, request)
		));
	}

	@PatchMapping("/{goalId}")
	public ResponseEntity<ApiResponse<GoalIdResponseDto>> updateGoal(
			@PathVariable Long goalId,
			@RequestAttribute(JwtAuthFilter.LOGIN_USER_ID_ATTRIBUTE) Long userId,
			@Valid @RequestBody UpdateGoalRequestDto request
	) {
		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				"목표 수정 성공",
				goalService.update(goalId, userId, request)
		));
	}

	@DeleteMapping("/{goalId}")
	public ResponseEntity<ApiResponse<Void>> deleteGoal(
			@PathVariable Long goalId,
			@RequestAttribute(JwtAuthFilter.LOGIN_USER_ID_ATTRIBUTE) Long userId
	) {
		goalService.delete(goalId, userId);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "목표 삭제 성공", null));
	}
}
