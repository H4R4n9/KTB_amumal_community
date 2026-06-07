package com.kyla.community.domain.post.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity // 게시글 조회수와 집계 수치 저장
@Table(name = "post_stats")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostStat {
	@Id
	@Column(columnDefinition = "INT UNSIGNED")
	private Long postId;
	@Column(nullable = false, columnDefinition = "BIGINT UNSIGNED")
	private long viewCount;
	@Column(nullable = false, columnDefinition = "BIGINT UNSIGNED")
	private long likeCount;
	@Column(nullable = false, columnDefinition = "BIGINT UNSIGNED")
	private long commentCount;

	public PostStat(Long postId) {
		this.postId = postId;
	}

	// 게시글 조회수 1 증가
	public void increaseViewCount() {
		viewCount++;
	}

	// 게시글 집계 수치 일괄 갱신
	public void refresh(long viewCount, long likeCount, long commentCount) {
		this.viewCount = viewCount;
		this.likeCount = likeCount;
		this.commentCount = commentCount;
	}
}
