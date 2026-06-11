package com.kyla.community.domain.user.repository;

import com.kyla.community.domain.user.entity.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 프로필 이미지 저장과 회원별 최신 이미지 조회
public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
	Optional<ProfileImage> findFirstByUserIdOrderByProfileImageIdDesc(Long userId);
}
