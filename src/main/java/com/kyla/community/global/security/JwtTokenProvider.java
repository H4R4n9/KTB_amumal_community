package com.kyla.community.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component // JWT 생성과 서명 및 토큰 검증
public class JwtTokenProvider {
	private static final String TOKEN_TYPE_CLAIM = "type";
	private static final String ACCESS_TOKEN_TYPE = "access";
	private static final String REFRESH_TOKEN_TYPE = "refresh";

	private final SecretKey secretKey;
	private final long accessTokenExpirationMillis;
	private final long refreshTokenExpirationMillis;

	// 환경 설정 기반 JWT 서명키와 만료 시간 초기화
	public JwtTokenProvider(
			@Value("${security.jwt.secret}") String secret,
			@Value("${security.jwt.access-token-expiration-seconds}") long accessTokenExpirationSeconds,
			@Value("${security.jwt.refresh-token-expiration-seconds}") long refreshTokenExpirationSeconds
	) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.accessTokenExpirationMillis = accessTokenExpirationSeconds * 1000;
		this.refreshTokenExpirationMillis = refreshTokenExpirationSeconds * 1000;
	}

	// API 인증용 액세스 토큰 생성
	public String createAccessToken(Long userId, String email) {
		return createToken(userId, email, ACCESS_TOKEN_TYPE, accessTokenExpirationMillis);
	}

	// 액세스 토큰 재발급용 리프레시 토큰 생성
	public String createRefreshToken(Long userId, String email) {
		return createToken(userId, email, REFRESH_TOKEN_TYPE, refreshTokenExpirationMillis);
	}

	// 사용자·토큰 유형·만료 시간을 포함한 서명 JWT 생성
	private String createToken(Long userId, String email, String tokenType, long expirationMillis) {
		Date now = new Date();

		return Jwts.builder()
				.id(UUID.randomUUID().toString())
				.subject(String.valueOf(userId))
				.claim("email", email)
				.claim(TOKEN_TYPE_CLAIM, tokenType)
				.issuedAt(now)
				.expiration(new Date(now.getTime() + expirationMillis))
				.signWith(secretKey)
				.compact();
	}

	// 액세스 토큰의 서명·만료·유형 검증
	public boolean isValidAccessToken(String token) {
		return isValid(token, ACCESS_TOKEN_TYPE);
	}

	// 리프레시 토큰의 서명·만료·유형 검증
	public boolean isValidRefreshToken(String token) {
		return isValid(token, REFRESH_TOKEN_TYPE);
	}

	// 액세스 토큰 subject의 회원 ID 추출
	public Long getUserIdFromAccessToken(String token) {
		return getUserId(token);
	}

	// 리프레시 토큰 subject의 회원 ID 추출
	public Long getUserIdFromRefreshToken(String token) {
		return getUserId(token);
	}

	// 리프레시 토큰 만료 시각 추출
	public LocalDateTime getRefreshTokenExpiration(String token) {
		return getClaims(token).getExpiration().toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime();
	}

	// JWT 파싱과 예상 토큰 유형 비교
	private boolean isValid(String token, String expectedType) {
		try {
			Claims claims = getClaims(token);
			return Objects.equals(expectedType, claims.get(TOKEN_TYPE_CLAIM, String.class));
		} catch (Exception exception) {
			return false;
		}
	}

	// JWT subject의 회원 ID 변환
	private Long getUserId(String token) {
		return Long.valueOf(getClaims(token).getSubject());
	}

	// JWT 서명과 만료 검증 후 Claims 반환
	private Claims getClaims(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
