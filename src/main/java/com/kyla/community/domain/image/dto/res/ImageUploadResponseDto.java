package com.kyla.community.domain.image.dto.res;

import java.time.LocalDateTime;

public record ImageUploadResponseDto(
		Long imageId,
		String objectKey,
		String uploadObjectKey,
		String uploadUrl,
		LocalDateTime expiresAt
) {
}
