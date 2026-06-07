package com.kyla.community.domain.like.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 좋아요 등록·취소 상태와 집계 응답
@Getter
@AllArgsConstructor
public class LikeResponseDto {
	private Long postId;
	private Long userId;
	private boolean liked;
	private long likeCount;
}
