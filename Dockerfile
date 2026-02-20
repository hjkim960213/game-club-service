# 1. 자바 실행 환경(JDK)을 가져옵니다. (희준 님 버전이 17이라면 17-jdk)
FROM eclipse-temurin:17-jdk-alpine

# 2. 빌드된 jar 파일을 서버 안으로 복사합니다.
# 스프링 부트 빌드 시 생성되는 jar 파일 이름을 app.jar로 복사한다는 뜻입니다.
COPY build/libs/*.jar app.jar

# 3. 서버가 켜질 때 실행할 명령어를 입력합니다.
ENTRYPOINT ["java", "-jar", "/app.jar"]