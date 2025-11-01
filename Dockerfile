# Étape 1 : Build avec Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
# Cache les dépendances Maven
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Image runtime Java
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/adoption-Project-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8089
# Health check pour Docker
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8089/adoption/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]