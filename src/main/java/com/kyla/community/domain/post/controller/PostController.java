package com.kyla.community.domain.post.controller;

import com.kyla.community.domain.post.dto.req.CreatePostRequestDto;
import com.kyla.community.domain.post.dto.res.PostIdResponseDto;
import com.kyla.community.domain.post.dto.res.PostInfoResponseDto;
import com.kyla.community.domain.post.dto.req.UpdatePostRequestDto;
import com.kyla.community.domain.post.service.PostService;
import com.kyla.community.global.common.ApiResponse;
import com.kyla.community.global.filter.JwtAuthFilter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // 게시글 API 요청 처리
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
	private final PostService postService;

	// 게시글 상세 조회
	@GetMapping("/{postId}")
	public ResponseEntity<ApiResponse<PostInfoResponseDto>> getPost(@PathVariable Long postId) {
		PostInfoResponseDto response = postService.getDetail(postId);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "게시글 조회 성공", response));
	}

	// 로그인 회원의 게시글 생성
	@PostMapping
	public ResponseEntity<ApiResponse<PostIdResponseDto>> createPost(
			@RequestAttribute(JwtAuthFilter.LOGIN_USER_ID_ATTRIBUTE) Long userId,
			@Valid @RequestBody CreatePostRequestDto request
	) {
		PostIdResponseDto response = postService.create(userId, request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(HttpStatus.CREATED.value(), "게시글 작성 성공", response));
	}

	// 작성자 본인의 게시글 수정
	@PatchMapping("/{postId}")
	public ResponseEntity<ApiResponse<PostIdResponseDto>> updatePost(
			@PathVariable Long postId,
			@RequestAttribute(JwtAuthFilter.LOGIN_USER_ID_ATTRIBUTE) Long userId,
			@Valid @RequestBody UpdatePostRequestDto request
	) {
		PostIdResponseDto response = postService.update(postId, userId, request);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "게시글 수정 성공", response));
	}

	// 작성자 본인의 게시글 소프트 삭제
	@DeleteMapping("/{postId}")
	public ResponseEntity<ApiResponse<Void>> deletePost(
			@PathVariable Long postId,
			@RequestAttribute(JwtAuthFilter.LOGIN_USER_ID_ATTRIBUTE) Long userId
	) {
		postService.delete(postId, userId);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "게시글 삭제 성공", null));
	}
}
