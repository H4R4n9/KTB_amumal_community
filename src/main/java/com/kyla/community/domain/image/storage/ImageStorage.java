package com.kyla.community.domain.image.storage;

import java.net.URL;
import java.time.Duration;

public interface ImageStorage {
	URL createPresignedPutUrl(
			String objectKey,
			String contentType,
			long fileSize,
			Duration expiration
	);

	void verifyExists(String objectKey);

	void delete(String objectKey);
}
