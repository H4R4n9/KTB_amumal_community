package com.kyla.community.domain.user.repository;

import com.kyla.community.domain.user.dto.projection.UserProfileImageDto;
import com.kyla.community.domain.user.entity.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

// 프로필 이미지 저장과 회원별 최신 이미지 조회
public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
	Optional<ProfileImage> findByUserId(Long userId);

	@Query("""
			select new com.kyla.community.domain.user.dto.projection.UserProfileImageDto(
				pi.userId,
				pi.objectKey
			)
			from ProfileImage pi
			where pi.userId in :userIds
			""")
	List<UserProfileImageDto> findResponsesByUserIds(@Param("userIds") Collection<Long> userIds);
}
