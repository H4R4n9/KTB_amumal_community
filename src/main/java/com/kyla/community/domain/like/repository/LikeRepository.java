package com.kyla.community.domain.like.repository;

import com.kyla.community.domain.like.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 게시글 좋아요 저장과 사용자별 좋아요 조회
public interface LikeRepository extends JpaRepository<Like, Long> {
	// 사용자와 게시글 조합의 좋아요 조회
	Optional<Like> findByPostIdAndUserId(Long postId, Long userId);
	// 사용자 중복 좋아요 확인
	boolean existsByPostIdAndUserId(Long postId, Long userId);
	// 게시글 좋아요 수 집계
	long countByPostId(Long postId);
}
