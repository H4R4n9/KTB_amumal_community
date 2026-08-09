package com.kyla.community.domain.image.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageScheduler {
	private static final int BATCH_SIZE = 100;

	private final ImageCleanupService imageCleanupService;

	@Value("${app.image.orphan-retention}")
	private Duration orphanRetention;

	@Scheduled(cron = "${app.image.orphan-cleanup-cron}")
	public void deleteOrphanImages() {
		LocalDateTime threshold = LocalDateTime.now().minus(orphanRetention);
		int deletedCount = imageCleanupService.cleanup(threshold, BATCH_SIZE);
		if (deletedCount > 0) {
			log.info("고아 이미지 {}개를 삭제했습니다.", deletedCount);
		}
	}
}
