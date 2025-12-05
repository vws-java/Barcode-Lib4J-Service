# Multi-stage build for Spring Boot application

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Install Xvfb and fonts for headless graphics rendering
RUN apt-get update && apt-get install -y \
  xvfb \
  fontconfig \
  libfreetype6 \
  fonts-dejavu-core \
  fonts-liberation \
  fonts-ubuntu \
  fonts-roboto \
  fonts-opensans \
  && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy the built JAR from build stage
COPY --from=build /app/target/barcodelib4j-service-1.0.0.jar app.jar

# Change ownership to non-root user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring

# Expose port (render.com uses PORT environment variable)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application with Xvfb
ENTRYPOINT ["sh", "-c", "Xvfb :99 -screen 0 1024x768x24 & \
  export DISPLAY=:99 && \
  java -Djava.awt.headless=true -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
