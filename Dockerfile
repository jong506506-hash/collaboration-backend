FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Copy Gradle wrapper and build files first to improve layer caching.
COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Pre-download dependencies so later source-only edits rebuild faster.
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies

# Copy the application source and build the runnable jar.
COPY src src
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app

# Render injects PORT at runtime, and Spring reads it from application.properties.
COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
