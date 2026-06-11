package com.kyla.community.domain.post.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 표시 순서와 썸네일 정보를 포함한 첨부파일 응답
@Getter
@AllArgsConstructor
public class PostFileInfoResponseDto {
	private Long fileId;
	private String filePath;
	private int fileOrder;
	private boolean representative;
	private String thumbnailPath;
}
