FROM eclipse-temurin:25-jdk

WORKDIR /app

# Download the Application Insights Java agent
ADD https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.6.2/applicationinsights-agent-3.6.2.jar applicationinsights-agent.jar

COPY target/azure-agentserver-langchain4j-sample-*-jar-with-dependencies.jar app.jar
COPY applicationinsights.json applicationinsights.json
COPY run.sh /app/run.sh
RUN chmod +x /app/run.sh

EXPOSE 8088

ENTRYPOINT ["/app/run.sh"]

