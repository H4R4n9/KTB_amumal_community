package com.kyla.community.domain.goal.service;

import com.kyla.community.domain.goal.dto.req.CreateGoalRequestDto;
import com.kyla.community.domain.goal.dto.req.UpdateGoalRequestDto;
import com.kyla.community.domain.goal.dto.res.GoalDetailResponseDto;
import com.kyla.community.domain.goal.dto.res.GoalIdResponseDto;
import com.kyla.community.domain.goal.dto.res.GoalImageResponseDto;
import com.kyla.community.domain.goal.dto.res.GoalListItemResponseDto;
import com.kyla.community.domain.goal.entity.Goal;
import com.kyla.community.domain.goal.entity.GoalStat;
import com.kyla.community.domain.goal.entity.GoalStatus;
import com.kyla.community.domain.goal.repository.GoalRepository;
import com.kyla.community.domain.goal.repository.GoalStatRepository;
import com.kyla.community.domain.user.dto.res.AuthorResponseDto;
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

@Service
@Transactional
@RequiredArgsConstructor
public class GoalService {
	private final GoalRepository goalRepository;
	private final GoalStatRepository goalStatRepository;
	private final GoalImageService goalImageService;
	private final UserService userService;
	private final AuthorizationValidator authorizationValidator;

	public GoalIdResponseDto create(Long userId, CreateGoalRequestDto request) {
		userService.getActiveUser(userId);
		Goal goal = goalRepository.save(new Goal(
				userId,
				request.getTitle(),
				request.getDescription(),
				request.getStartDate(),
				request.getEndDate(),
				request.getStatus() == null ? GoalStatus.IN_PROGRESS : request.getStatus()
		));
		goalStatRepository.save(new GoalStat(goal.getGoalId()));
		goalImageService.replace(goal.getGoalId(), request.getImages() == null ? List.of() : request.getImages());
		return new GoalIdResponseDto(goal.getGoalId());
	}

	public GoalDetailResponseDto getDetail(Long goalId) {
		Goal goal = getGoal(goalId);
		if (goalStatRepository.increaseViewCount(goalId) != 1) {
			throw new IllegalStateException("목표 통계 정보가 존재하지 않습니다.");
		}
		return toDetailResponse(goal, getStat(goalId));
	}

	@Transactional(readOnly = true)
	public List<GoalListItemResponseDto> getList(int offset, int limit) {
		Pageable pageable = toPageable(offset, limit);
		return goalRepository.findAllByOrderByCreatedAtDesc(pageable)
				.stream()
				.map(this::toListItemResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<GoalListItemResponseDto> search(String keyword, int offset, int limit) {
		if (keyword == null || keyword.isBlank()) {
			throw new IllegalArgumentException("검색어가 필요합니다.");
		}
		return goalRepository.search(keyword.trim(), toPageable(offset, limit))
				.stream()
				.map(this::toListItemResponse)
				.toList();
	}

	public GoalIdResponseDto update(Long goalId, Long userId, UpdateGoalRequestDto request) {
		Goal goal = getGoalForUpdate(goalId);
		authorizationValidator.validateOwner(goal.getUserId(), userId);
		goal.update(
				request.getTitle(),
				request.getDescription(),
				request.getStartDate(),
				request.getEndDate(),
				request.getStatus()
		);
		goalImageService.replace(goalId, request.getImages());
		return new GoalIdResponseDto(goalId);
	}

	public void delete(Long goalId, Long userId) {
		Goal goal = getGoalForUpdate(goalId);
		authorizationValidator.validateOwner(goal.getUserId(), userId);
		goalRepository.delete(goal);
	}

	@Transactional(readOnly = true)
	public Goal getGoal(Long goalId) {
		return goalRepository.findById(goalId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "요청한 목표를 찾을 수 없습니다."));
	}

	@Transactional(readOnly = true)
	public void validateOwner(Long goalId, Long userId) {
		authorizationValidator.validateOwner(getGoal(goalId).getUserId(), userId);
	}

	private Goal getGoalForUpdate(Long goalId) {
		return goalRepository.findByIdForUpdate(goalId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "요청한 목표를 찾을 수 없습니다."));
	}

	private GoalStat getStat(Long goalId) {
		return goalStatRepository.findById(goalId)
				.orElseThrow(() -> new IllegalStateException("목표 통계 정보가 존재하지 않습니다."));
	}

	private GoalDetailResponseDto toDetailResponse(Goal goal, GoalStat stat) {
		return new GoalDetailResponseDto(
				goal.getGoalId(),
				goal.getUserId(),
				goal.getTitle(),
				goal.getDescription(),
				goal.getStartDate(),
				goal.getEndDate(),
				goal.getStatus(),
				toAuthorResponse(goal.getUserId()),
				stat.getViewCount(),
				stat.getLikeCount(),
				goalImageService.getImages(goal.getGoalId()),
				goal.getCreatedAt(),
				goal.getUpdatedAt()
		);
	}

	private GoalListItemResponseDto toListItemResponse(Goal goal) {
		GoalStat stat = getStat(goal.getGoalId());
		String representativeImage = goalImageService.getImages(goal.getGoalId())
				.stream()
				.findFirst()
				.map(GoalImageResponseDto::objectKey)
				.orElse(null);
		return new GoalListItemResponseDto(
				goal.getGoalId(),
				goal.getTitle(),
				goal.getStartDate(),
				goal.getEndDate(),
				goal.getStatus(),
				toAuthorResponse(goal.getUserId()),
				stat.getViewCount(),
				stat.getLikeCount(),
				representativeImage,
				goal.getCreatedAt()
		);
	}

	private AuthorResponseDto toAuthorResponse(Long userId) {
		return userService.getAuthor(userId);
	}

	private Pageable toPageable(int offset, int limit) {
		if (offset < 0 || limit < 1 || limit > 100) {
			throw new IllegalArgumentException("페이지 범위가 올바르지 않습니다.");
		}
		return PageRequest.of(offset / limit, limit);
	}
}
