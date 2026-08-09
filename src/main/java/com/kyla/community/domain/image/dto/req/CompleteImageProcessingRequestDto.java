package com.kyla.community.domain.image.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompleteImageProcessingRequestDto(
		@NotBlank @Size(max = 512) String
		objectKey
) {
}
