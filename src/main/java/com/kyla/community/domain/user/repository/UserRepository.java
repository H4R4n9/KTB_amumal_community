package com.kyla.community.domain.user.repository;

import com.kyla.community.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// 회원 저장과 활성 회원 조회
public interface UserRepository extends JpaRepository<User, Long> {
	// 활성 회원의 이메일 중복 확인
	boolean existsByEmailAndDeletedAtIsNull(String email);

	// 활성 회원의 닉네임 중복 확인
	boolean existsByNicknameAndDeletedAtIsNull(String nickname);

	// 이메일 기반 활성 회원 조회
	Optional<User> findByEmailAndDeletedAtIsNull(String email);
}
