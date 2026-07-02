# Single-stage build for cosmos-avad-test soak runner
# Build context: azure-cosmos-benchmark/ (module root)
# Requires: mvn package run locally first (produces fat jar)

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Install curl for health probes
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

COPY target/azure-cosmos-benchmark-*-jar-with-dependencies.jar /app/app.jar

# Health endpoint port
EXPOSE 8080

# JVM tuning for container environments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp /app/app.jar com.azure.cosmos.avadtest.Main $0 $@"]
