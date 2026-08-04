package com.kyla.community.domain.goal.dto.req;

import com.kyla.community.domain.goal.entity.GoalStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CreateGoalRequestDto {
	@NotBlank
	@Size(max = 100)
	private String title;

	private String description;

	@NotNull
	private LocalDate startDate;

	private LocalDate endDate;

	private GoalStatus status;

	@Valid
	@Size(max = 10)
	private List<GoalImageRequestDto> images;
}
