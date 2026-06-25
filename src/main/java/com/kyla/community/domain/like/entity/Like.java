package com.kyla.community.domain.like.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity // 사용자와 게시글의 좋아요 관계 저장
@Table(
		name = "post_likes",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_post_likes_post_user",
				columnNames = {"post_id", "user_id"}
		)
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INT UNSIGNED")
	private Long likeId;
	@Column(nullable = false, columnDefinition = "INT UNSIGNED")
	private Long postId;
	@Column(nullable = false, columnDefinition = "INT UNSIGNED")
	private Long userId;

	public Like(Long postId, Long userId) {
		this.postId = postId;
		this.userId = userId;
	}
}
