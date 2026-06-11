package com.kyla.community.domain.post.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 생성·수정된 게시글 식별자 응답
@Getter
@AllArgsConstructor
public class PostIdResponseDto {
	private Long postId;
}
