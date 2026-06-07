package com.kyla.community.domain.like.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 게시글 좋아요 수 조회 응답
@Getter
@AllArgsConstructor
public class LikeCountResponseDto {
	private Long postId;
	private long likeCount;
}
