package com.kyla.community.domain.goal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "goal_stats")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoalStat {
	@Id
	@Column(columnDefinition = "INT UNSIGNED")
	private Long goalId;
	@Column(nullable = false, columnDefinition = "BIGINT UNSIGNED")
	private long viewCount;
	@Column(nullable = false, columnDefinition = "BIGINT UNSIGNED")
	private long likeCount;
	@Column(nullable = false)
	private LocalDateTime updatedAt;

	public GoalStat(Long goalId) {
		this.goalId = goalId;
	}

	@PrePersist
	@PreUpdate
	void updateTimestamp() {
		updatedAt = LocalDateTime.now();
	}
}
