# syntax=docker/dockerfile:1.7

FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /src

# Cache dependencies first for faster rebuilds.
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package

# Smaller, secure runtime image (non-root by default).
FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app
COPY --from=build /src/target/*.jar /app/app.jar

EXPOSE 8080
# Use JAVA_TOOL_OPTIONS so JVM options are still configurable without shell.
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
