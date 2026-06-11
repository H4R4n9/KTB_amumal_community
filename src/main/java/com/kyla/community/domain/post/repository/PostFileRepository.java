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

}
