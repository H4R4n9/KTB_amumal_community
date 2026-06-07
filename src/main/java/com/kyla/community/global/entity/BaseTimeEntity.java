package com.kyla.community.global.entity;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // 엔티티 생성일과 수정일 공통 관리
public abstract class BaseTimeEntity {
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	// 엔티티 최초 저장 시 생성·수정 시각 기록
	@PrePersist
	void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
	}

	// 엔티티 변경 시 수정 시각 갱신
	@PreUpdate
	void preUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
