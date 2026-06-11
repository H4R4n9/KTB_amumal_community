package com.kyla.community.domain.post.service;

import com.kyla.community.domain.post.dto.req.CreatePostRequestDto;
import com.kyla.community.domain.post.dto.res.FileUploadResponseDto;
import com.kyla.community.domain.post.dto.res.PostIdResponseDto;
import com.kyla.community.domain.post.dto.res.PostInfoResponseDto;
import com.kyla.community.domain.post.dto.req.UpdatePostRequestDto;
import com.kyla.community.domain.post.entity.Post;
import com.kyla.community.domain.post.repository.PostRepository;
import com.kyla.community.domain.user.service.UserService;
import com.kyla.community.domain.user.entity.User;
import com.kyla.community.global.exception.ApiException;
import com.kyla.community.global.security.AuthorizationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service // 게시글과 첨부파일 비즈니스 규칙 관리
@Transactional // 게시글 변경 작업의 트랜잭션 관리
@RequiredArgsConstructor
public class PostService {
	private final PostRepository postRepository;
	private final PostFileService postFileService;
	private final UserService userService;
	private final AuthorizationValidator authorizationValidator;

	// 작성자 검증을 포함한 게시글 생성
	public PostIdResponseDto create(Long userId, CreatePostRequestDto request) {
		userService.getActiveUser(userId);
		Post post = postRepository.save(new Post(userId, request.getPostTitle(), request.getPostContent()));
		return new PostIdResponseDto(post.getPostId());
	}

	// 게시글 상세 조회
	public PostInfoResponseDto getDetail(Long postId) {
		Post post = getActivePost(postId);
		return toDetailResponse(post);
	}

	// 작성자 권한 검증 후 게시글과 첨부파일 수정
	public PostIdResponseDto update(Long postId, Long userId, UpdatePostRequestDto request) {
		Post post = getActivePostForUpdate(postId);
		authorizationValidator.validateOwner(post.getUserId(), userId);
		post.update(request.getPostTitle(), request.getPostContent());
		return new PostIdResponseDto(post.getPostId());
	}

	// 작성자 권한 검증 후 첨부파일 업로드
	public FileUploadResponseDto uploadAttachFile(Long postId, Long userId, MultipartFile attachFile) {
		Post post = getActivePostForUpdate(postId);
		authorizationValidator.validateOwner(post.getUserId(), userId);
		return postFileService.upload(postId, attachFile);
	}

	// 작성자 권한 검증 후 게시글 소프트 삭제
	public void delete(Long postId, Long userId) {
		Post post = getActivePostForUpdate(postId);
		authorizationValidator.validateOwner(post.getUserId(), userId);
		post.delete();
	}

	// 삭제되지 않은 게시글 조회
	@Transactional(readOnly = true)
	public Post getActivePost(Long postId) {
		return postRepository.findByPostIdAndDeletedAtIsNull(postId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "요청한 게시글을 찾을 수 없습니다."));
	}

	// 첨부파일 순서 갱신을 위한 게시글 잠금 조회
	private Post getActivePostForUpdate(Long postId) {
		return postRepository.findActivePostForUpdate(postId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "요청한 게시글을 찾을 수 없습니다."));
	}

	// 게시글·작성자·첨부파일 상세 응답 조립
	private PostInfoResponseDto toDetailResponse(Post post) {
		User author = userService.getActiveUser(post.getUserId());
		return new PostInfoResponseDto(
				post.getPostId(),
				post.getPostTitle(),
				post.getPostContent(),
				author.getUserId(),
				author.getNickname(),
				post.getCreatedAt(),
				post.getUpdatedAt(),
				post.getDeletedAt(),
				0,
				0,
				0,
				postFileService.getFiles(post.getPostId())
		);
	}
}
