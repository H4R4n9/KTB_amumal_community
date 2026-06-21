package com.kyla.community.domain.post.dto.res;

import com.kyla.community.domain.user.dto.res.AuthorResponseDto;
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
	private AuthorResponseDto author;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime deletedAt;
	private long like;
	private long commentCount;
	private long viewCount;
	private List<PostFileInfoResponseDto> files;

	public Long getId() {
		return postId;
	}

	public String getTitle() {
		return postTitle;
	}

	public String getContent() {
		return postContent;
	}

	public Long getWriterId() {
		return userId;
	}

	public String getProfileImagePath() {
		return author == null ? null : author.getProfileImagePath();
	}

	public long getLikeCount() {
		return like;
	}

	public String getFilePath() {
		if (files == null || files.isEmpty()) {
			return null;
		}
		return files.get(0).getFilePath();
	}
}
