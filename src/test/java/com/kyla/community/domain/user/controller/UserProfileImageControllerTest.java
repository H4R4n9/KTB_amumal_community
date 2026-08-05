package com.kyla.community.domain.user.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "file.upload-dir=build/test-uploads/profile"
)
class UserProfileImageControllerTest {
	@LocalServerPort
	private int port;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void uploadProfileImageStoresFileAndServesItFromUploadsPath() throws Exception {
		byte[] imageBytes = new byte[] {
				(byte) 0x89, 0x50, 0x4E, 0x47,
				0x0D, 0x0A, 0x1A, 0x0A
		};
		String boundary = "profile-upload-boundary";
		HttpClient client = HttpClient.newHttpClient();

		HttpRequest uploadRequest = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + port + "/users/upload/profile-image"))
				.header("Content-Type", "multipart/form-data; boundary=" + boundary)
				.POST(HttpRequest.BodyPublishers.ofByteArray(buildMultipartBody(boundary, imageBytes)))
				.build();

		HttpResponse<String> uploadResponse = client.send(
				uploadRequest,
				HttpResponse.BodyHandlers.ofString()
		);

		assertThat(uploadResponse.statusCode()).isEqualTo(201);
		JsonNode responseBody = objectMapper.readTree(uploadResponse.body());
		String objectKey = responseBody.at("/data/objectKey").asText();

		assertThat(objectKey).startsWith("profiles/");
		assertThat(responseBody.at("/data/contentType").asText()).isEqualTo("image/png");
		assertThat(responseBody.at("/data/fileSize").asLong()).isEqualTo(imageBytes.length);

		HttpRequest fileRequest = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + port + "/uploads/" + objectKey))
				.GET()
				.build();

		HttpResponse<byte[]> fileResponse = client.send(
				fileRequest,
				HttpResponse.BodyHandlers.ofByteArray()
		);

		assertThat(fileResponse.statusCode()).isEqualTo(200);
		assertThat(fileResponse.body()).isEqualTo(imageBytes);
	}

	private byte[] buildMultipartBody(String boundary, byte[] imageBytes) throws Exception {
		ByteArrayOutputStream body = new ByteArrayOutputStream();
		body.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
		body.write((
				"Content-Disposition: form-data; name=\"profileImage\"; filename=\"profile.png\"\r\n"
						+ "Content-Type: image/png\r\n\r\n"
		).getBytes(StandardCharsets.UTF_8));
		body.write(imageBytes);
		body.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
		return body.toByteArray();
	}
}
