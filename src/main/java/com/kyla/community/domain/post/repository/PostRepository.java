package com.kyla.community.domain.post.repository;

import com.kyla.community.domain.post.dto.PostListItemResponseDto;
import com.kyla.community.domain.post.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import jakarta.persistence.LockModeType;

// 게시글 저장과 활성 게시글 조회
public interface PostRepository extends JpaRepository<Post, Long> {
	// 삭제되지 않은 게시글 단건 조회
	Optional<Post> findByPostIdAndDeletedAtIsNull(Long postId);
}
