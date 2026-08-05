package com.kyla.community.domain.goal.entity;

import com.kyla.community.global.entity.CreatedTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
public class GoalImage extends CreatedTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INT UNSIGNED")
	private Long goalImageId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "goal_id", nullable = false, columnDefinition = "INT UNSIGNED")
	private Goal goal;

	@Column(nullable = false, unique = true, length = 512)
	private String objectKey;

	@Column(nullable = false, length = 100)
	private String contentType;

	@Column(nullable = false, columnDefinition = "BIGINT UNSIGNED")
	private long fileSize;

	@Column(nullable = false, columnDefinition = "SMALLINT UNSIGNED")
	private int displayOrder;

	public GoalImage(String objectKey, String contentType, long fileSize, int displayOrder) {
		this.objectKey = objectKey;
		this.contentType = contentType;
		this.fileSize = fileSize;
		this.displayOrder = displayOrder;
	}

	// goal과 image 양방향 연관관계
	void assignGoal(Goal goal) {
		this.goal = goal;
	}

	void removeGoal() {
		this.goal = null;
	}
}
