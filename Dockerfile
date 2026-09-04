# Multi-stage build for Keycloak Integration Service
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY spotbugs-exclude.xml .
COPY src src

# Make maven wrapper executable and build jar
RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /workspace/app/target/keycloak-integration-*.jar app.jar

ENV SERVER_PORT=8080 \
    KEYCLOAK_URL=http://keycloak:8080 \
    KEYCLOAK_MASTER_REALM=master \
    KEYCLOAK_CLIENT_ID=admin-cli

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]