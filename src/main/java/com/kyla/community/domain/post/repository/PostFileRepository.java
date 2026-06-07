package com.kyla.community.domain.post.repository;

import com.kyla.community.domain.post.entity.PostFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// 게시글 첨부파일 저장과 표시 순서 조회
public interface PostFileRepository extends JpaRepository<PostFile, Long> {
	// 게시글 첨부파일의 표시 순서 조회
	List<PostFile> findByPostIdOrderByFileOrderAsc(Long postId);

	// 신규 첨부파일 순번 계산용 마지막 순서 조회
	@Query("select coalesce(max(pf.fileOrder), 0) from PostFile pf where pf.postId = :postId")
	int findMaxFileOrderByPostId(@Param("postId") Long postId);
}
