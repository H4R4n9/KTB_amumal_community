package com.kyla.community.domain.post.repository;

import com.kyla.community.domain.post.entity.PostStat;
import org.springframework.data.jpa.repository.JpaRepository;

// 게시글별 통계 저장과 조회
public interface PostStatRepository extends JpaRepository<PostStat, Long> {
}
