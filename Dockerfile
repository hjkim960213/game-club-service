# 1단계: 빌드용 이미지 (JDK + 소스코드)
FROM gradle:7.6-jdk17-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# 🚩 여기서 실제로 .jar 파일을 만듭니다.
RUN gradle build -x test --no-daemon

# 2단계: 실행용 이미지 (경량 JDK)
FROM eclipse-temurin:17-jdk-alpine
EXPOSE 8080
# 🚩 빌드 단계에서 만든 jar 파일만 쏙 가져옵니다.
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

# 3단계: 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]