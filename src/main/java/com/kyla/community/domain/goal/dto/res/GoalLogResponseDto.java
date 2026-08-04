package com.kyla.community.domain.goal.dto.res;

import com.kyla.community.domain.goal.entity.GoalLogStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record GoalLogResponseDto(
		Long logId,
		Long goalId,
		LocalDate logDate,
		GoalLogStatus completionStatus,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
}
