package com.kyla.community.domain.user.service;

import com.kyla.community.domain.user.dto.res.ProfileUploadResponseDto;
import com.kyla.community.domain.user.entity.ProfileImage;
import com.kyla.community.domain.user.repository.ProfileImageRepository;
import com.kyla.community.global.storage.FileStorage;
import com.kyla.community.global.storage.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserProfileImageService {
    private final FileStorage fileStorage;
    private final ProfileImageRepository profileImageRepository;

    public ProfileUploadResponseDto upload(MultipartFile profileImage) {
		StoredFile storedFile = fileStorage.uploadImage(profileImage, "profiles");
		return new ProfileUploadResponseDto(
				storedFile.objectKey(),
				storedFile.contentType(),
				storedFile.fileSize()
		);
    }

	public void saveIfPresent(Long userId, String objectKey, String contentType, Long fileSize) {
		if (objectKey == null || objectKey.isBlank()) {
            return;
        }
		saveOrUpdate(userId, objectKey, contentType, requireFileSize(fileSize));
    }

	public void updateIfPresent(Long userId, String objectKey, String contentType, Long fileSize) {
		if (objectKey == null || objectKey.isBlank()) {
            return;
        }
		saveOrUpdate(userId, objectKey, contentType, requireFileSize(fileSize));
    }

	public String getObjectKey(Long userId) {
		return profileImageRepository.findByUserId(userId)
				.map(ProfileImage::getObjectKey)
                .orElse(null);
    }

	private void saveOrUpdate(Long userId, String objectKey, String contentType, long fileSize) {
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new IllegalArgumentException("프로필 이미지 contentType이 올바르지 않습니다.");
		}
		ProfileImage profileImage = profileImageRepository.findByUserId(userId)
				.orElseGet(() -> new ProfileImage(userId, objectKey, contentType, fileSize));
		profileImage.update(objectKey, contentType, fileSize);
		profileImageRepository.save(profileImage);
	}

	private long requireFileSize(Long fileSize) {
		if (fileSize == null || fileSize <= 0) {
			throw new IllegalArgumentException("프로필 이미지 크기가 올바르지 않습니다.");
		}
		return fileSize;
	}
}
