package com.kyla.community.domain.goal.repository;

import com.kyla.community.domain.goal.entity.GoalLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GoalLogRepository extends JpaRepository<GoalLog, Long> {
	Optional<GoalLog> findByGoalIdAndLogDate(Long goalId, LocalDate logDate);
	List<GoalLog> findByGoalIdOrderByLogDateDesc(Long goalId);
}
