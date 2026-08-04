package com.kyla.community.domain.goal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@IdClass(GoalLikeId.class)
@Table(name = "goal_likes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoalLike {
	@Id
	@Column(nullable = false, columnDefinition = "INT UNSIGNED")
	private Long goalId;

	@Id
	@Column(nullable = false, columnDefinition = "INT UNSIGNED")
	private Long userId;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public GoalLike(Long goalId, Long userId) {
		this.goalId = goalId;
		this.userId = userId;
	}

	@PrePersist
	void prePersist() {
		createdAt = LocalDateTime.now();
	}
}
