package com.kyla.community.domain.image.service;

public record ImageCleanupTarget(
		Long imageId,
		String objectKey,
		String uploadObjectKey
) {
}
