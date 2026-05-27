ARG REGISTRY="azsdkengsys.azurecr.io"
FROM ${REGISTRY}/java/jdk-mariner-mvn:jdk11-latest AS builder

RUN yum update -y

RUN mkdir /stress
WORKDIR /stress

ADD ./.vscode /stress/.vscode
ADD ./sdk/tools /stress/sdk/tools
ADD ./sdk/parents /stress/sdk/parents
ADD ./sdk/appconfiguration-v2 /stress/sdk/appconfiguration-v2
ADD ./sdk/core /stress/sdk/core
ADD ./sdk/core-v2 /stress/sdk/core-v2
ADD ./sdk/identity-v2 /stress/sdk/identity-v2
ADD ./sdk/monitor /stress/sdk/monitor
ADD ./sdk/eventhubs /stress/sdk/eventhubs
ADD ./eng /stress/eng

ARG SKIP_CHECKS="-Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -Dmaven.test.skip -Dcodesnippet.skip -Dspotless.skip"

RUN --mount=type=cache,target=/root/.m2 \
 mvn -f /stress/sdk/core-v2/azure-core/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/core-v2/azure-core-test/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/identity-v2/azure-identity/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/appconfiguration-v2/azure-data-appconfiguration/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/tools/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/tools/azure-openrewrite/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/core/azure-core/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/core/azure-core-http-netty/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/core/azure-core-http-okhttp/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/core/azure-core-http-vertx/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/core/azure-core-http-jdk-httpclient/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/core/azure-core-test/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/core/azure-core-amqp/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/core/azure-core-http-netty/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/monitor/azure-monitor-opentelemetry-exporter/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/eventhubs/azure-messaging-eventhubs/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/eventhubs/azure-messaging-eventhubs-stress/pom.xml clean install ${SKIP_CHECKS}

FROM mcr.microsoft.com/openjdk/jdk:21-mariner

WORKDIR /app
COPY --from=builder /stress/sdk/eventhubs/azure-messaging-eventhubs-stress/target .

# This is never executed (since job yaml overrides it)
ENTRYPOINT ["bash"]
