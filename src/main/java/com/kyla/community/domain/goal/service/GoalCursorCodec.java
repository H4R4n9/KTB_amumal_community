package com.kyla.community.domain.goal.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;

@Component
public class GoalCursorCodec {
	private static final String VERSION = "v1";
	private static final String DELIMITER = "|";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

	public String encode(LocalDateTime createdAt, Long goalId) {
		String value = String.join(
				DELIMITER,
				VERSION,
				createdAt.format(DATE_TIME_FORMATTER),
				goalId.toString()
		);
		return Base64.getUrlEncoder()
				.withoutPadding()
				.encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

	public Optional<GoalCursor> decode(String encodedCursor) {
		if (encodedCursor == null || encodedCursor.isBlank()) {
			return Optional.empty();
		}

		try {
			String decoded = new String(
					Base64.getUrlDecoder().decode(encodedCursor),
					StandardCharsets.UTF_8
			);
			String[] parts = decoded.split("\\|", -1);
			if (parts.length != 3 || !VERSION.equals(parts[0])) {
				throw new IllegalArgumentException();
			}

			return Optional.of(new GoalCursor(
					LocalDateTime.parse(parts[1], DATE_TIME_FORMATTER),
					Long.parseLong(parts[2])
			));
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("커서가 올바르지 않습니다.");
		}
	}

	public record GoalCursor(LocalDateTime createdAt, Long goalId) {
		public GoalCursor {
			if (createdAt == null || goalId == null || goalId < 1) {
				throw new IllegalArgumentException("커서가 올바르지 않습니다.");
			}
		}
	}
}
