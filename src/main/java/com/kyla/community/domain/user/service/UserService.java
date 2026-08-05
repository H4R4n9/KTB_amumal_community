package com.kyla.community.domain.user.service;

import com.kyla.community.domain.user.dto.req.ChangePasswordRequestDto;
import com.kyla.community.domain.user.dto.res.AuthorResponseDto;
import com.kyla.community.domain.user.dto.res.ProfileUploadResponseDto;
import com.kyla.community.domain.user.dto.req.SignupRequestDto;
import com.kyla.community.domain.user.dto.res.SignupResponseDto;
import com.kyla.community.domain.user.dto.req.UpdateUserRequestDto;
import com.kyla.community.domain.user.dto.res.UserResponseDto;
import com.kyla.community.domain.user.entity.User;
import com.kyla.community.domain.user.repository.UserRepository;
import com.kyla.community.global.exception.ApiException;
import com.kyla.community.global.security.AuthorizationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Service // 회원 가입과 회원정보 관리
@Transactional // 회원 변경 작업의 트랜잭션 관리
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final UserProfileImageService userProfileImageService;
	private final AuthorizationValidator authorizationValidator;
	private final PasswordEncoder passwordEncoder;

	// 중복 검증과 (선택)프로필 이미지를 포함한 회원 생성
	public SignupResponseDto signUp(SignupRequestDto request) {
		validateEmailAvailable(request.getEmail());
		validateNicknameAvailable(request.getNickname());

		User user = new User(
				request.getEmail(),
				passwordEncoder.encode(request.getPassword()), // BCrypt로 비밃번호 암호화해서 DB 저장
				request.getNickname()
		);
		User savedUser = userRepository.save(user);
		userProfileImageService.saveIfPresent(
				savedUser.getUserId(),
				request.getProfileImageObjectKey(),
				request.getProfileImageContentType(),
				request.getProfileImageFileSize()
		);

		return new SignupResponseDto(savedUser.getUserId());
	}

	// 활동 중인 회원 기준 이메일 사용 가능 여부 검증
	@Transactional(readOnly = true)
	public void validateEmailAvailable(String email) {
		if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
			throw new ApiException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
		}
	}

	// 활동 중인 회원 기준 닉네임 사용 가능 여부 검증
	@Transactional(readOnly = true)
	public void validateNicknameAvailable(String nickname) {
		if (userRepository.existsByNicknameAndDeletedAtIsNull(nickname)) {
			throw new ApiException(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다.");
		}
	}

	// 닉네임 수정
	public void updateProfile(Long userId, Long loginUserId, UpdateUserRequestDto request) {
		authorizationValidator.validateOwner(userId, loginUserId);
		User user = getActiveUser(userId);
		if (!Objects.equals(user.getNickname(), request.getNickname())) {
			validateNicknameAvailable(request.getNickname());
		}
		user.updateNickname(request.getNickname());
		userProfileImageService.updateIfPresent(
				userId,
				request.getProfileImageObjectKey(),
				request.getProfileImageContentType(),
				request.getProfileImageFileSize()
		);
	}

	// 비밀번호 확인값 검증 후 비밀번호 변경
	public void changePassword(Long userId, ChangePasswordRequestDto request) {
		validatePasswordMatch(request.getNewPassword(), request.getNewPasswordCheck());
		getActiveUser(userId).updatePasswordHash(passwordEncoder.encode(request.getNewPassword()));
	}

	// 회원 생성·수정 전에 선택 프로필 이미지 업로드
	public ProfileUploadResponseDto uploadProfileImage(MultipartFile profileImage) {
		return userProfileImageService.upload(profileImage);
	}

	// 회원 탈퇴 시 삭제 시각 기록
	public void delete(Long userId, Long loginUserId) {
		authorizationValidator.validateOwner(userId, loginUserId);
		getActiveUser(userId).delete();
	}

	// 로그인 본인의 회원정보와 프로필 이미지 조회
	@Transactional(readOnly = true)
	public UserResponseDto getUser(Long userId, Long loginUserId) {
		authorizationValidator.validateOwner(userId, loginUserId);
		User user = getActiveUser(userId);
		return new UserResponseDto(
				user.getUserId(),
				user.getEmail(),
				user.getNickname(),
				getProfileImageObjectKey(userId),
				user.getCreatedAt(),
				user.getUpdatedAt()
		);
	}

	// 목표 작성자 표시 정보 조회
	@Transactional(readOnly = true)
	public AuthorResponseDto getAuthor(Long userId) {
		User user = getActiveUser(userId);
		return new AuthorResponseDto(
				user.getUserId(),
				user.getNickname(),
				getProfileImageObjectKey(userId)
		);
	}

	// 탈퇴하지 않은 회원 조회
	@Transactional(readOnly = true)
	public User getActiveUser(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "요청한 회원을 찾을 수 없습니다."));
		if (user.isDeleted()) {
			throw new ApiException(HttpStatus.NOT_FOUND, "요청한 회원을 찾을 수 없습니다.");
		}
		return user;
	}

	// 새 비밀번호와 확인값 일치 검증
	private void validatePasswordMatch(String password, String passwordCheck) {
		if (!Objects.equals(password, passwordCheck)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "비밀번호 확인이 일치하지 않습니다.");
		}
	}

	// 회원의 프로필 이미지 Object Key 조회
	@Transactional(readOnly = true)
	public String getProfileImageObjectKey(Long userId) {
		return userProfileImageService.getObjectKey(userId);
	}
}
