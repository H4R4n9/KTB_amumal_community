package com.kyla.community.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_profile_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileImage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INT UNSIGNED")
	private Long userProfileImageId;
	@Column(nullable = false, columnDefinition = "INT UNSIGNED")
	private Long userId;
	@Column(nullable = false, length = 512, unique = true)
	private String objectKey;
	@Column(nullable = false, length = 100)
	private String contentType;
	@Column(nullable = false, columnDefinition = "BIGINT UNSIGNED")
	private long fileSize;
	@Column(nullable = false, updatable = false)
	private java.time.LocalDateTime createdAt;
	@Column(nullable = false)
	private java.time.LocalDateTime updatedAt;

	public ProfileImage(Long userId, String objectKey, String contentType, long fileSize) {
		this.userId = userId;
		update(objectKey, contentType, fileSize);
	}

	public void update(String objectKey, String contentType, long fileSize) {
		this.objectKey = objectKey;
		this.contentType = contentType;
		this.fileSize = fileSize;
	}

	@jakarta.persistence.PrePersist
	void prePersist() {
		java.time.LocalDateTime now = java.time.LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
	}

	@jakarta.persistence.PreUpdate
	void preUpdate() {
		updatedAt = java.time.LocalDateTime.now();
	}
}
