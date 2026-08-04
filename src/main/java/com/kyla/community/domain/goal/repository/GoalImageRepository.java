package com.kyla.community.domain.goal.repository;

import com.kyla.community.domain.goal.entity.GoalImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GoalImageRepository extends JpaRepository<GoalImage, Long> {
	List<GoalImage> findByGoalIdOrderByDisplayOrderAsc(Long goalId);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from GoalImage gi where gi.goalId = :goalId")
	int deleteByGoalId(@Param("goalId") Long goalId);
}
