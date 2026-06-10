package com.kyla.community.domain.auth;

import com.kyla.community.domain.auth.dto.LoginRequestDto;
import com.kyla.community.domain.auth.dto.LoginResponseDto;
import com.kyla.community.domain.auth.dto.TokenRefreshRequestDto;
import com.kyla.community.domain.auth.dto.TokenRefreshResponseDto;
import com.kyla.community.domain.auth.entity.RefreshToken;
import com.kyla.community.domain.auth.repository.RefreshTokenRepository;
import com.kyla.community.domain.user.repository.ProfileImageRepository;
import com.kyla.community.domain.user.entity.User;
import com.kyla.community.domain.user.repository.UserRepository;
import com.kyla.community.global.exception.ApiException;
import com.kyla.community.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

@Service // 로그인과 토큰 수명주기 관리
@Transactional // 인증 데이터 변경 작업의 트랜잭션 관리
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository userRepository;
	private final ProfileImageRepository profileImageRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final PasswordEncoder passwordEncoder;

	// 회원 인증과 JWT 및 리프레시 토큰 정보 저장
	public LoginResponseDto login(LoginRequestDto request) {
		User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));
		// BCrypt로 암호화한 비밀번호 검증
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
		}

		String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getEmail());
		String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId(), user.getEmail());
		refreshTokenRepository.save(new RefreshToken(
				user.getUserId(),
				hashToken(refreshToken),
				jwtTokenProvider.getRefreshTokenExpiration(refreshToken)
		));
		return new LoginResponseDto(
				user.getUserId(),
				user.getEmail(),
				user.getNickname(),
				profileImageRepository.findFirstByUserIdOrderByProfileImageIdDesc(user.getUserId())
						.map(profileImage -> profileImage.getFilePath())
						.orElse(null),
				accessToken,
				refreshToken,
				"Bearer",
				user.getCreatedAt(),
				user.getUpdatedAt()
		);
	}

	// 유효한 리프레시 토큰을 이용한 액세스 토큰 재발급
	public TokenRefreshResponseDto refreshAccessToken(TokenRefreshRequestDto request) {
		if (!jwtTokenProvider.isValidRefreshToken(request.getRefreshToken())) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "refresh token이 유효하지 않습니다.");
		}

		Long userId = jwtTokenProvider.getUserIdFromRefreshToken(request.getRefreshToken());
		refreshTokenRepository.findByTokenHash(hashToken(request.getRefreshToken()))
				.filter(RefreshToken::isUsable)
				.filter(token -> Objects.equals(token.getUserId(), userId))
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "refresh token이 유효하지 않습니다."));
		User user = userRepository.findById(userId)
				.filter(foundUser -> !foundUser.isDeleted())
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "refresh token이 유효하지 않습니다."));

		String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getEmail());
		return new TokenRefreshResponseDto(accessToken, "Bearer");
	}

	// 로그아웃 시에 저장된 refresh Token의 is_revoked = true로 변경
	public void logout(String refreshToken) {
		refreshTokenRepository.findByTokenHash(hashToken(refreshToken))
				.ifPresent(RefreshToken::revoke);
	}

	// refresh Token의 원문을 SHA-256 해시 변환하여 저장
	private String hashToken(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", exception);
		}
	}
}
