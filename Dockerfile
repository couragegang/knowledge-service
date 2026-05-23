FROM gradle:8.10.2-jdk21 AS build
WORKDIR /app

COPY gradle.properties settings.gradle.kts build.gradle.kts gradlew gradlew.bat ./
COPY gradle ./gradle
COPY src ./src

RUN gradle --no-daemon shadowJar -x test \
    && JAR=$(ls build/libs/*-all.jar | head -n1) \
    && cp "$JAR" /app/knowledge-service.jar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/knowledge-service.jar /app/knowledge-service.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "/app/knowledge-service.jar"]
