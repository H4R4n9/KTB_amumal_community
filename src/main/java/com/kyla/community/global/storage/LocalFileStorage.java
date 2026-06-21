package com.kyla.community.global.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalFileStorage implements FileStorage {
    private static final String PUBLIC_UPLOAD_PATH = "/uploads/";

    @Value("${file.upload-dir:uploads}")
    private String uploadDirectory;

    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 필요합니다.");
        }

        try {
            // 로컬 파일 디렉토리가 없는 경우 생성
            Path directory = Paths.get(uploadDirectory).toAbsolutePath().normalize();
            Files.createDirectories(directory);

            // 로컬 파일명 충돌 방지;
            String originalFilename = file.getOriginalFilename() == null
                    ? "file"
                    : StringUtils.cleanPath(file.getOriginalFilename());
            String safeFilename = Paths.get(originalFilename).getFileName().toString();
            if (safeFilename.isBlank()) {
                safeFilename = "file";
            }
            String filename = UUID.randomUUID() + "_" + safeFilename;
            Path targetPath = directory.resolve(filename).normalize();

            if (!targetPath.startsWith(directory)) {
                throw new IllegalArgumentException("파일 경로가 올바르지 않습니다.");
            }

            // 로컬 파일 저장
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return PUBLIC_UPLOAD_PATH + filename;
        } catch (IOException e) {
            throw new IllegalStateException("파일 저장 실패", e);
        }
    }
} 
