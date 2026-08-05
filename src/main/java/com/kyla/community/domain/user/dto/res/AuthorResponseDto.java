package com.kyla.community.domain.user.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthorResponseDto {
	private Long userId;
	private String nickname;
	private String profileImageObjectKey;
}
