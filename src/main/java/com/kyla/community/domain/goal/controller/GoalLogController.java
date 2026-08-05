package com.kyla.community.domain.goal.controller;

import com.kyla.community.domain.goal.dto.req.UpdateGoalLogRequestDto;
import com.kyla.community.domain.goal.dto.res.GoalLogResponseDto;
import com.kyla.community.domain.goal.service.GoalLogService;
import com.kyla.community.global.common.ApiResponse;
import com.kyla.community.global.filter.JwtAuthFilter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/goals/{goalId}/logs")
@RequiredArgsConstructor
public class GoalLogController {
	private final GoalLogService goalLogService;

	@GetMapping
	public ResponseEntity<ApiResponse<List<GoalLogResponseDto>>> getLogs(
			@PathVariable Long goalId,
			@RequestAttribute(JwtAuthFilter.LOGIN_USER_ID_ATTRIBUTE) Long userId
	) {
		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				"목표 기록 조회 성공",
				goalLogService.getLogs(goalId, userId)
		));
	}

	@PutMapping("/{logDate}")
	public ResponseEntity<ApiResponse<GoalLogResponseDto>> putLog(
			@PathVariable Long goalId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate logDate,
			@RequestAttribute(JwtAuthFilter.LOGIN_USER_ID_ATTRIBUTE) Long userId,
			@Valid @RequestBody UpdateGoalLogRequestDto request
	) {
		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				"목표 기록 저장 성공",
				goalLogService.put(goalId, logDate, userId, request)
		));
	}

	@DeleteMapping("/{logDate}")
	public ResponseEntity<ApiResponse<Void>> deleteLog(
			@PathVariable Long goalId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate logDate,
			@RequestAttribute(JwtAuthFilter.LOGIN_USER_ID_ATTRIBUTE) Long userId
	) {
		goalLogService.delete(goalId, logDate, userId);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "목표 기록 삭제 성공", null));
	}
}
