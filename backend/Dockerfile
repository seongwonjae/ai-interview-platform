# ---- build stage ----
FROM gradle:8.7-jdk17 AS build
WORKDIR /app

# 캐시 최적화: 빌드 설정 먼저 복사
COPY backend/settings.gradle* backend/build.gradle* ./
COPY backend/gradle.properties* ./
COPY backend/gradle ./gradle

# 소스 복사
COPY backend/src ./src

# ✅ wrapper(./gradlew) 말고 gradle(8.7)로 빌드
RUN gradle clean bootJar --no-daemon

# ---- run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]