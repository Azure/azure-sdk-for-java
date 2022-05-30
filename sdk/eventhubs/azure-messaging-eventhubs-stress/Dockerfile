FROM mcr.microsoft.com/java/jdk:11-zulu-alpine as builder
WORKDIR application
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM mcr.microsoft.com/java/jdk:11-zulu-alpine
WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ARG AGENT_URL=https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.2.11/applicationinsights-agent-3.2.11.jar
ADD ${AGENT_URL} ./BOOT-INF/classes/
ENTRYPOINT ["java","org.springframework.boot.loader.JarLauncher", \
            "-javaagent:BOOT-INF/classes/applicationinsights-agent-3.2.11.jar", \
            "--TEST_CLASS=SendEvents"]

