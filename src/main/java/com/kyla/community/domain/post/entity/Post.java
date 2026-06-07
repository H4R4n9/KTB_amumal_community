package com.kyla.community.domain.post.entity;

import com.kyla.community.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity // 게시글 본문과 삭제 상태 저장
@Table(name = "posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INT UNSIGNED")
	private Long postId;
	@Column(nullable = false, columnDefinition = "INT UNSIGNED")
	private Long userId;
	@Column(nullable = false, length = 255)
	private String postTitle;
	@Column(nullable = false, columnDefinition = "TEXT")
	private String postContent;
	private LocalDateTime deletedAt;

	public Post(Long userId, String title, String content) {
		this.userId = userId;
		this.postTitle = title;
		this.postContent = content;
	}

	// 게시글 제목과 내용 변경
	public void update(String title, String content) {
		this.postTitle = title;
		this.postContent = content;
	}
	// 게시글 삭제 시각 기록
	public void delete() {deletedAt = LocalDateTime.now();}
	// 게시글 삭제 여부 확인
	public boolean isDeleted() {return deletedAt != null;}
}
