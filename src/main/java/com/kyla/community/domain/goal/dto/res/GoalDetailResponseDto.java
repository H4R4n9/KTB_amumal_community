package com.kyla.community.domain.goal.dto.res;

import com.kyla.community.domain.goal.entity.GoalStatus;
import com.kyla.community.domain.user.dto.res.AuthorResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record GoalDetailResponseDto(
		Long goalId,
		Long userId,
		String title,
		String description,
		LocalDate startDate,
		LocalDate endDate,
		GoalStatus status,
		AuthorResponseDto author,
		long viewCount,
		long likeCount,
		List<GoalImageResponseDto> images,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
}
