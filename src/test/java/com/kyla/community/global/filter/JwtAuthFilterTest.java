package com.kyla.community.global.filter;

import com.kyla.community.global.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class JwtAuthFilterTest {
	@Test
	void actuatorHealthPassesThroughWithoutAuthorization() throws Exception {
		JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
		JwtAuthFilter filter = new JwtAuthFilter(jwtTokenProvider);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/actuator/health");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		request.setContextPath("/v1");

		filter.doFilter(request, response, chain);

		assertThat(chain.getRequest()).isSameAs(request);
		assertThat(response.getStatus()).isEqualTo(200);
		verifyNoInteractions(jwtTokenProvider);
	}

	@Test
	void publicEmailCheckPassesThroughEvenWhenInvalidAuthorizationHeaderExists() throws Exception {
		JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
		JwtAuthFilter filter = new JwtAuthFilter(jwtTokenProvider);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/email");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		request.setParameter("email", "fresh@example.com");
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");

		filter.doFilter(request, response, chain);

		assertThat(chain.getRequest()).isSameAs(request);
		assertThat(response.getStatus()).isEqualTo(200);
		verifyNoInteractions(jwtTokenProvider);
	}

	@Test
	void publicNicknameCheckPassesThroughEvenWhenInvalidAuthorizationHeaderExists() throws Exception {
		JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
		JwtAuthFilter filter = new JwtAuthFilter(jwtTokenProvider);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/nickname");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		request.setParameter("nickname", "freshNickname");
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");

		filter.doFilter(request, response, chain);

		assertThat(chain.getRequest()).isSameAs(request);
		assertThat(response.getStatus()).isEqualTo(200);
		verifyNoInteractions(jwtTokenProvider);
	}
}
