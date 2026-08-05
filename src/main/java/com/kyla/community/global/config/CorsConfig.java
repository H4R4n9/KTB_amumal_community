package com.kyla.community.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
	private static final String DEFAULT_ALLOWED_ORIGIN = "https://kylamumal.cloud";
	private final String[] allowedOrigins;

	public CorsConfig(@Value("${app.cors.allowed-origins:" + DEFAULT_ALLOWED_ORIGIN + "}") String allowedOrigins) {
		String[] parsedOrigins = Arrays.stream(allowedOrigins.split(","))
				.map(String::trim)
				.filter(origin -> !origin.isBlank())
				.toArray(String[]::new);
		this.allowedOrigins = parsedOrigins.length > 0
				? parsedOrigins
				: new String[] { DEFAULT_ALLOWED_ORIGIN };
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins(allowedOrigins)
				.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
				.allowedHeaders("Authorization", "Content-Type")
				.allowCredentials(true)
				.maxAge(3600);
	}
}
