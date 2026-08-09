package com.kyla.community.domain.goal.dto.res;

import java.util.List;

public record GoalCursorPageResponseDto(
		List<GoalListItemResponseDto> items,
		String nextCursor,
		boolean hasNext
) {
	public GoalCursorPageResponseDto {
		items = List.copyOf(items);
	}
}
