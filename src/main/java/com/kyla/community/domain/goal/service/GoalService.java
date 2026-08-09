package com.kyla.community.domain.goal.service;

import com.kyla.community.domain.goal.dto.projection.GoalListRowDto;
import com.kyla.community.domain.goal.dto.projection.GoalRepresentativeImageDto;
import com.kyla.community.domain.goal.dto.req.CreateGoalRequestDto;
import com.kyla.community.domain.goal.dto.req.UpdateGoalRequestDto;
import com.kyla.community.domain.goal.dto.res.GoalCursorPageResponseDto;
import com.kyla.community.domain.goal.dto.res.GoalDetailResponseDto;
import com.kyla.community.domain.goal.dto.res.GoalIdResponseDto;
import com.kyla.community.domain.goal.dto.res.GoalImageResponseDto;
import com.kyla.community.domain.goal.dto.res.GoalListItemResponseDto;
import com.kyla.community.domain.goal.entity.Goal;
import com.kyla.community.domain.goal.entity.GoalImage;
import com.kyla.community.domain.goal.entity.GoalStat;
import com.kyla.community.domain.goal.entity.GoalStatus;
import com.kyla.community.domain.goal.repository.GoalImageRepository;
import com.kyla.community.domain.goal.repository.GoalRepository;
import com.kyla.community.domain.goal.repository.GoalStatRepository;
import com.kyla.community.domain.user.dto.projection.UserProfileImageDto;
import com.kyla.community.domain.user.dto.res.AuthorResponseDto;
import com.kyla.community.domain.user.entity.User;
import com.kyla.community.domain.user.repository.ProfileImageRepository;
import com.kyla.community.domain.user.service.UserService;
import com.kyla.community.domain.image.entity.Image;
import com.kyla.community.domain.image.service.ImageService;
import com.kyla.community.global.exception.ApiException;
import com.kyla.community.global.security.AuthorizationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class GoalService {
	private final GoalRepository goalRepository;
	private final GoalStatRepository goalStatRepository;
	private final GoalImageRepository goalImageRepository;
	private final ProfileImageRepository profileImageRepository;
	private final ImageService imageService;
	private final GoalCursorCodec goalCursorCodec;
	private final UserService userService;
	private final AuthorizationValidator authorizationValidator;

	public GoalIdResponseDto create(Long userId, CreateGoalRequestDto request) {
		User user = userService.getActiveUser(userId);
		Goal goal = new Goal(
				user,
				request.getTitle(),
				request.getDescription(),
				request.getStartDate(),
				request.getEndDate(),
				request.getStatus() == null ? GoalStatus.IN_PROGRESS : request.getStatus()
		);

		goal.initializeStat();
		attachImages(
				goal,
				userId,
				request.getImages() == null ? List.of() : request.getImages()
		);

		Goal savedGoal = goalRepository.save(goal);
		return new GoalIdResponseDto(savedGoal.getGoalId());
	}

	public GoalDetailResponseDto getDetail(Long goalId) {
		if (goalStatRepository.increaseViewCount(goalId) != 1) {
			throw new ApiException(HttpStatus.NOT_FOUND, "요청한 목표를 찾을 수 없습니다.");
		}
		Goal goal = goalRepository.findDetailById(goalId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "요청한 목표를 찾을 수 없습니다."));
		List<GoalImageResponseDto> images = goalImageRepository.findResponsesByGoalId(goalId);
		return toDetailResponse(goal, images);
	}

	@Transactional(readOnly = true)
	public GoalCursorPageResponseDto getList(String encodedCursor, int limit) {
		List<GoalListRowDto> rows = goalCursorCodec.decode(encodedCursor)
				.map(cursor -> goalRepository.findListRowsAfter(
						cursor.createdAt(),
						cursor.goalId(),
						toPageable(limit)
				))
				.orElseGet(() -> goalRepository.findListRows(toPageable(limit)));
		return toCursorPageResponse(rows, limit);
	}

	@Transactional(readOnly = true)
	public GoalCursorPageResponseDto search(String keyword, String encodedCursor, int limit) {
		if (keyword == null || keyword.isBlank()) {
			throw new IllegalArgumentException("검색어가 필요합니다.");
		}
		String normalizedKeyword = keyword.trim();
		List<GoalListRowDto> rows = goalCursorCodec.decode(encodedCursor)
				.map(cursor -> goalRepository.searchRowsAfter(
						normalizedKeyword,
						cursor.createdAt(),
						cursor.goalId(),
						toPageable(limit)
				))
				.orElseGet(() -> goalRepository.searchRows(normalizedKeyword, toPageable(limit)));
		return toCursorPageResponse(rows, limit);
	}

	public GoalIdResponseDto update(Long goalId, Long userId, UpdateGoalRequestDto request) {
		Goal goal = getGoalForUpdate(goalId);
		validateOwner(goal, userId);
		goal.update(
				request.getTitle(),
				request.getDescription(),
				request.getStartDate(),
				request.getEndDate(),
				request.getStatus()
		);

		if (request.getImages() != null) {
			List<Image> previousImages = goal.getImages().stream()
					.map(GoalImage::getImage)
					.toList();
			goal.clearImages();
			goalRepository.flush();
			previousImages.forEach(imageService::release);
			attachImages(goal, userId, request.getImages());
		}
		return new GoalIdResponseDto(goalId);
	}

	public void delete(Long goalId, Long userId) {
		Goal goal = getGoalForUpdate(goalId);
		validateOwner(goal, userId);
		goal.getImages().stream()
				.map(GoalImage::getImage)
				.forEach(imageService::release);
		goalRepository.delete(goal);
	}

	@Transactional(readOnly = true)
	public Goal getGoal(Long goalId) {
		return goalRepository.findById(goalId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "요청한 목표를 찾을 수 없습니다."));
	}

	@Transactional(readOnly = true)
	public Goal getOwnedGoal(Long goalId, Long userId) {
		Goal goal = getGoal(goalId);
		validateOwner(goal, userId);
		return goal;
	}

	@Transactional(readOnly = true)
	public void validateOwner(Long goalId, Long userId) {
		validateOwner(getGoal(goalId), userId);
	}

	private Goal getGoalForUpdate(Long goalId) {
		return goalRepository.findByIdForUpdate(goalId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "요청한 목표를 찾을 수 없습니다."));
	}

	private void validateOwner(Goal goal, Long userId) {
		authorizationValidator.validateOwner(goal.getUser().getUserId(), userId);
	}

	private void attachImages(
			Goal goal,
			Long userId,
			List<com.kyla.community.domain.goal.dto.req.GoalImageRequestDto> requests
	) {
		validateImageRequests(requests);
		Map<String, Image> imagesByKey = imageService.getGoalImagesForAttach(
				userId,
				requests.stream().map(request -> request.getObjectKey()).toList()
		).stream().collect(Collectors.toMap(Image::getObjectKey, image -> image));
		requests.forEach(request -> goal.addImage(new GoalImage(
				imagesByKey.get(request.getObjectKey()),
				request.getDisplayOrder()
		)));
	}

	private void validateImageRequests(
			List<com.kyla.community.domain.goal.dto.req.GoalImageRequestDto> requests
	) {
		Set<String> objectKeys = new HashSet<>();
		Set<Integer> displayOrders = new HashSet<>();
		for (var request : requests) {
			if (!objectKeys.add(request.getObjectKey())) {
				throw new IllegalArgumentException("같은 이미지를 중복 등록할 수 없습니다.");
			}
			if (!displayOrders.add(request.getDisplayOrder())) {
				throw new IllegalArgumentException("이미지 표시 순서는 중복될 수 없습니다.");
			}
		}
		for (int order = 0; order < requests.size(); order++) {
			if (!displayOrders.contains(order)) {
				throw new IllegalArgumentException("이미지 표시 순서는 0부터 연속되어야 합니다.");
			}
		}
	}

	private GoalDetailResponseDto toDetailResponse(Goal goal, List<GoalImageResponseDto> images) {
		GoalStat stat = goal.getStat();
		return new GoalDetailResponseDto(
				goal.getGoalId(),
				goal.getUser().getUserId(),
				goal.getTitle(),
				goal.getDescription(),
				goal.getStartDate(),
				goal.getEndDate(),
				goal.getStatus(),
				toAuthorResponse(goal.getUser()),
				stat.getViewCount(),
				stat.getLikeCount(),
				images,
				goal.getCreatedAt(),
				goal.getUpdatedAt()
		);
	}

	private AuthorResponseDto toAuthorResponse(User user) {
		return userService.getAuthor(user.getUserId());
	}

	private List<GoalListItemResponseDto> toListResponses(List<GoalListRowDto> rows) {
		if (rows.isEmpty()) {
			return List.of();
		}

		List<Long> goalIds = rows.stream()
				.map(GoalListRowDto::goalId)
				.toList();
		Set<Long> userIds = rows.stream()
				.map(GoalListRowDto::userId)
				.collect(Collectors.toSet());

		Map<Long, String> imageByGoalId = goalImageRepository.findRepresentativesByGoalIds(goalIds)
				.stream()
				.collect(Collectors.toMap(
						GoalRepresentativeImageDto::goalId,
						GoalRepresentativeImageDto::objectKey
				));
		Map<Long, String> profileByUserId = profileImageRepository.findResponsesByUserIds(userIds)
				.stream()
				.collect(Collectors.toMap(
						UserProfileImageDto::userId,
						UserProfileImageDto::objectKey
				));

		return rows.stream()
				.map(row -> new GoalListItemResponseDto(
						row.goalId(),
						row.title(),
						row.startDate(),
						row.endDate(),
						row.status(),
						new AuthorResponseDto(
								row.userId(),
								row.nickname(),
								profileByUserId.get(row.userId())
						),
						row.viewCount(),
						row.likeCount(),
						imageByGoalId.get(row.goalId()),
						row.createdAt()
				))
				.toList();
	}

	private GoalCursorPageResponseDto toCursorPageResponse(List<GoalListRowDto> rows, int limit) {
		boolean hasNext = rows.size() > limit;
		List<GoalListRowDto> pageRows = hasNext ? rows.subList(0, limit) : rows;
		String nextCursor = hasNext
				? goalCursorCodec.encode(
						pageRows.getLast().createdAt(),
						pageRows.getLast().goalId()
				)
				: null;

		return new GoalCursorPageResponseDto(
				toListResponses(pageRows),
				nextCursor,
				hasNext
		);
	}

	private Pageable toPageable(int limit) {
		if (limit < 1 || limit > 100) {
			throw new IllegalArgumentException("페이지 범위가 올바르지 않습니다.");
		}
		return PageRequest.of(0, limit + 1);
	}
}
