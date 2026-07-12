# 1. 빌드 단계
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /workspace

# 2. 변경 빈도가 낮은 Gradle 설정 먼저 복사
COPY --chmod=0755 gradlew ./
COPY gradle ./gradle
COPY settings.gradle build.gradle ./

# 3. Gradle 의존성 다운로드
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon

# 4. 자주 변경되는 소스 코드 복사
COPY src ./src

# 5. 실행 가능한 Spring Boot JAR 생성
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon

# 6. 실행 단계
FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app

# 7. root가 아닌 실행 사용자 생성
RUN useradd --system --uid 10001 spring

# 8. 빌드 결과물만 실행 이미지로 복사
COPY --from=builder \
    /workspace/build/libs/*.jar \
    /app/app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
