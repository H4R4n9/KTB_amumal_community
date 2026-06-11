package com.kyla.community.global.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalFileStorage implements FileStorage {
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
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetPath = directory.resolve(filename);

            // 로컬 파일 저장
            Files.copy(file.getInputStream(), targetPath);
            return filename;
        } catch (IOException e) {
            throw new IllegalStateException("파일 저장 실패", e);
        }
    }
}
