package com.kyla.community.domain.user.service;

import com.kyla.community.domain.user.dto.res.ProfileUploadResponseDto;
import com.kyla.community.domain.user.entity.ProfileImage;
import com.kyla.community.domain.user.repository.ProfileImageRepository;
import com.kyla.community.global.storage.FileStorage;
import com.kyla.community.global.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserProfileImageService {
    private final FileStorage fileStorage;
    private final ProfileImageRepository profileImageRepository;

    public ProfileImage handleImageUpload(MultipartFile file, String type){
        throw new ApiException(HttpStatus.NOT_IMPLEMENTED, "프로필 이미지 업로드 기능은 준비 중입니다.");
    }

    public void saveIfPresent(Long userId, String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }
        profileImageRepository.save(new ProfileImage(userId, filePath));
    }

    public ProfileUploadResponseDto upload(Long userId, MultipartFile profileImage) {
        throw new ApiException(HttpStatus.NOT_IMPLEMENTED, "프로필 이미지 업로드 기능은 준비 중입니다.");
    }

    public String getLatestFilePath(Long userId) {
        return profileImageRepository.findFirstByUserIdOrderByProfileImageIdDesc(userId)
                .map(ProfileImage::getFilePath)
                .orElse(null);
    }
}
