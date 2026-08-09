package com.kyla.community.domain.image.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class S3ImageStorage implements ImageStorage {
	private final S3Client s3Client;
	private final S3Presigner s3Presigner;

	@Value("${aws.s3.bucket-name}")
	private String bucketName;

	@Override
	public URL createPresignedPutUrl(
			String objectKey,
			String contentType,
			long fileSize,
			Duration expiration
	) {
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(objectKey)
				.contentType(contentType)
				.contentLength(fileSize)
				.build();
		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
				.signatureDuration(expiration)
				.putObjectRequest(putObjectRequest)
				.build();
		return s3Presigner.presignPutObject(presignRequest).url();
	}

	@Override
	public void verifyExists(String objectKey) {
		s3Client.headObject(HeadObjectRequest.builder()
				.bucket(bucketName)
				.key(objectKey)
				.build());
	}

	@Override
	public void delete(String objectKey) {
		s3Client.deleteObject(DeleteObjectRequest.builder()
				.bucket(bucketName)
				.key(objectKey)
				.build());
	}
}
