package com.kyla.community.domain.goal.entity;

import com.kyla.community.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
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

	// goal과 단방향 연관관계
	// 장기 목표인 경우 하나의 goal_id에 여러 goal_log 가 발생할 수 있다.
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "goal_id", nullable = false, columnDefinition = "INT UNSIGNED")
	private Goal goal;

	@Column(nullable = false)
	private LocalDate logDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "completion_status", nullable = false, length = 20)
	private GoalLogStatus completionStatus;

	public GoalLog(Goal goal, LocalDate logDate, GoalLogStatus completionStatus) {
		this.goal = goal;
		this.logDate = logDate;
		this.completionStatus = Objects.requireNonNull(completionStatus, "목표 기록 상태가 필요합니다.");
	}

	public void updateStatus(GoalLogStatus completionStatus) {
		this.completionStatus = Objects.requireNonNull(completionStatus, "목표 기록 상태가 필요합니다.");
	}
}
