package com.kyla.community.domain.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// 작성자 정보를 포함한 댓글 조회 응답
@Getter
@AllArgsConstructor
public class CommentInfoResponseDto {
	private Long commentId;
	private String commentContent;
	private Long postId;
	private Long userId;
	private String nickname;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
