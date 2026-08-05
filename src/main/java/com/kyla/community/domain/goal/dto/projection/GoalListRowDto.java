package com.kyla.community.domain.goal.dto.projection;

import com.kyla.community.domain.goal.entity.GoalStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record GoalListRowDto(
		Long goalId,
		String title,
		LocalDate startDate,
		LocalDate endDate,
		GoalStatus status,
		Long userId,
		String nickname,
		long viewCount,
		long likeCount,
		LocalDateTime createdAt
) {
}
