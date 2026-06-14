FROM eclipse-temurin:21-jdk AS builder
WORKDIR /build

COPY gradlew gradlew.bat* ./
COPY gradle/ gradle/
COPY settings.gradle.kts ./

# Recréer la structure du sous-projet backend
RUN mkdir -p backend/src
COPY backend/build.gradle.kts backend/
COPY backend/src/ backend/src/

RUN chmod +x gradlew && ./gradlew :backend:shadowJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /build/backend/build/libs/app.jar app.jar
RUN mkdir -p /app/uploads/student-cards
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]