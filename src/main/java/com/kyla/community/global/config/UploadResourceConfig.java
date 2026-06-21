package com.kyla.community.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {
	@Value("${file.upload-dir:uploads}")
	private String uploadDirectory;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();
		registry.addResourceHandler("/uploads/**")
				.addResourceLocations(uploadPath.toUri().toString());
	}
}
