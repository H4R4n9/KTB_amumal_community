package com.kyla.community.domain.goal.dto.res;

public record GoalImageUploadResponseDto(
		String objectKey,
		String contentType,
		long fileSize
) {
}
