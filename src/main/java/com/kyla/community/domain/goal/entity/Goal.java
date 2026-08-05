package com.kyla.community.domain.goal.entity;

import com.kyla.community.domain.user.entity.User;
import com.kyla.community.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "goals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Goal extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INT UNSIGNED")
	private Long goalId;

	// goals 테이블에서 user_id 와 Many to one 관계로 단방향 참조
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, columnDefinition = "INT UNSIGNED")
	private User user;

	@OneToMany(
			mappedBy = "goal",
			fetch = FetchType.LAZY,
			cascade = CascadeType.ALL,
			orphanRemoval = true
	)
	@OrderBy("displayOrder ASC")
	private List<GoalImage> images = new ArrayList<>();

	@OneToOne(
			mappedBy = "goal",
			fetch = FetchType.LAZY,
			cascade = CascadeType.ALL,
			orphanRemoval = true
	)
	private GoalStat stat;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false)
	private LocalDate startDate;

	private LocalDate endDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private GoalStatus status;

	public Goal(
			User user,
			String title,
			String description,
			LocalDate startDate,
			LocalDate endDate,
			GoalStatus status
	) {
		this.user = user;
		update(title, description, startDate, endDate, status);
	}

	// goal과 stat, goal과 image는 양방향 연관관계를 갖고 있기 때문에 양쪽 객체를 모두 연결해야한다.
	public void initializeStat() {
		this.stat = new GoalStat(this);
	}

	public void addImage(GoalImage image) {
		images.add(image);
		image.assignGoal(this);
	}

	public void removeImage(GoalImage image) {
		images.remove(image);
		image.removeGoal();
	}

	public void clearImages() {
		new ArrayList<>(images).forEach(this::removeImage);
	}

	public void update(
			String title,
			String description,
			LocalDate startDate,
			LocalDate endDate,
			GoalStatus status
	) {
		validateDates(startDate, endDate);
		this.title = title;
		this.description = description;
		this.startDate = startDate;
		this.endDate = endDate;
		this.status = status == null ? GoalStatus.IN_PROGRESS : status;
	}

	private void validateDates(LocalDate startDate, LocalDate endDate) {
		if (startDate == null) {
			throw new IllegalArgumentException("목표 시작일이 필요합니다.");
		}
		if (endDate != null && endDate.isBefore(startDate)) {
			throw new IllegalArgumentException("목표 종료일은 시작일보다 빠를 수 없습니다.");
		}
	}

}
