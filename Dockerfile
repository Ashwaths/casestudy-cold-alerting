FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY backend/temperature-analysis-service/pom.xml ./backend/temperature-analysis-service/pom.xml
RUN mvn -B -q -f backend/temperature-analysis-service/pom.xml dependency:go-offline

COPY backend/temperature-analysis-service/src ./backend/temperature-analysis-service/src
RUN mvn -B -pl backend/temperature-analysis-service -am verify

FROM eclipse-temurin:25-jre
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app \
    && useradd --system --gid app --create-home app

COPY --from=build --chown=app:app /workspace/backend/temperature-analysis-service/target/*.jar /app/app.jar
USER app

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl --fail --silent http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
