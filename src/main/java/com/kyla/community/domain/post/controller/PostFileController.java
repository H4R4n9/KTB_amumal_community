package com.kyla.community.domain.post.controller;

import com.kyla.community.domain.post.dto.res.FileUploadResponseDto;
import com.kyla.community.domain.post.service.PostService;
import com.kyla.community.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/posts/upload/attach-file")
@RequiredArgsConstructor
public class PostFileController {
	private final PostService postService;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<FileUploadResponseDto>> uploadPostFile(
			@RequestPart("postFile") MultipartFile postFile
	) {
		FileUploadResponseDto response = postService.uploadPostFile(postFile);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(HttpStatus.CREATED.value(), "파일 업로드 성공", response));
	}
}
