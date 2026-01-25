# --- Stage 1: Build ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline

# 2. Build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: Runtime ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 3. Copy the finished JAR from the builder
COPY --from=build /app/target/*.jar app.jar

# 4. Open the Auth Service port
EXPOSE 8080

# 5. Launch the service
ENTRYPOINT ["java", "-jar", "app.jar"]