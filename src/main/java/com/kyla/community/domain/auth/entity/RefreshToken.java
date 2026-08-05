package com.kyla.community.domain.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity // 리프레시 토큰 해시와 만료 상태 저장
@Table(name = "refresh_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INT UNSIGNED")
	private Long refreshTokenId;
	@Column(nullable = false, columnDefinition = "INT UNSIGNED")
	private Long userId;
	@Column(nullable = false, unique = true, length = 64)
	private String tokenHash;
	@Column(nullable = false)
	private LocalDateTime expiresAt;
	private LocalDateTime revokedAt;
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public RefreshToken(Long userId, String tokenHash, LocalDateTime expiresAt) {
		this.userId = userId;
		this.tokenHash = tokenHash;
		this.expiresAt = expiresAt;
	}

	// 미폐기·미만료 토큰 여부 확인
	public boolean isUsable() {
		return revokedAt == null && expiresAt.isAfter(LocalDateTime.now());
	}

	// 로그아웃용 토큰 폐기 상태 변경
	public void revoke() {
		if (revokedAt == null) {
			revokedAt = LocalDateTime.now();
		}
	}

	@jakarta.persistence.PrePersist
	void prePersist() {
		createdAt = LocalDateTime.now();
	}
}
