package com.kyla.community.domain.goal.entity;

import com.kyla.community.global.entity.CreatedTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@IdClass(GoalLikeId.class)
@Table(name = "goal_likes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoalLike extends CreatedTimeEntity {
	@Id
	@Column(nullable = false, columnDefinition = "INT UNSIGNED")
	private Long goalId;

	@Id
	@Column(nullable = false, columnDefinition = "INT UNSIGNED")
	private Long userId;

	public GoalLike(Long goalId, Long userId) {
		this.goalId = goalId;
		this.userId = userId;
	}
}
