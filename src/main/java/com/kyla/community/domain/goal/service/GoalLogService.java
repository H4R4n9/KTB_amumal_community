package com.kyla.community.domain.goal.service;

import com.kyla.community.domain.goal.dto.req.UpdateGoalLogRequestDto;
import com.kyla.community.domain.goal.dto.res.GoalLogResponseDto;
import com.kyla.community.domain.goal.entity.Goal;
import com.kyla.community.domain.goal.entity.GoalLog;
import com.kyla.community.domain.goal.repository.GoalLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class GoalLogService {
	private final GoalService goalService;
	private final GoalLogRepository goalLogRepository;

	public GoalLogResponseDto put(
			Long goalId,
			LocalDate logDate,
			Long userId,
			UpdateGoalLogRequestDto request
	) {
		Goal goal = goalService.getOwnedGoal(goalId, userId);
		GoalLog log = goalLogRepository.findByGoalGoalIdAndLogDate(goalId, logDate)
				.orElseGet(() -> new GoalLog(goal, logDate, request.getCompletionStatus()));
		log.updateStatus(request.getCompletionStatus());
		return toResponse(goalLogRepository.save(log));
	}

	@Transactional(readOnly = true)
	public List<GoalLogResponseDto> getLogs(Long goalId, Long userId) {
		goalService.validateOwner(goalId, userId);
		return goalLogRepository.findByGoalGoalIdOrderByLogDateDesc(goalId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	public void delete(Long goalId, LocalDate logDate, Long userId) {
		goalService.validateOwner(goalId, userId);
		goalLogRepository.findByGoalGoalIdAndLogDate(goalId, logDate)
				.ifPresent(goalLogRepository::delete);
	}

	private GoalLogResponseDto toResponse(GoalLog log) {
		return new GoalLogResponseDto(
				log.getLogId(),
				log.getGoal().getGoalId(),
				log.getLogDate(),
				log.getCompletionStatus(),
				log.getCreatedAt(),
				log.getUpdatedAt()
		);
	}
}
