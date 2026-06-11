package com.kyla.community.domain.post.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

// 작성자·통계·첨부파일을 포함한 게시글 상세 응답
@Getter
@AllArgsConstructor
public class PostInfoResponseDto {
	private Long postId;
	private String postTitle;
	private String postContent;
	private Long userId;
	private String nickname;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime deletedAt;
	private long like;
	private long commentCount;
	private long viewCount;
	private List<PostFileInfoResponseDto> files;
}
