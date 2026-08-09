package com.kyla.community.domain.user.service;

import com.kyla.community.domain.image.entity.Image;
import com.kyla.community.domain.image.service.ImageService;
import com.kyla.community.domain.user.entity.ProfileImage;
import com.kyla.community.domain.user.repository.ProfileImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileImageService {
	private final ProfileImageRepository profileImageRepository;
	private final ImageService imageService;

	public void saveIfPresent(Long userId, String objectKey) {
		if (objectKey == null || objectKey.isBlank()) {
			return;
		}
		saveOrUpdate(userId, objectKey);
	}

	public void updateIfPresent(Long userId, String objectKey) {
		if (objectKey == null || objectKey.isBlank()) {
			return;
		}
		saveOrUpdate(userId, objectKey);
    }

	public String getObjectKey(Long userId) {
		return profileImageRepository.findByUserId(userId)
				.map(ProfileImage::getObjectKey)
                .orElse(null);
    }

	private void saveOrUpdate(Long userId, String objectKey) {
		ProfileImage current = profileImageRepository.findByUserId(userId).orElse(null);
		if (current != null && current.getObjectKey().equals(objectKey)) {
			return;
		}

		Image nextImage = imageService.getProfileImageForAttach(userId, objectKey);
		if (current == null) {
			profileImageRepository.save(new ProfileImage(userId, nextImage));
			return;
		}

		imageService.release(current.getImage());
		current.update(nextImage);
	}
}
