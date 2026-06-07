package com.kyla.community.domain.auth;

import com.kyla.community.domain.auth.dto.PasswordResetDto;
import com.kyla.community.domain.auth.dto.VerificationCodeRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // 비밀번호 재설정과 이메일 인증 흐름 관리
@Transactional // 비밀번호 재설정 데이터 변경 작업의 트랜잭션 관리
public class PasswordResetService {

	// 비밀번호 재설정 대상 이메일 확인
	@Transactional(readOnly = true)
	public void checkEmailExists(String email) {}
	// 비밀번호 재설정용 이메일 인증번호 발급
	public void sendVerificationCode(VerificationCodeRequestDto request) {}
	// 인증번호 검증 기반 비밀번호 재설정
	public void resetPassword(PasswordResetDto request) {}
}
