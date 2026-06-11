package com.kyla.community.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity // 이메일 인증번호와 만료 시각 저장
@Table(name = "verification_codes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerificationCode {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INT UNSIGNED")
	private Long verificationCodeId;
	@Column(nullable = false, length = 255)
	private String email;
	@Column(nullable = false, length = 6)
	private String code;
	@Column(nullable = false)
	private LocalDateTime expiresAt;
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public VerificationCode(String email, String code, LocalDateTime expiresAt) {
		this.email = email;
		this.code = code;
		this.expiresAt = expiresAt;
	}

	// 인증번호 일치와 만료 여부 확인
	public boolean matches(String code) {
		return this.code.equals(code) && expiresAt.isAfter(LocalDateTime.now());
	}

	// 인증번호 생성 시각 자동 기록
	@PrePersist
	void prePersist() {
		createdAt = LocalDateTime.now();
	}
}
