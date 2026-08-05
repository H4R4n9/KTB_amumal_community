package com.kyla.community.domain.goal.entity;

import jakarta.persistence.*;
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
	@Column(
			name = "goal_id",
			columnDefinition = "INT UNSIGNED"
	)
	private Long goalId;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "goal_id", columnDefinition = "INT UNSIGNED")
	private Goal goal;

	@Column(nullable = false, columnDefinition = "BIGINT UNSIGNED")
	private long viewCount;

	@Column(nullable = false, columnDefinition = "BIGINT UNSIGNED")
	private long likeCount;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	GoalStat(Goal goal) {
		this.goal = goal;
	}

	@PrePersist
	@PreUpdate
	protected void recordUpdatedAt() {
		updatedAt = LocalDateTime.now();
	}
}
