package com.kyla.community.domain.post.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

// 다음 페이지 번호를 포함한 게시글 목록 응답
@Getter
@AllArgsConstructor
public class PostListResponseDto {
	private List<PostListItemResponseDto> posts;
	private Integer nextPage;
	private boolean hasNext;
}
