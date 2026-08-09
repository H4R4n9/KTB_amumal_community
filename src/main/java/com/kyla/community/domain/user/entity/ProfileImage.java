package com.kyla.community.domain.user.entity;

import com.kyla.community.domain.image.entity.Image;
import com.kyla.community.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
		name = "user_profile_images",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_user_profile_images_user", columnNames = "user_id"),
				@UniqueConstraint(name = "uk_user_profile_images_image", columnNames = "image_id")
		}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileImage extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INT UNSIGNED")
	private Long userProfileImageId;
	@Column(nullable = false, unique = true, columnDefinition = "INT UNSIGNED")
	private Long userId;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "image_id", nullable = false, unique = true, columnDefinition = "INT UNSIGNED")
	private Image image;

	public ProfileImage(Long userId, Image image) {
		this.userId = userId;
		this.image = image;
	}

	public void update(Image image) {
		this.image = image;
	}

	public String getObjectKey() {
		return image.getObjectKey();
	}
}
