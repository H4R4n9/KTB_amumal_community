package com.kyla.community.domain.goal.dto.res;

import com.kyla.community.domain.goal.entity.GoalStatus;
import com.kyla.community.domain.user.dto.res.AuthorResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record GoalListItemResponseDto(
		Long goalId,
		String title,
		LocalDate startDate,
		LocalDate endDate,
		GoalStatus status,
		AuthorResponseDto author,
		long viewCount,
		long likeCount,
		String representativeImageObjectKey,
		LocalDateTime createdAt
) {
}
