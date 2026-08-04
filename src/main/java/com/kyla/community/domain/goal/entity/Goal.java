package com.kyla.community.domain.goal.entity;

import com.kyla.community.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "goals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Goal extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INT UNSIGNED")
	private Long goalId;

	@Column(nullable = false, columnDefinition = "INT UNSIGNED")
	private Long userId;

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
			Long userId,
			String title,
			String description,
			LocalDate startDate,
			LocalDate endDate,
			GoalStatus status
	) {
		this.userId = userId;
		update(title, description, startDate, endDate, status);
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
