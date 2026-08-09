package com.kyla.community.domain.goal.repository;

import com.kyla.community.domain.goal.dto.projection.GoalListRowDto;
import com.kyla.community.domain.goal.entity.Goal;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {
	@Query("""
			select g
			from Goal g
			join fetch g.stat
			where g.goalId = :goalId
			""")
	Optional<Goal> findDetailById(@Param("goalId") Long goalId);

	@Query("""
			select new com.kyla.community.domain.goal.dto.projection.GoalListRowDto(
				g.goalId,
				g.title,
				g.startDate,
				g.endDate,
				g.status,
				u.userId,
				u.nickname,
				s.viewCount,
				s.likeCount,
				g.createdAt
			)
			from Goal g
			join g.user u
			join g.stat s
			order by g.createdAt desc, g.goalId desc
			""")
	List<GoalListRowDto> findListRows(Pageable pageable);

	@Query("""
			select new com.kyla.community.domain.goal.dto.projection.GoalListRowDto(
				g.goalId,
				g.title,
				g.startDate,
				g.endDate,
				g.status,
				u.userId,
				u.nickname,
				s.viewCount,
				s.likeCount,
				g.createdAt
			)
			from Goal g
			join g.user u
			join g.stat s
			where g.createdAt < :cursorCreatedAt
			   or (g.createdAt = :cursorCreatedAt and g.goalId < :cursorGoalId)
			order by g.createdAt desc, g.goalId desc
			""")
	List<GoalListRowDto> findListRowsAfter(
			@Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
			@Param("cursorGoalId") Long cursorGoalId,
			Pageable pageable
	);

	@Query("""
			select new com.kyla.community.domain.goal.dto.projection.GoalListRowDto(
				g.goalId,
				g.title,
				g.startDate,
				g.endDate,
				g.status,
				u.userId,
				u.nickname,
				s.viewCount,
				s.likeCount,
				g.createdAt
			)
			from Goal g
			join g.user u
			join g.stat s
			where lower(g.title) like lower(concat('%', :keyword, '%'))
			   or lower(coalesce(g.description, '')) like lower(concat('%', :keyword, '%'))
			order by g.createdAt desc, g.goalId desc
			""")
	List<GoalListRowDto> searchRows(@Param("keyword") String keyword, Pageable pageable);

	@Query("""
			select new com.kyla.community.domain.goal.dto.projection.GoalListRowDto(
				g.goalId,
				g.title,
				g.startDate,
				g.endDate,
				g.status,
				u.userId,
				u.nickname,
				s.viewCount,
				s.likeCount,
				g.createdAt
			)
			from Goal g
			join g.user u
			join g.stat s
			where (
				g.createdAt < :cursorCreatedAt
				or (g.createdAt = :cursorCreatedAt and g.goalId < :cursorGoalId)
			)
			and (
				lower(g.title) like lower(concat('%', :keyword, '%'))
				or lower(coalesce(g.description, '')) like lower(concat('%', :keyword, '%'))
			)
			order by g.createdAt desc, g.goalId desc
			""")
	List<GoalListRowDto> searchRowsAfter(
			@Param("keyword") String keyword,
			@Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
			@Param("cursorGoalId") Long cursorGoalId,
			Pageable pageable
	);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select g from Goal g where g.goalId = :goalId")
	Optional<Goal> findByIdForUpdate(@Param("goalId") Long goalId);
}
