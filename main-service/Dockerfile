FROM gradle:8.8-jdk17 AS build

WORKDIR /app

COPY --chown=gradle:gradle . .

RUN chmod +x gradlew

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY --from=build /app/build/libs/blps-1-0.0.1-SNAPSHOT.jar app.jar

RUN mkdir -p /app/transaction-logs && \
    chmod -R 777 /app/transaction-logs

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-Duser.timezone=UTC", \
            "-jar", "app.jar"]