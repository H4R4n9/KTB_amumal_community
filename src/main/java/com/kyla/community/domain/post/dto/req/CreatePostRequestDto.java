package com.kyla.community.domain.post.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 제목·내용·선택 첨부파일을 포함한 게시글 생성 요청
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CreatePostRequestDto {
	@NotBlank
	private String postTitle;
	@NotBlank
	private String postContent;
	private String postFilePath;
}
