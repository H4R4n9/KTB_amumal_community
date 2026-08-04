package com.kyla.community.domain.goal.repository;

import com.kyla.community.domain.goal.entity.Goal;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {
	List<Goal> findAllByOrderByCreatedAtDesc(Pageable pageable);

	@Query("""
			select g
			from Goal g
			where lower(g.title) like lower(concat('%', :keyword, '%'))
			   or lower(coalesce(g.description, '')) like lower(concat('%', :keyword, '%'))
			order by g.createdAt desc
			""")
	List<Goal> search(@Param("keyword") String keyword, Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select g from Goal g where g.goalId = :goalId")
	Optional<Goal> findByIdForUpdate(@Param("goalId") Long goalId);
}
