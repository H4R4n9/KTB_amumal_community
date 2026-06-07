package com.kyla.community.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // QueryDSL 조회 객체 구성
public class QuerydslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    // EntityManager 기반 QueryDSL 쿼리 팩토리 등록
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
