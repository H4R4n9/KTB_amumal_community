package com.kyla.community.domain.post.service;

import com.kyla.community.domain.post.dto.res.FileUploadResponseDto;
import com.kyla.community.domain.post.dto.res.PostFileInfoResponseDto;
import com.kyla.community.global.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class PostFileService {
	public FileUploadResponseDto upload(Long postId, MultipartFile attachFile) {
		throw new ApiException(HttpStatus.NOT_IMPLEMENTED, "게시글 첨부파일 업로드 기능은 준비 중입니다.");
	}

	public List<PostFileInfoResponseDto> getFiles(Long postId) {
		return List.of();
	}
}
