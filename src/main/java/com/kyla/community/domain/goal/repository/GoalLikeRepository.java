package com.kyla.community.domain.goal.repository;

import com.kyla.community.domain.goal.entity.GoalLike;
import com.kyla.community.domain.goal.entity.GoalLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GoalLikeRepository extends JpaRepository<GoalLike, GoalLikeId> {
	boolean existsByGoalIdAndUserId(Long goalId, Long userId);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query(
			value = """
					insert ignore into goal_likes (goal_id, user_id, created_at)
					values (:goalId, :userId, current_timestamp(6))
					""",
			nativeQuery = true
	)
	int insertIgnore(@Param("goalId") Long goalId, @Param("userId") Long userId);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from GoalLike gl where gl.goalId = :goalId and gl.userId = :userId")
	int deleteByGoalIdAndUserId(@Param("goalId") Long goalId, @Param("userId") Long userId);
}
