package com.kyla.community.global.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {
	StoredFile uploadImage(MultipartFile file, String directory);
}
