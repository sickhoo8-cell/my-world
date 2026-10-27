
# Java 21로 Spring Boot 빌드
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app
COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon

# Java 21 실행 환경
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

ENV SERVER_PORT=10000
EXPOSE 10000

ENTRYPOINT ["java", "-jar", "app.jar"]
