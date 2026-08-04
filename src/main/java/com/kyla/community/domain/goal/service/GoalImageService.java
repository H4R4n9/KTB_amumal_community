package com.kyla.community.domain.goal.service;

import com.kyla.community.domain.goal.dto.req.GoalImageRequestDto;
import com.kyla.community.domain.goal.dto.res.GoalImageResponseDto;
import com.kyla.community.domain.goal.dto.res.GoalImageUploadResponseDto;
import com.kyla.community.domain.goal.entity.GoalImage;
import com.kyla.community.domain.goal.repository.GoalImageRepository;
import com.kyla.community.global.storage.FileStorage;
import com.kyla.community.global.storage.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GoalImageService {
	private final FileStorage fileStorage;
	private final GoalImageRepository goalImageRepository;

	public GoalImageUploadResponseDto upload(MultipartFile image) {
		StoredFile storedFile = fileStorage.uploadImage(image, "goals");
		return new GoalImageUploadResponseDto(
				storedFile.objectKey(),
				storedFile.contentType(),
				storedFile.fileSize()
		);
	}

	public void replace(Long goalId, List<GoalImageRequestDto> requests) {
		if (requests == null) {
			return;
		}
		validate(requests);
		goalImageRepository.deleteByGoalId(goalId);
		List<GoalImage> images = requests.stream()
				.map(request -> new GoalImage(
						goalId,
						request.getObjectKey(),
						request.getContentType(),
						request.getFileSize(),
						request.getDisplayOrder()
				))
				.toList();
		goalImageRepository.saveAll(images);
	}

	public List<GoalImageResponseDto> getImages(Long goalId) {
		return goalImageRepository.findByGoalIdOrderByDisplayOrderAsc(goalId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	private void validate(List<GoalImageRequestDto> requests) {
		Set<Integer> orders = new HashSet<>();
		Set<String> objectKeys = new HashSet<>();
		for (GoalImageRequestDto request : requests) {
			if (!orders.add(request.getDisplayOrder())) {
				throw new IllegalArgumentException("이미지 표시 순서는 중복될 수 없습니다.");
			}
			if (!objectKeys.add(request.getObjectKey())) {
				throw new IllegalArgumentException("같은 이미지를 중복 등록할 수 없습니다.");
			}
			validateObjectKey(request.getObjectKey());
			if (!request.getContentType().startsWith("image/")) {
				throw new IllegalArgumentException("이미지 contentType이 올바르지 않습니다.");
			}
		}
	}

	private void validateObjectKey(String objectKey) {
		if (!objectKey.matches("[A-Za-z0-9!_.*'()/-]+")) {
			throw new IllegalArgumentException("이미지 Object Key가 올바르지 않습니다.");
		}
		for (String segment : objectKey.split("/")) {
			if (segment.isBlank() || ".".equals(segment) || "..".equals(segment)) {
				throw new IllegalArgumentException("이미지 Object Key 경로가 올바르지 않습니다.");
			}
		}
	}

	private GoalImageResponseDto toResponse(GoalImage image) {
		return new GoalImageResponseDto(
				image.getGoalImageId(),
				image.getObjectKey(),
				image.getContentType(),
				image.getFileSize(),
				image.getDisplayOrder()
		);
	}
}
