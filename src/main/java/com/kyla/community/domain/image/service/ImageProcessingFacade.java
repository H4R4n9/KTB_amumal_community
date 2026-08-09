package com.kyla.community.domain.image.service;

import com.kyla.community.domain.image.storage.ImageStorage;
import com.kyla.community.global.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class ImageProcessingFacade {
	private final ImageStorage imageStorage;
	private final ImageService imageService;
	private final byte[] callbackSecret;

	public ImageProcessingFacade(
			ImageStorage imageStorage,
			ImageService imageService,
			@Value("${app.image.lambda-callback-secret}") String callbackSecret
	) {
		if (callbackSecret == null || callbackSecret.isBlank()) {
			throw new IllegalStateException("Lambda 콜백 secret이 필요합니다.");
		}
		this.imageStorage = imageStorage;
		this.imageService = imageService;
		this.callbackSecret = callbackSecret.getBytes(StandardCharsets.UTF_8);
	}

	public void complete(String providedSecret, String objectKey) {
		verifySecret(providedSecret);
		imageStorage.verifyExists(objectKey);
		imageService.completeUpload(objectKey);
	}

	private void verifySecret(String providedSecret) {
		byte[] provided = providedSecret == null
				? new byte[0]
				: providedSecret.getBytes(StandardCharsets.UTF_8);
		if (!MessageDigest.isEqual(callbackSecret, provided)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Lambda 콜백 인증에 실패했습니다.");
		}
	}
}
