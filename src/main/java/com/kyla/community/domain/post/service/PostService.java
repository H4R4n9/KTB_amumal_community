package com.kyla.community.domain.post.service;

import com.kyla.community.domain.post.dto.req.CreatePostRequestDto;
import com.kyla.community.domain.post.dto.res.FileUploadResponseDto;
import com.kyla.community.domain.post.dto.res.PostIdResponseDto;
import com.kyla.community.domain.post.dto.res.PostInfoResponseDto;
import com.kyla.community.domain.post.dto.req.UpdatePostRequestDto;
import com.kyla.community.domain.post.dto.res.PostListItemResponseDto;
import com.kyla.community.domain.post.entity.Post;
import com.kyla.community.domain.post.repository.PostRepository;
import com.kyla.community.domain.comment.repository.CommentRepository;
import com.kyla.community.domain.like.repository.LikeRepository;
import com.kyla.community.domain.user.service.UserService;
import com.kyla.community.domain.user.entity.User;
import com.kyla.community.domain.user.dto.res.AuthorResponseDto;
import com.kyla.community.global.exception.ApiException;
import com.kyla.community.global.security.AuthorizationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;

@Service // 게시글과 첨부파일 비즈니스 규칙 관리
@Transactional // 게시글 변경 작업의 트랜잭션 관리
@RequiredArgsConstructor
public class PostService {
	private final PostRepository postRepository;
	private final PostFileService postFileService;
	private final CommentRepository commentRepository;
	private final LikeRepository likeRepository;
	private final UserService userService;
	private final AuthorizationValidator authorizationValidator;

	// 작성자 검증을 포함한 게시글 생성
	public PostIdResponseDto create(Long userId, CreatePostRequestDto request) {
		userService.getActiveUser(userId);
		Post post = postRepository.save(new Post(userId, request.getPostTitle(), request.getPostContent()));
		postFileService.saveIfPresent(post.getPostId(), request.getPostFilePath());
		return new PostIdResponseDto(post.getPostId());
	}

	// 게시글 상세 조회
	public PostInfoResponseDto getDetail(Long postId) {
		Post post = getActivePost(postId);
		return toDetailResponse(post);
	}

	// 삭제되지 않은 게시글 목록 조회
	@Transactional(readOnly = true)
	public List<PostListItemResponseDto> getList(int offset, int limit) {
		Pageable pageable = PageRequest.of(offset / limit, limit);
		return postRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(pageable)
				.stream()
				.map(this::toListItemResponse)
				.toList();
	}

	// 제목·내용 기준 게시글 검색
	@Transactional(readOnly = true)
	public List<PostListItemResponseDto> search(String keyword, int offset, int limit, String sort) {
		Pageable pageable = PageRequest.of(offset / limit, limit);
		List<PostListItemResponseDto> posts = postRepository.searchActivePosts(keyword, pageable)
				.stream()
				.map(this::toListItemResponse)
				.toList();
		if ("popular".equalsIgnoreCase(sort)) {
			return posts.stream()
					.sorted(Comparator.comparingLong(PostListItemResponseDto::getLikeCount).reversed())
					.toList();
		}
		return posts;
	}

	// 작성자 권한 검증 후 게시글과 첨부파일 수정
	public PostIdResponseDto update(Long postId, Long userId, UpdatePostRequestDto request) {
		Post post = getActivePostForUpdate(postId);
		authorizationValidator.validateOwner(post.getUserId(), userId);
		post.update(request.getPostTitle(), request.getPostContent());
		postFileService.saveIfPresent(post.getPostId(), request.getPostFilePath());
		return new PostIdResponseDto(post.getPostId());
	}

	// 게시글 생성·수정 전에 선택 첨부파일 업로드
	public FileUploadResponseDto uploadPostFile(MultipartFile postFile) {
		return postFileService.upload(postFile);
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
		AuthorResponseDto authorResponse = toAuthorResponse(author);
		List<com.kyla.community.domain.post.dto.res.PostFileInfoResponseDto> files =
				postFileService.getFiles(post.getPostId());
		return new PostInfoResponseDto(
				post.getPostId(),
				post.getPostTitle(),
				post.getPostContent(),
				author.getUserId(),
				author.getNickname(),
				authorResponse,
				post.getCreatedAt(),
				post.getUpdatedAt(),
				post.getDeletedAt(),
				likeRepository.countByPostId(post.getPostId()),
				commentRepository.countByPostId(post.getPostId()),
				0,
				files
		);
	}

	private PostListItemResponseDto toListItemResponse(Post post) {
		User author = userService.getActiveUser(post.getUserId());
		AuthorResponseDto authorResponse = toAuthorResponse(author);
		String representativeImagePath = postFileService.getFiles(post.getPostId())
				.stream()
				.findFirst()
				.map(file -> file.getThumbnailPath() != null ? file.getThumbnailPath() : file.getFilePath())
				.orElse(null);
		return new PostListItemResponseDto(
				post.getPostId(),
				post.getPostTitle(),
				author.getUserId(),
				author.getNickname(),
				authorResponse,
				post.getCreatedAt(),
				likeRepository.countByPostId(post.getPostId()),
				commentRepository.countByPostId(post.getPostId()),
				0,
				representativeImagePath
		);
	}

	private AuthorResponseDto toAuthorResponse(User author) {
		return new AuthorResponseDto(
				author.getUserId(),
				author.getNickname(),
				userService.getProfileImagePath(author.getUserId())
		);
	}
}
