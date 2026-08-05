package com.kyla.community.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // 엔티티 생성일과 수정일 공통 관리
public abstract class BaseTimeEntity extends CreatedTimeEntity {
	@Column(nullable = false)
	private LocalDateTime updatedAt;

	// 엔티티 최초 저장 시 생성·수정 시각 기록
	@PrePersist
	protected void initializeUpdatedAt() {
		updatedAt = LocalDateTime.now();
	}

	// 엔티티 변경 시 수정 시각 갱신
	@PreUpdate
	protected void recordUpdatedAt() {
		updatedAt = LocalDateTime.now();
	}
}
