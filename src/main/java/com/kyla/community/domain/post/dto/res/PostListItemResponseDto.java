package com.kyla.community.domain.post.dto.res;

import com.kyla.community.domain.user.dto.res.AuthorResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// 게시글 목록의 단일 항목 응답
@Getter
@AllArgsConstructor
public class PostListItemResponseDto {
	private Long postId;
	private String postTitle;
	private Long userId;
	private String nickname;
	private AuthorResponseDto author;
	private LocalDateTime createdAt;
	private long likeCount;
	private long commentCount;
	private long viewCount;
	private String representativeImagePath;

	public Long getId() {
		return postId;
	}

	public String getTitle() {
		return postTitle;
	}
}
