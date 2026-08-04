package com.kyla.community.domain.goal.repository;

import com.kyla.community.domain.goal.entity.GoalStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GoalStatRepository extends JpaRepository<GoalStat, Long> {
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update GoalStat s
			set s.viewCount = s.viewCount + 1,
			    s.updatedAt = CURRENT_TIMESTAMP
			where s.goalId = :goalId
			""")
	int increaseViewCount(@Param("goalId") Long goalId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update GoalStat s
			set s.likeCount = s.likeCount + 1,
			    s.updatedAt = CURRENT_TIMESTAMP
			where s.goalId = :goalId
			""")
	int increaseLikeCount(@Param("goalId") Long goalId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update GoalStat s
			set s.likeCount = case when s.likeCount > 0 then s.likeCount - 1 else 0 end,
			    s.updatedAt = CURRENT_TIMESTAMP
			where s.goalId = :goalId
			""")
	int decreaseLikeCount(@Param("goalId") Long goalId);
}
