# 1단계: 빌드용 이미지 (Gradle 8.x + JDK 17)
# 🚩 Gradle 버전을 8.x대로 높였습니다.
FROM gradle:8-jdk17-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# 🚩 실제 빌드 수행
RUN gradle build -x test --no-daemon

# 2단계: 실행용 이미지 (경량 JDK)
FROM eclipse-temurin:17-jdk-alpine
EXPOSE 8080
# 🚩 빌드된 jar 파일 가져오기
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

# 3단계: 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]