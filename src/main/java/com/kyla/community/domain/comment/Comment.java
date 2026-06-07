package com.kyla.community.domain.comment;

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

@Getter
@Entity // 게시글 댓글 내용과 작성자 정보 저장
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INT UNSIGNED")
	private Long commentId;
	@Column(nullable = false, columnDefinition = "INT UNSIGNED")
	private Long postId;
	@Column(nullable = false, columnDefinition = "INT UNSIGNED")
	private Long userId;
	@Column(nullable = false, columnDefinition = "TEXT")
	private String commentContent;

	public Comment(Long postId, Long userId, String content) {
		this.postId = postId;
		this.userId = userId;
		this.commentContent = content;
	}

	// 댓글 내용 변경
	public void update(String content) {
		this.commentContent = content;
	}
}
