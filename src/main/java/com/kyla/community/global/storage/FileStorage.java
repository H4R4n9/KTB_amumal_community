package com.kyla.community.global.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {
    String uploadFile(MultipartFile file);
}
