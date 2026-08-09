package com.kyla.community.domain.goal.repository;

import com.kyla.community.domain.goal.dto.projection.GoalRepresentativeImageDto;
import com.kyla.community.domain.goal.dto.res.GoalImageResponseDto;
import com.kyla.community.domain.goal.entity.GoalImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GoalImageRepository extends JpaRepository<GoalImage, Long> {
	List<GoalImage> findByGoalGoalIdOrderByDisplayOrderAsc(Long goalId);

	@Query("""
			select new com.kyla.community.domain.goal.dto.res.GoalImageResponseDto(
				gi.goalImageId,
				i.id,
				concat(i.path, i.name),
				gi.displayOrder
			)
			from GoalImage gi
			join gi.image i
			where gi.goal.goalId = :goalId
			order by gi.displayOrder asc
			""")
	List<GoalImageResponseDto> findResponsesByGoalId(@Param("goalId") Long goalId);

	@Query("""
			select new com.kyla.community.domain.goal.dto.projection.GoalRepresentativeImageDto(
				gi.goal.goalId,
				concat(i.path, i.name)
			)
			from GoalImage gi
			join gi.image i
			where gi.goal.goalId in :goalIds
			  and gi.displayOrder = 0
			""")
	List<GoalRepresentativeImageDto> findRepresentativesByGoalIds(
			@Param("goalIds") List<Long> goalIds
	);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from GoalImage gi where gi.goal.goalId = :goalId")
	int deleteByGoalId(@Param("goalId") Long goalId);
}
