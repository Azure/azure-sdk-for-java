FROM maven:3.8.6-openjdk-11 as builder

RUN mkdir /stress-sb
WORKDIR /stress-sb

ADD ./sdk/tools /stress-sb/sdk/tools
ADD ./sdk/parents /stress-sb/sdk/parents
ADD ./sdk/core /stress-sb/sdk/core
ADD ./sdk/servicebus /stress-sb/sdk/servicebus
ADD ./eng /stress-sb/eng

RUN --mount=type=cache,target=/root/.m2 \
 mvn -f /stress-sb/eng/code-quality-reports/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-sb/sdk/tools/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-sb/sdk/core/azure-core/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-sb/sdk/core/azure-core-test/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-sb/sdk/core/azure-core-amqp/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-sb/sdk/core/azure-core-http-netty/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-sb/sdk/servicebus/azure-messaging-servicebus/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-sb/sdk/servicebus/azure-messaging-servicebus-stress/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests

RUN mkdir /stress-sb/stress-app
WORKDIR /stress-sb/stress-app
RUN java -Djarmode=layertools -jar /stress-sb/sdk/servicebus/azure-messaging-servicebus-stress/target/azure-messaging-servicebus-stress-1.0.0-beta.1.jar extract

FROM mcr.microsoft.com/openjdk/jdk:11-mariner
WORKDIR /sb-stress-app
COPY --from=builder /stress-sb/stress-app/dependencies/ /sb-stress-app
RUN true # https://stackoverflow.com/questions/51115856/docker-failed-to-export-image-failed-to-create-image-failed-to-get-layer
COPY --from=builder /stress-sb/stress-app/spring-boot-loader/ /sb-stress-app
RUN true
COPY --from=builder /stress-sb/stress-app/snapshot-dependencies/ /sb-stress-app
RUN true
COPY --from=builder /stress-sb/stress-app/application/ /sb-stress-app
RUN true
ARG AGENT_URL=https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.4.13/applicationinsights-agent-3.4.13.jar
ADD ${AGENT_URL} ./BOOT-INF/classes/
ENTRYPOINT ["java","org.springframework.boot.loader.JarLauncher", \
            "-javaagent:BOOT-INF/classes/applicationinsights-agent-3.4.13.jar", \
            "--TEST_CLASS=MessageSender"]
