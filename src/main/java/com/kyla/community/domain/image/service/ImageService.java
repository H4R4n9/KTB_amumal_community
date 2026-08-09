package com.kyla.community.domain.image.service;

import com.kyla.community.domain.image.dto.req.ImageUploadRequestDto;
import com.kyla.community.domain.image.dto.res.ImageResponseDto;
import com.kyla.community.domain.image.dto.res.ImageUploadResponseDto;
import com.kyla.community.domain.image.entity.Image;
import com.kyla.community.domain.image.entity.ImagePurpose;
import com.kyla.community.domain.image.entity.ImageStatus;
import com.kyla.community.domain.image.repository.ImageRepository;
import com.kyla.community.domain.image.storage.ImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {
	private static final String INCOMING_PREFIX = "incoming/";
	private static final Map<String, String> EXTENSIONS = Map.of(
			"image/jpeg", ".jpg",
			"image/png", ".png"
	);

	private final ImageRepository imageRepository;
	private final ImageStorage imageStorage;

	@Value("${app.image.presigned-url-expiration}")
	private Duration presignedUrlExpiration;

	@Value("${app.image.max-file-size}")
	private DataSize maxFileSize;

	@Transactional
	public ImageUploadResponseDto issueGoalUpload(Long userId, ImageUploadRequestDto request) {
		return issueUpload(userId, ImagePurpose.GOAL, request);
	}

	@Transactional
	public ImageUploadResponseDto issueProfileUpload(ImageUploadRequestDto request) {
		return issueUpload(null, ImagePurpose.PROFILE, request);
	}

	@Transactional
	public void completeUpload(String objectKey) {
		Image image = findByObjectKeyForUpdate(objectKey)
				.orElseThrow(() -> new IllegalArgumentException("업로드 요청을 찾을 수 없습니다."));
		image.completeUpload();
	}

	@Transactional(readOnly = true)
	public ImageResponseDto getStatus(String objectKey) {
		Image image = findByObjectKey(objectKey)
				.orElseThrow(() -> new IllegalArgumentException("업로드 요청을 찾을 수 없습니다."));
		return ImageResponseDto.from(image);
	}

	@Transactional
	public List<Image> getGoalImagesForAttach(
			Long userId,
			Collection<String> objectKeys
	) {
		Map<String, Image> imagesByKey = lockImages(objectKeys);
		return objectKeys.stream()
				.map(objectKey -> {
					Image image = requireImage(imagesByKey, objectKey);
					image.attach(ImagePurpose.GOAL, userId);
					return image;
				})
				.toList();
	}

	@Transactional
	public Image getProfileImageForAttach(Long userId, String objectKey) {
		Image image = findByObjectKeyForUpdate(objectKey)
				.orElseThrow(() -> new IllegalArgumentException("업로드한 이미지를 찾을 수 없습니다."));
		image.attach(ImagePurpose.PROFILE, userId);
		return image;
	}

	public void release(Image image) {
		image.release();
	}

	@Transactional
	public List<ImageCleanupTarget> claimExpiredOrphans(LocalDateTime threshold, int limit) {
		List<Long> candidateIds = imageRepository.findOrphanIds(
				EnumSet.of(ImageStatus.PENDING, ImageStatus.UPLOADED, ImageStatus.DELETE_FAILED),
				threshold,
				PageRequest.of(0, limit)
		);
		return candidateIds.stream()
				.map(imageRepository::findByIdForUpdate)
				.flatMap(java.util.Optional::stream)
				.filter(this::isCleanupCandidate)
				.filter(image -> !imageRepository.isReferenced(image.getId()))
				.filter(image -> image.getCreatedAt().isBefore(threshold))
				.map(image -> {
					image.markDeleting();
					return new ImageCleanupTarget(
							image.getId(),
							image.getObjectKey(),
							toUploadObjectKey(image.getObjectKey())
					);
				})
				.toList();
	}

	@Transactional
	public void deleteClaimedImage(Long imageId) {
		imageRepository.findById(imageId)
				.filter(image -> image.getStatus() == ImageStatus.DELETING)
				.ifPresent(imageRepository::delete);
	}

	@Transactional
	public void markDeleteFailed(Long imageId) {
		imageRepository.findById(imageId).ifPresent(Image::markDeleteFailed);
	}

	private ImageUploadResponseDto issueUpload(
			Long uploaderId,
			ImagePurpose purpose,
			ImageUploadRequestDto request
	) {
		String normalizedContentType = request.contentType().toLowerCase(Locale.ROOT);
		String extension = validateAndResolveExtension(normalizedContentType, request.fileSize());
		validateOriginalFilename(request.originalFilename(), extension);
		String name = UUID.randomUUID() + extension;
		LocalDateTime expiresAt = LocalDateTime.now().plus(presignedUrlExpiration);
		Image image = imageRepository.save(Image.pending(
				uploaderId,
				purpose,
				purpose.path(),
				name
		));
		String uploadObjectKey = toUploadObjectKey(image.getObjectKey());
		URL uploadUrl = imageStorage.createPresignedPutUrl(
				uploadObjectKey,
				normalizedContentType,
				request.fileSize(),
				presignedUrlExpiration
		);
		return new ImageUploadResponseDto(
				image.getId(),
				image.getObjectKey(),
				uploadObjectKey,
				uploadUrl.toString(),
				expiresAt
		);
	}

	private String toUploadObjectKey(String objectKey) {
		return INCOMING_PREFIX + objectKey;
	}

	private String validateAndResolveExtension(String contentType, long fileSize) {
		String extension = EXTENSIONS.get(contentType);
		if (extension == null) {
			throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다.");
		}
		if (fileSize > maxFileSize.toBytes()) {
			throw new IllegalArgumentException("이미지 파일 크기가 제한을 초과했습니다.");
		}
		return extension;
	}

	private Map<String, Image> lockImages(Collection<String> objectKeys) {
		if (objectKeys.isEmpty()) {
			return Map.of();
		}
		return objectKeys.stream()
				.map(this::findByObjectKeyForUpdate)
				.flatMap(java.util.Optional::stream)
				.collect(Collectors.toMap(Image::getObjectKey, Function.identity()));
	}

	private java.util.Optional<Image> findByObjectKeyForUpdate(String objectKey) {
		ObjectKeyParts parts = splitObjectKey(objectKey);
		if (parts == null) {
			return java.util.Optional.empty();
		}
		return imageRepository.findByPathAndNameForUpdate(parts.path(), parts.name());
	}

	private java.util.Optional<Image> findByObjectKey(String objectKey) {
		ObjectKeyParts parts = splitObjectKey(objectKey);
		if (parts == null) {
			return java.util.Optional.empty();
		}
		return imageRepository.findByPathAndName(parts.path(), parts.name());
	}

	private ObjectKeyParts splitObjectKey(String objectKey) {
		int nameStart = objectKey.lastIndexOf('/') + 1;
		if (nameStart <= 0 || nameStart >= objectKey.length()) {
			return null;
		}
		return new ObjectKeyParts(
				objectKey.substring(0, nameStart),
				objectKey.substring(nameStart)
		);
	}

	private void validateOriginalFilename(String originalFilename, String expectedExtension) {
		int extensionStart = originalFilename.lastIndexOf('.');
		if (extensionStart < 0) {
			throw new IllegalArgumentException("이미지 파일 확장자가 필요합니다.");
		}
		String extension = originalFilename.substring(extensionStart).toLowerCase(Locale.ROOT);
		if (".jpeg".equals(extension)) {
			extension = ".jpg";
		}
		if (!expectedExtension.equals(extension)) {
			throw new IllegalArgumentException("이미지 확장자와 Content-Type이 일치하지 않습니다.");
		}
	}

	private boolean isCleanupCandidate(Image image) {
		return image.getStatus() == ImageStatus.PENDING
				|| image.getStatus() == ImageStatus.UPLOADED
				|| image.getStatus() == ImageStatus.DELETE_FAILED;
	}

	private record ObjectKeyParts(String path, String name) {
	}

	private Image requireImage(Map<String, Image> imagesByKey, String objectKey) {
		Image image = imagesByKey.get(objectKey);
		if (image == null) {
			throw new IllegalArgumentException("업로드한 이미지를 찾을 수 없습니다.");
		}
		return image;
	}
}
