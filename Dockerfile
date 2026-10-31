# Multi-stage build for Spring Boot application

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-25-noble AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-noble

WORKDIR /app

# Install fonts for headless graphics rendering
RUN apt-get update && apt-get install -y --no-install-recommends \
  fontconfig \
  libfreetype6 \
  fonts-dejavu-core \
  fonts-roboto-unhinted \
  && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy the built JAR from build stage and hand it over to the non-root user
COPY --from=build --chown=spring:spring /app/target/barcodelib4j-service-*.jar app.jar

# Switch to non-root user
USER spring

# Expose port (documentation only)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", \
  "-Djava.awt.headless=true", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", \
  "app.jar"]
