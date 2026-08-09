package com.kyla.community.domain.image.entity;

public enum ImagePurpose {
	GOAL("goals/"),
	PROFILE("profiles/");

	private final String path;
	ImagePurpose(String path) {
		this.path = path;
	}
	public String path() {
		return path;
	}
}
