package com.kyla.community.domain.post.repository;

import com.kyla.community.domain.post.dto.PostListItemResponseDto;
import com.kyla.community.domain.post.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import jakarta.persistence.LockModeType;

// 게시글 저장과 활성 게시글 조회
public interface PostRepository extends JpaRepository<Post, Long> {
	// 삭제되지 않은 게시글 단건 조회
	Optional<Post> findByPostIdAndDeletedAtIsNull(Long postId);

	// 수정과 파일 순서 변경을 위한 활성 게시글 잠금 조회
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from Post p where p.postId = :postId and p.deletedAt is null")
	Optional<Post> findActivePostForUpdate(@Param("postId") Long postId);

	// 작성자·통계·대표 이미지를 포함한 커서 목록 조회
	@Query("""
			select new com.kyla.community.domain.post.dto.PostListItemResponseDto(
				p.postId,
				p.postTitle,
				u.userId,
				u.nickname,
				p.createdAt,
				coalesce(s.likeCount, 0L),
				coalesce(s.commentCount, 0L),
				coalesce(s.viewCount, 0L),
				coalesce(pf.thumbnailPath, pf.filePath)
			)
			from Post p
			join User u on u.userId = p.userId
			left join PostStat s on s.postId = p.postId
			left join PostFile pf on pf.postId = p.postId and pf.fileOrder = 1
			where p.deletedAt is null
			  and (
				:cursorCreatedAt is null
				or p.createdAt < :cursorCreatedAt
				or (p.createdAt = :cursorCreatedAt and p.postId < :cursorPostId)
			  )
			order by p.createdAt desc, p.postId desc
			""")
	Slice<PostListItemResponseDto> findPostList(
			@Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
			@Param("cursorPostId") Long cursorPostId,
			Pageable pageable
	);
}
