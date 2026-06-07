package com.kyla.community.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

// 다음 커서를 포함한 게시글 목록 응답
@Getter
@AllArgsConstructor
public class PostListResponseDto {
	private List<PostListItemResponseDto> posts;
	private LocalDateTime nextCursorCreatedAt;
	private Long nextCursorPostId;
	private boolean hasNext;
}
