package com.kyla.community.domain.goal.entity;

import com.kyla.community.domain.image.entity.Image;
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
				@UniqueConstraint(name = "uk_goal_images_image", columnNames = "image_id"),
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

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "image_id", nullable = false, unique = true, columnDefinition = "INT UNSIGNED")
	private Image image;

	@Column(nullable = false, columnDefinition = "SMALLINT UNSIGNED")
	private int displayOrder;

	public GoalImage(Image image, int displayOrder) {
		this.image = image;
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
