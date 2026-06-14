FROM eclipse-temurin:21-jdk AS builder
WORKDIR /build

COPY gradlew gradlew.bat* ./
COPY gradle/ gradle/
COPY settings.gradle.kts ./
RUN mkdir -p backend/src
COPY backend/build.gradle.kts backend/
COPY backend/src/ backend/src/

RUN chmod +x gradlew && ./gradlew :backend:installDist --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /build/backend/build/install/backend/ .
EXPOSE 8081
ENTRYPOINT ["bin/backend"]