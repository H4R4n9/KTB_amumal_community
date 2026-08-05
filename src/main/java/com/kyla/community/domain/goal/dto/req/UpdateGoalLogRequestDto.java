package com.kyla.community.domain.goal.dto.req;

import com.kyla.community.domain.goal.entity.GoalLogStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UpdateGoalLogRequestDto {
	@NotNull
	private GoalLogStatus completionStatus;
}
