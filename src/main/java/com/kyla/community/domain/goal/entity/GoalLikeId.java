package com.kyla.community.domain.goal.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class GoalLikeId implements Serializable {
	private Long goalId;
	private Long userId;
}
