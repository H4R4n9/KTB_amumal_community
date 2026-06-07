package com.kyla.community.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyla.community.global.common.ApiResponse;
import com.kyla.community.global.security.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
	public static final String LOGIN_USER_ID_ATTRIBUTE = "loginUserId";
	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;
	private final ObjectMapper objectMapper = new ObjectMapper();

	// 공개 요청 통과와 보호 요청의 액세스 토큰 검증
	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain chain
	) throws IOException, ServletException {
		Optional<String> token = extractToken(request);

		if (token.isEmpty()) {
			if (isPublicRequest(request)) {
				chain.doFilter(request, response);
			} else {
				writeUnauthorizedResponse(response, "인증 토큰이 필요합니다.");
			}
			return;
		}

		if (!jwtTokenProvider.isValidAccessToken(token.get())) {
			writeUnauthorizedResponse(response, "인증 토큰이 유효하지 않습니다.");
			return;
		}

		request.setAttribute(
				LOGIN_USER_ID_ATTRIBUTE,
				jwtTokenProvider.getUserIdFromAccessToken(token.get())
		);
		chain.doFilter(request, response);
	}

	// Authorization 헤더의 Bearer 토큰 추출
	private Optional<String> extractToken(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
				.filter(header -> header.startsWith(BEARER_PREFIX))
				.map(header -> header.substring(BEARER_PREFIX.length()));
	}

	// 인증 없이 접근 가능한 API 구분
	private boolean isPublicRequest(HttpServletRequest request) {
		String method = request.getMethod();
		String path = request.getRequestURI();

		if (HttpMethod.OPTIONS.matches(method) || path.startsWith("/error")) {
			return true;
		}
		if (path.startsWith("/auth")) {
			return true;
		}
		if (HttpMethod.POST.matches(method) && "/users".equals(path)) {
			return true;
		}
		if (HttpMethod.GET.matches(method)
				&& ("/users/email".equals(path) || "/users/nickname".equals(path))) {
			return true;
		}
		return HttpMethod.GET.matches(method) && path.startsWith("/posts");
	}

	// 프로젝트 공통 형식의 인증 실패 응답
	private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setCharacterEncoding("UTF-8");
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(
				response.getWriter(),
				ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), message)
		);
	}
}
