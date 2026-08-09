package com.kyla.community.domain.goal.dto.res;

public record GoalImageResponseDto(
		Long goalImageId,
		Long imageId,
		String objectKey,
		int displayOrder
) {
}
