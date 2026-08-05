package com.kyla.community.domain.goal.service;

import com.kyla.community.domain.goal.dto.projection.GoalListRowDto;
import com.kyla.community.domain.goal.dto.projection.GoalRepresentativeImageDto;
import com.kyla.community.domain.goal.dto.req.CreateGoalRequestDto;
import com.kyla.community.domain.goal.dto.req.UpdateGoalRequestDto;
import com.kyla.community.domain.goal.dto.res.GoalDetailResponseDto;
import com.kyla.community.domain.goal.dto.res.GoalIdResponseDto;
import com.kyla.community.domain.goal.dto.res.GoalImageResponseDto;
import com.kyla.community.domain.goal.dto.res.GoalListItemResponseDto;
import com.kyla.community.domain.goal.entity.Goal;
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
import com.kyla.community.global.exception.ApiException;
import com.kyla.community.global.security.AuthorizationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
	private final GoalImageService goalImageService;
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
		goalImageService.createImages(
				request.getImages() == null ? List.of() : request.getImages()
		).forEach(goal::addImage);

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
	public List<GoalListItemResponseDto> getList(int offset, int limit) {
		return toListResponses(goalRepository.findListRows(toPageable(offset, limit)));
	}

	@Transactional(readOnly = true)
	public List<GoalListItemResponseDto> search(String keyword, int offset, int limit) {
		if (keyword == null || keyword.isBlank()) {
			throw new IllegalArgumentException("검색어가 필요합니다.");
		}
		return toListResponses(goalRepository.searchRows(keyword.trim(), toPageable(offset, limit)));
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
			goal.clearImages();
			goalRepository.flush();
			goalImageService.createImages(request.getImages())
					.forEach(goal::addImage);
		}
		return new GoalIdResponseDto(goalId);
	}

	public void delete(Long goalId, Long userId) {
		Goal goal = getGoalForUpdate(goalId);
		validateOwner(goal, userId);
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

	private Pageable toPageable(int offset, int limit) {
		if (offset < 0 || limit < 1 || limit > 100) {
			throw new IllegalArgumentException("페이지 범위가 올바르지 않습니다.");
		}
		return PageRequest.of(offset / limit, limit);
	}
}
