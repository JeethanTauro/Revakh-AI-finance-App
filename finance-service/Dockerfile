# --- STAGE 1: Build (The Kitchen) ---
# We use Maven with JDK 21 to compile the Java code
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Copy only the pom.xml first to download dependencies.
# This is a cache optimization: if your pom.xml doesn't change,
# Docker skips this long download next time you build.
COPY pom.xml .
RUN mvn dependency:go-offline

# 2. Copy the actual source code and package it into a JAR
COPY src ./src
RUN mvn clean package -DskipTests

# --- STAGE 2: Runtime (The Dining Room) ---
# We use a lightweight JRE (Java Runtime) to run the app.
# No Maven or source code exists in this final stage.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 3. Copy only the finished JAR from the build stage
# We rename it to 'app.jar' for simplicity
COPY --from=build /app/target/*.jar app.jar

# 4. Expose the port from your application.properties (8081)
EXPOSE 8081

# 5. The command to start your finance microservice
ENTRYPOINT ["java", "-jar", "app.jar"]