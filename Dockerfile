# Étape 1 : Build Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Image runtime
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/adoption-Project-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8089

# Healthcheck fiable (60s pour le démarrage)
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
  CMD curl -f http://localhost:8089/adoption/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
