package com.kyla.community.domain.image.dto.res;

import com.kyla.community.domain.image.entity.Image;
import com.kyla.community.domain.image.entity.ImageStatus;

import java.time.LocalDateTime;

public record ImageResponseDto(
		Long id,
		String path,
		String name,
		String objectKey,
		ImageStatus status,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
	public static ImageResponseDto from(Image image) {
		return new ImageResponseDto(
				image.getId(),
				image.getPath(),
				image.getName(),
				image.getObjectKey(),
				image.getStatus(),
				image.getCreatedAt(),
				image.getUpdatedAt()
		);
	}
}
