package com.kyla.community.domain.post.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 게시글 첨부파일 업로드 경로 응답
@Getter
@AllArgsConstructor
public class FileUploadResponseDto {
	private String filePath;
}
