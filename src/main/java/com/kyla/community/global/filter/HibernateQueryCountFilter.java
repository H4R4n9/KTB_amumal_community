package com.kyla.community.global.filter;

import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 로컬에서 API 요청 한 건이 준비한 JDBC statement 수를 확인하기 위한 필터.
 * Hibernate Statistics가 SessionFactory 전체 값을 제공하므로 동시 요청이 없는 로컬 측정에서 사용한다.
 */
@Slf4j
public class HibernateQueryCountFilter extends OncePerRequestFilter {
	private final Statistics statistics;

	public HibernateQueryCountFilter(EntityManagerFactory entityManagerFactory) {
		this.statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
		this.statistics.setStatisticsEnabled(true);
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		long statementsBefore = statistics.getPrepareStatementCount();
		long startedAt = System.nanoTime();

		try {
			filterChain.doFilter(request, response);
		} finally {
			long statementCount = statistics.getPrepareStatementCount() - statementsBefore;
			long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
			String requestTarget = request.getQueryString() == null
					? request.getRequestURI()
					: request.getRequestURI() + "?" + request.getQueryString();

			log.info(
					"[SQL-COUNT] {} {} -> status={}, statements={}, elapsed={}ms",
					request.getMethod(),
					requestTarget,
					response.getStatus(),
					statementCount,
					elapsedMillis
			);
		}
	}
}
