package com.kyla.community.domain.image.entity;

import com.kyla.community.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
		name = "images",
		indexes = @Index(name = "idx_images_status_created", columnList = "status, created_at"),
		uniqueConstraints = @UniqueConstraint(
				name = "uk_images_path_name",
				columnNames = {"path", "name"}
		)
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INT UNSIGNED")
	private Long id;

	@Column(columnDefinition = "INT UNSIGNED")
	private Long uploaderId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ImagePurpose purpose;

	@Column(nullable = false, length = 100)
	private String path;

	@Column(nullable = false, length = 255)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ImageStatus status;

	private Image(
			Long uploaderId,
			ImagePurpose purpose,
			String path,
			String name,
			ImageStatus status
	) {
		this.uploaderId = uploaderId;
		this.purpose = purpose;
		this.path = path;
		this.name = name;
		this.status = status;
	}

	public static Image pending(
			Long uploaderId,
			ImagePurpose purpose,
			String path,
			String name
	) {
		return new Image(
				uploaderId,
				purpose,
				path,
				name,
				ImageStatus.PENDING
		);
	}

	public static Image uploaded(
			Long uploaderId,
			ImagePurpose purpose,
			String path,
			String name
	) {
		return new Image(
				uploaderId,
				purpose,
				path,
				name,
				ImageStatus.UPLOADED
		);
	}

	public String getObjectKey() {
		return path + name;
	}

	public void completeUpload() {
		if (status == ImageStatus.UPLOADED || status == ImageStatus.ATTACHED) {
			return;
		}
		if (status != ImageStatus.PENDING) {
			throw new IllegalStateException("업로드를 완료할 수 없는 이미지 상태입니다.");
		}
		status = ImageStatus.UPLOADED;
	}

	public void attach(ImagePurpose expectedPurpose, Long userId) {
		if (purpose != expectedPurpose) {
			throw new IllegalArgumentException("이미지 용도가 올바르지 않습니다.");
		}
		if (uploaderId != null && !uploaderId.equals(userId)) {
			throw new IllegalArgumentException("다른 사용자가 업로드한 이미지입니다.");
		}
		if (status != ImageStatus.UPLOADED) {
			throw new IllegalStateException("업로드가 완료된 이미지만 등록할 수 있습니다.");
		}
		status = ImageStatus.ATTACHED;
	}

	public void release() {
		if (status == ImageStatus.ATTACHED) {
			status = ImageStatus.UPLOADED;
		}
	}

	public void markDeleting() {
		if (status == ImageStatus.ATTACHED) {
			throw new IllegalStateException("연결된 이미지는 고아 이미지로 삭제할 수 없습니다.");
		}
		status = ImageStatus.DELETING;
	}

	public void markDeleteFailed() {
		if (status == ImageStatus.DELETING) {
			status = ImageStatus.DELETE_FAILED;
		}
	}
}
