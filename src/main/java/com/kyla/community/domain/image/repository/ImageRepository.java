package com.kyla.community.domain.image.repository;

import com.kyla.community.domain.image.entity.Image;
import com.kyla.community.domain.image.entity.ImageStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
	Optional<Image> findByPathAndName(String path, String name);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select i from Image i where i.path = :path and i.name = :name")
	Optional<Image> findByPathAndNameForUpdate(
			@Param("path") String path,
			@Param("name") String name
	);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select i from Image i where i.id = :imageId")
	Optional<Image> findByIdForUpdate(@Param("imageId") Long imageId);

	@Query("""
			select i.id
			from Image i
			where i.status in :statuses
			  and i.createdAt < :threshold
			  and not exists (select gi.goalImageId from GoalImage gi where gi.image = i)
			  and not exists (select pi.userProfileImageId from ProfileImage pi where pi.image = i)
			order by i.createdAt asc
			""")
	List<Long> findOrphanIds(
			@Param("statuses") Collection<ImageStatus> statuses,
			@Param("threshold") LocalDateTime threshold,
			Pageable pageable
	);

	@Query("""
			select case when count(i) > 0 then true else false end
			from Image i
			where i.id = :imageId
			  and (
			    exists (select gi.goalImageId from GoalImage gi where gi.image = i)
			    or exists (select pi.userProfileImageId from ProfileImage pi where pi.image = i)
			  )
			""")
	boolean isReferenced(@Param("imageId") Long imageId);
}
