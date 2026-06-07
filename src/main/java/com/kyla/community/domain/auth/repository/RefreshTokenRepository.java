package com.kyla.community.domain.auth.repository;

import com.kyla.community.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 리프레시 토큰 저장과 토큰 해시 조회
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	Optional<RefreshToken> findByTokenHash(String tokenHash);
}
