# syntax=docker/dockerfile:1.7

# ===== BUILD STAGE =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src

# Cache dependencies
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -DskipTests package

# ===== RUNTIME STAGE =====
FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app

# Copy đúng jar (tránh wildcard lỗi)
COPY --from=build /src/target/*jar /app/app.jar

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]