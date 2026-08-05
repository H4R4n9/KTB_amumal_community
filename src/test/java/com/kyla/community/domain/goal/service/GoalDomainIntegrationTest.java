package com.kyla.community.domain.goal.service;

import com.kyla.community.domain.goal.dto.req.CreateGoalRequestDto;
import com.kyla.community.domain.goal.dto.req.UpdateGoalLogRequestDto;
import com.kyla.community.domain.goal.entity.GoalLogStatus;
import com.kyla.community.domain.goal.entity.GoalStatus;
import com.kyla.community.domain.goal.repository.GoalLikeRepository;
import com.kyla.community.domain.goal.repository.GoalLogRepository;
import com.kyla.community.domain.goal.repository.GoalStatRepository;
import com.kyla.community.domain.user.entity.User;
import com.kyla.community.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class GoalDomainIntegrationTest {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private GoalService goalService;
	@Autowired
	private GoalLikeService goalLikeService;
	@Autowired
	private GoalLogService goalLogService;
	@Autowired
	private GoalStatRepository goalStatRepository;
	@Autowired
	private GoalLikeRepository goalLikeRepository;
	@Autowired
	private GoalLogRepository goalLogRepository;

	@Test
	void createsGoalStatsAndHandlesViewLikeAndDailyLogIdempotently() {
		User user = userRepository.save(new User("goal-user@example.com", "encoded-password", "goal-user"));
		LocalDate startDate = LocalDate.of(2026, 8, 3);

		Long goalId = goalService.create(user.getUserId(), new CreateGoalRequestDto(
				"매일 운동하기",
				"하루 30분 운동",
				startDate,
				startDate.plusMonths(1),
				GoalStatus.IN_PROGRESS,
				List.of()
		)).goalId();

		assertThat(goalStatRepository.findById(goalId)).get()
				.extracting("viewCount", "likeCount")
				.containsExactly(0L, 0L);

		assertThat(goalService.getDetail(goalId).viewCount()).isEqualTo(1L);

		goalLikeService.like(goalId, user.getUserId());
		goalLikeService.like(goalId, user.getUserId());
		assertThat(goalLikeRepository.count()).isEqualTo(1L);
		assertThat(goalStatRepository.findById(goalId).orElseThrow().getLikeCount()).isEqualTo(1L);

		goalLikeService.unlike(goalId, user.getUserId());
		goalLikeService.unlike(goalId, user.getUserId());
		assertThat(goalStatRepository.findById(goalId).orElseThrow().getLikeCount()).isZero();

		goalLogService.put(
				goalId,
				startDate,
				user.getUserId(),
				new UpdateGoalLogRequestDto(GoalLogStatus.COMPLETED)
		);
		goalLogService.put(
				goalId,
				startDate,
				user.getUserId(),
				new UpdateGoalLogRequestDto(GoalLogStatus.FAILED)
		);
		assertThat(goalLogRepository.count()).isEqualTo(1L);
		assertThat(goalLogRepository.findByGoalIdAndLogDate(goalId, startDate).orElseThrow().getCompletionStatus())
				.isEqualTo(GoalLogStatus.FAILED);
	}
}
