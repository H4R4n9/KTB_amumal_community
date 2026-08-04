package com.kyla.community.global.storage;

public record StoredFile(
		String objectKey,
		String contentType,
		long fileSize
) {
}
