package com.kyla.community.domain.goal.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GoalImageRequestDto {
	@NotBlank
	@Size(max = 512)
	private String objectKey;

	@NotBlank
	@Size(max = 100)
	private String contentType;

	@Positive
	private long fileSize;

	@PositiveOrZero
	private int displayOrder;
}
