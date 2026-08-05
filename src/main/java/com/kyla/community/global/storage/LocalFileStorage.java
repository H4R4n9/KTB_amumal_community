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
import java.util.Locale;
import java.util.UUID;

@Service
public class LocalFileStorage implements FileStorage {
    @Value("${file.upload-dir:uploads}")
    private String uploadDirectory;

    @Override
    public StoredFile uploadImage(MultipartFile file, String directoryName) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 필요합니다.");
        }
		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
		}
		if (directoryName == null || !directoryName.matches("[a-z0-9-]+")) {
			throw new IllegalArgumentException("업로드 경로가 올바르지 않습니다.");
		}

        try {
            Path rootDirectory = Paths.get(uploadDirectory).toAbsolutePath().normalize();
			Path directory = rootDirectory.resolve(directoryName).normalize();
			if (!directory.startsWith(rootDirectory)) {
				throw new IllegalArgumentException("업로드 경로가 올바르지 않습니다.");
			}
            Files.createDirectories(directory);

			String extension = resolveExtension(file.getOriginalFilename(), contentType);
            String filename = UUID.randomUUID() + extension;
            Path targetPath = directory.resolve(filename).normalize();

            if (!targetPath.startsWith(directory)) {
                throw new IllegalArgumentException("파일 경로가 올바르지 않습니다.");
            }

            // 로컬 파일 저장
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
			String objectKey = directoryName + "/" + filename;
			return new StoredFile(objectKey, contentType, file.getSize());
        } catch (IOException e) {
            throw new IllegalStateException("파일 저장 실패", e);
        }
    }

	private String resolveExtension(String originalFilename, String contentType) {
		String filename = originalFilename == null ? "" : StringUtils.cleanPath(originalFilename);
		String extension = StringUtils.getFilenameExtension(filename);
		if (extension != null && extension.matches("[A-Za-z0-9]{1,10}")) {
			return "." + extension.toLowerCase(Locale.ROOT);
		}
		return switch (contentType.toLowerCase(Locale.ROOT)) {
			case "image/jpeg" -> ".jpg";
			case "image/png" -> ".png";
			case "image/gif" -> ".gif";
			case "image/webp" -> ".webp";
			default -> "";
		};
	}
}
