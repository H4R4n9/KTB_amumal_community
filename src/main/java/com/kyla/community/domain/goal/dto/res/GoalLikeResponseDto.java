package com.kyla.community.domain.goal.dto.res;

public record GoalLikeResponseDto(
		Long goalId,
		Long userId,
		boolean liked,
		long likeCount
) {
}
