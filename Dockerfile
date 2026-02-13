# Multi-stage Dockerfile for Selenium MCP Server

# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build the application
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

# Install required packages for browsers
RUN apk add --no-cache \
    chromium \
    chromium-chromedriver \
    firefox \
    nss \
    freetype \
    harfbuzz \
    ttf-freefont \
    dbus \
    fontconfig \
    && rm -rf /var/cache/apk/*

# Set Chrome/Chromium environment variables
ENV CHROME_BIN=/usr/bin/chromium-browser
ENV CHROMEDRIVER_PATH=/usr/bin/chromedriver
ENV SELENIUM_REMOTE_URL=""

# Create non-root user
RUN addgroup -g 1000 selenium && \
    adduser -u 1000 -G selenium -s /bin/sh -D selenium

WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/selenium-mcp.jar app.jar

# Create directories for logs
RUN mkdir -p /app/logs && chown -R selenium:selenium /app

# Switch to non-root user
USER selenium

# JVM options for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom"

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health/liveness || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=k8s"]

