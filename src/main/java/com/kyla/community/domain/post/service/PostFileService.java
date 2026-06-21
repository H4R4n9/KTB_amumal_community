package com.kyla.community.domain.post.service;

import com.kyla.community.domain.post.dto.res.FileUploadResponseDto;
import com.kyla.community.domain.post.dto.res.PostFileInfoResponseDto;
import com.kyla.community.domain.post.entity.PostFile;
import com.kyla.community.domain.post.repository.PostFileRepository;
import com.kyla.community.global.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostFileService {
	private final FileStorage fileStorage;
	private final PostFileRepository postFileRepository;

	public FileUploadResponseDto upload(MultipartFile postFile) {
		String filePath = fileStorage.uploadFile(postFile);
		return new FileUploadResponseDto(filePath);
	}

	public void saveIfPresent(Long postId, String postFilePath) {
		if (postFilePath == null || postFilePath.isBlank()) {
			return;
		}
		int fileOrder = postFileRepository.findByPostIdOrderByFileOrderAsc(postId).size() + 1;
		postFileRepository.save(new PostFile(postId, postFilePath, fileOrder, null));
	}

	public List<PostFileInfoResponseDto> getFiles(Long postId) {
		return postFileRepository.findByPostIdOrderByFileOrderAsc(postId)
				.stream()
				.map(postFile -> new PostFileInfoResponseDto(
						postFile.getFileId(),
						postFile.getFilePath(),
						postFile.getFileOrder(),
						postFile.isRepresentative(),
						postFile.getThumbnailPath()
				))
				.toList();
	}
}
