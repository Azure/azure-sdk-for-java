FROM maven:3.8.6-openjdk-11 as builder

RUN mkdir /stress-sb
WORKDIR /stress-sb

ADD ./sdk/tools /stress-eh/sdk/tools
ADD ./sdk/parents /stress-eh/sdk/parents
ADD ./sdk/core /stress-sb/sdk/core
ADD ./sdk/servicebus /stress-sb/sdk/servicebus
ADD ./eng /stress-eh/eng

RUN --mount=type=cache,target=/root/.m2 \
 mvn -f /stress-eh/eng/code-quality-reports/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-eh/sdk/tools/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-sb/sdk/core/azure-core/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-sb/sdk/core/azure-core-test/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-sb/sdk/core/azure-core-amqp/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-sb/sdk/core/azure-core-http-netty/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-sb/sdk/servicebus/azure-messaging-servicebus/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-sb/sdk/servicebus/azure-messaging-servicebus-stress/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests

FROM mcr.microsoft.com/openjdk/jdk:11-mariner

WORKDIR /app
COPY --from=builder /stress-sb/sdk/servicebus/azure-messaging-servicebus-stress/target .

ARG AGENT_URL=https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.4.13/applicationinsights-agent-3.4.13.jar
ADD ${AGENT_URL} ./BOOT-INF/classes/
ENTRYPOINT ["java", "-javaagent:BOOT-INF/classes/applicationinsights-agent-3.4.13.jar", \
            "-jar", "/app/azure-messaging-servicebus-stress/target/azure-messaging-servicebus-stress-1.0.0-beta.1.jar", \
            "--TEST_CLASS=MessageSender"]
