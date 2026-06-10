FROM eclipse-temurin:25-jdk

WORKDIR /app

# Download the Application Insights Java agent
ADD https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.7.8/applicationinsights-agent-3.7.8.jar applicationinsights-agent.jar

COPY target/azure-agentserver-langchain4j-financial-jersey-sample-*-jar-with-dependencies.jar app.jar
COPY applicationinsights.json applicationinsights.json
COPY scripts/run.sh run.sh

EXPOSE 8088

ENTRYPOINT ["/app/run.sh"]

