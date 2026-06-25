package com.kyla.community.domain.post.repository;

import com.kyla.community.domain.post.entity.Post;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// 게시글 저장과 활성 게시글 조회
public interface PostRepository extends JpaRepository<Post, Long> {
	// 삭제되지 않은 게시글 단건 조회
	Optional<Post> findByPostIdAndDeletedAtIsNull(Long postId);

	// 삭제되지 않은 게시글 목록 조회
	List<Post> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

	// 제목 또는 내용 기준 게시글 검색
	@Query("""
			select p
			from Post p
			where p.deletedAt is null
			  and (
			    lower(p.postTitle) like lower(concat('%', :keyword, '%'))
			    or lower(p.postContent) like lower(concat('%', :keyword, '%'))
			  )
			order by p.createdAt desc
			""")
	List<Post> searchActivePosts(@Param("keyword") String keyword, Pageable pageable);

	// 수정 작업 중 동일 게시글의 동시 변경 방지
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from Post p where p.postId = :postId and p.deletedAt is null")
	Optional<Post> findActivePostForUpdate(@Param("postId") Long postId);
}
