package com.kyla.community.domain.user.entity;

import com.kyla.community.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity // 회원 계정 정보 저장
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INT UNSIGNED")
	private Long userId;
	@Column(nullable = false, unique = true, length = 255)
	private String email;
	@Column(nullable = false, length = 255)
	private String password;
	@Column(nullable = false, unique = true, length = 50)
	private String nickname;
	private LocalDateTime deletedAt;

	public User(String email, String password, String nickname) {
		this.email = email;
		this.password = password;
		this.nickname = nickname;
	}

	// 회원 닉네임 변경
	public void updateNickname(String nickname) {this.nickname = nickname;}
	// 회원 비밀번호 변경
	public void updatePassword(String password) {this.password = password;}
	// 회원 탈퇴 시각 기록
	public void delete() {deletedAt = LocalDateTime.now();}
	// 회원 탈퇴 여부 확인
	public boolean isDeleted() {return deletedAt != null;}
}
