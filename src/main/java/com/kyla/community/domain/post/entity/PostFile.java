package com.kyla.community.domain.post.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity // 게시글 첨부파일 경로와 표시 순서 저장
@Table(
		name = "post_files",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_post_files_post_order",
				columnNames = {"post_id", "file_order"}
		)
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostFile {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INT UNSIGNED")
	private Long fileId;
	@Column(nullable = false, columnDefinition = "INT UNSIGNED")
	private Long postId;
	@Column(nullable = false, length = 500)
	private String filePath;
	@Column(nullable = false, columnDefinition = "SMALLINT UNSIGNED")
	private int fileOrder;
	@Column(length = 500)
	private String thumbnailPath;

	public PostFile(Long postId, String filePath, int fileOrder, String thumbnailPath) {
		this.postId = postId;
		this.filePath = filePath;
		this.fileOrder = fileOrder;
		this.thumbnailPath = thumbnailPath;
	}

	// 첫 번째 표시 파일의 대표 이미지 여부 확인
	public boolean isRepresentative() {
		return fileOrder == 1;
	}
}
