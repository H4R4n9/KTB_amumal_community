package com.kyla.community.domain.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// 댓글 저장과 게시글별 댓글 조회
public interface CommentRepository extends JpaRepository<Comment, Long> {
	// 게시글에 소속된 댓글 단건 조회
	Optional<Comment> findByCommentIdAndPostId(Long commentId, Long postId);
	// 게시글 댓글의 작성 시각순 조회
	List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
	// 게시글 댓글 수 집계
	long countByPostId(Long postId);
}
