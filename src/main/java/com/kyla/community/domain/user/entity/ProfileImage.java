package com.kyla.community.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity // 회원 프로필 이미지 경로 저장
@Table(name = "profile_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileImage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INT UNSIGNED")
	private Long profileImageId;
	@Column(nullable = false, columnDefinition = "INT UNSIGNED")
	private Long userId;
	@Column(nullable = false, length = 500)
	private String filePath;

	public ProfileImage(Long userId, String filePath) {
		this.userId = userId;
		this.filePath = filePath;
	}

	// 프로필 이미지 파일 경로 변경
	public void updateFilePath(String filePath) {
		this.filePath = filePath;
	}
}
