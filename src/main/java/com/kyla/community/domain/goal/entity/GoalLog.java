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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@Entity
@Table(
		name = "goal_logs",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_goal_logs_goal_date",
				columnNames = {"goal_id", "log_date"}
		)
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoalLog extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INT UNSIGNED")
	private Long logId;

	@Column(nullable = false, columnDefinition = "INT UNSIGNED")
	private Long goalId;

	@Column(nullable = false)
	private LocalDate logDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "completion_status", nullable = false, length = 20)
	private GoalLogStatus completionStatus;

	public GoalLog(Long goalId, LocalDate logDate, GoalLogStatus completionStatus) {
		this.goalId = goalId;
		this.logDate = logDate;
		this.completionStatus = Objects.requireNonNull(completionStatus, "목표 기록 상태가 필요합니다.");
	}

	public void updateStatus(GoalLogStatus completionStatus) {
		this.completionStatus = Objects.requireNonNull(completionStatus, "목표 기록 상태가 필요합니다.");
	}
}
