package com.kyla.community.domain.auth.repository;

import com.kyla.community.domain.auth.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 이메일 인증번호 저장과 최신 발급 코드 조회
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
	Optional<VerificationCode> findFirstByEmailOrderByCreatedAtDesc(String email);
}
