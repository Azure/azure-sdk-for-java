FROM maven:3.8.6-openjdk-11 as builder

RUN mkdir /stress-eh
WORKDIR /stress-eh

ADD ./sdk/tools /stress-eh/sdk/tools
ADD ./sdk/parents /stress-eh/sdk/parents
ADD ./sdk/core /stress-eh/sdk/core
ADD ./sdk/eventhubs /stress-eh/sdk/eventhubs
ADD ./eng /stress-eh/eng

RUN --mount=type=cache,target=/root/.m2 \
 mvn -f /stress-eh/eng/code-quality-reports/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-eh/sdk/tools/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-eh/sdk/core/azure-core/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-eh/sdk/core/azure-core-test/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-eh/sdk/core/azure-core-amqp/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-eh/sdk/core/azure-core-http-netty/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-eh/sdk/eventhubs/azure-messaging-eventhubs/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-eh/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-eh/sdk/eventhubs/azure-messaging-eventhubs-stress/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests

FROM mcr.microsoft.com/openjdk/jdk:11-mariner

WORKDIR /app
COPY --from=builder /stress-eh/sdk/eventhubs/azure-messaging-eventhubs-stress/target .

ARG AGENT_URL=https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.4.13/applicationinsights-agent-3.4.13.jar
ADD ${AGENT_URL} ./BOOT-INF/classes/

ENTRYPOINT ["java", "-javaagent:BOOT-INF/classes/applicationinsights-agent-3.4.13.jar", \
            "-jar", "/app/azure-messaging-eventhubs-stress/target/azure-messaging-eventhubs-stress-1.0.0-beta.1.jar", \
            "--TEST_CLASS=EventSender"]

