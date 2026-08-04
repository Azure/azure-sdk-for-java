FROM eclipse-temurin:25-jre

WORKDIR /app

# Download the Application Insights Java agent
ADD https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.6.2/applicationinsights-agent-3.6.2.jar applicationinsights-agent.jar

COPY target/azure-agentserver-echo-sample-*-jar-with-dependencies.jar app.jar
COPY applicationinsights.json applicationinsights.json

EXPOSE 8088

ENTRYPOINT ["sh", "-c", "exec java ${APPLICATIONINSIGHTS_CONNECTION_STRING:+-javaagent:/app/applicationinsights-agent.jar} -jar app.jar"]

