package com.kyla.community.domain.image.service;

import com.kyla.community.domain.image.storage.ImageStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageCleanupService {
	private final ImageService imageService;
	private final ImageStorage imageStorage;

	public int cleanup(LocalDateTime threshold, int limit) {
		List<ImageCleanupTarget> targets = imageService.claimExpiredOrphans(threshold, limit);
		int deletedCount = 0;
		for (ImageCleanupTarget target : targets) {
			try {
				imageStorage.delete(target.objectKey());
				imageStorage.delete(target.uploadObjectKey());
				imageService.deleteClaimedImage(target.imageId());
				deletedCount++;
			} catch (RuntimeException exception) {
				imageService.markDeleteFailed(target.imageId());
				log.warn("고아 이미지 삭제 실패: imageId={}, objectKey={}, uploadObjectKey={}",
						target.imageId(), target.objectKey(), target.uploadObjectKey(), exception);
			}
		}
		return deletedCount;
	}
}
