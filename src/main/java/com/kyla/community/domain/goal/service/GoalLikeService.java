package com.kyla.community.domain.goal.service;

import com.kyla.community.domain.goal.dto.res.GoalLikeResponseDto;
import com.kyla.community.domain.goal.entity.GoalStat;
import com.kyla.community.domain.goal.repository.GoalLikeRepository;
import com.kyla.community.domain.goal.repository.GoalStatRepository;
import com.kyla.community.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GoalLikeService {
	private final GoalService goalService;
	private final UserService userService;
	private final GoalLikeRepository goalLikeRepository;
	private final GoalStatRepository goalStatRepository;

	public GoalLikeResponseDto like(Long goalId, Long userId) {
		goalService.getGoal(goalId);
		userService.getActiveUser(userId);
		if (goalLikeRepository.insertIgnore(goalId, userId) == 1) {
			goalStatRepository.increaseLikeCount(goalId);
		}
		return response(goalId, userId, true);
	}

	public GoalLikeResponseDto unlike(Long goalId, Long userId) {
		goalService.getGoal(goalId);
		if (goalLikeRepository.deleteByGoalIdAndUserId(goalId, userId) == 1) {
			goalStatRepository.decreaseLikeCount(goalId);
		}
		return response(goalId, userId, false);
	}

	@Transactional(readOnly = true)
	public GoalLikeResponseDto getStatus(Long goalId, Long userId) {
		goalService.getGoal(goalId);
		return response(
				goalId,
				userId,
				goalLikeRepository.existsByGoalIdAndUserId(goalId, userId)
		);
	}

	private GoalLikeResponseDto response(Long goalId, Long userId, boolean liked) {
		GoalStat stat = goalStatRepository.findById(goalId)
				.orElseThrow(() -> new IllegalStateException("목표 통계 정보가 존재하지 않습니다."));
		return new GoalLikeResponseDto(goalId, userId, liked, stat.getLikeCount());
	}
}
