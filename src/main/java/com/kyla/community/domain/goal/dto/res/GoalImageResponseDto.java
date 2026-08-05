package com.kyla.community.domain.goal.dto.res;

public record GoalImageResponseDto(
		Long goalImageId,
		String objectKey,
		String contentType,
		long fileSize,
		int displayOrder
) {
}
