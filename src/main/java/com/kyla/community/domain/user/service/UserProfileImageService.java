package com.kyla.community.domain.user.service;

import com.kyla.community.domain.user.dto.res.ProfileUploadResponseDto;
import com.kyla.community.domain.user.entity.ProfileImage;
import com.kyla.community.domain.user.repository.ProfileImageRepository;
import com.kyla.community.global.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserProfileImageService {
    private final FileStorage fileStorage;
    private final ProfileImageRepository profileImageRepository;

    public ProfileUploadResponseDto upload(MultipartFile profileImage) {
        String filePath = fileStorage.uploadFile(profileImage);
        return new ProfileUploadResponseDto(filePath);
    }

    public void saveIfPresent(Long userId, String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }
        saveIfChanged(userId, filePath);
    }

    public void saveIfChanged(Long userId, String filePath) {
        if (filePath == null) {
            return;
        }
        if (Objects.equals(getLatestFilePath(userId), filePath)) {
            return;
        }
        profileImageRepository.save(new ProfileImage(userId, filePath));
    }

    public String getLatestFilePath(Long userId) {
        return profileImageRepository.findFirstByUserIdOrderByProfileImageIdDesc(userId)
                .map(ProfileImage::getFilePath)
                .orElse(null);
    }
}
