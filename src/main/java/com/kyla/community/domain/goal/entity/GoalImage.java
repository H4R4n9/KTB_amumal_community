package com.kyla.community.domain.goal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
		name = "goal_images",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_goal_images_object_key", columnNames = "object_key"),
				@UniqueConstraint(name = "uk_goal_images_goal_order", columnNames = {"goal_id", "display_order"})
		}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoalImage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INT UNSIGNED")
	private Long goalImageId;

	@Column(nullable = false, columnDefinition = "INT UNSIGNED")
	private Long goalId;

	@Column(nullable = false, unique = true, length = 512)
	private String objectKey;

	@Column(nullable = false, length = 100)
	private String contentType;

	@Column(nullable = false, columnDefinition = "BIGINT UNSIGNED")
	private long fileSize;

	@Column(nullable = false, columnDefinition = "SMALLINT UNSIGNED")
	private int displayOrder;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public GoalImage(Long goalId, String objectKey, String contentType, long fileSize, int displayOrder) {
		this.goalId = goalId;
		this.objectKey = objectKey;
		this.contentType = contentType;
		this.fileSize = fileSize;
		this.displayOrder = displayOrder;
	}

	@PrePersist
	void prePersist() {
		createdAt = LocalDateTime.now();
	}
}
