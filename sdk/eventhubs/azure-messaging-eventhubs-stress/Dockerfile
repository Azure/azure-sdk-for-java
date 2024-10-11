ARG REGISTRY="azsdkengsys.azurecr.io"
FROM ${REGISTRY}/java/jdk-mariner-mvn:jdk11-latest as builder

RUN yum update -y

RUN mkdir /stress-eh
WORKDIR /stress-eh

ADD ./.vscode /stress-eh/.vscode
ADD ./sdk/tools /stress-eh/sdk/tools
ADD ./sdk/parents /stress-eh/sdk/parents
ADD ./sdk/core /stress-eh/sdk/core
ADD ./sdk/eventhubs /stress-eh/sdk/eventhubs
ADD ./eng /stress-eh/eng

ARG SKIP_CHECKS="-Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests -Dcodesnippet.skip -Dspotless.skip"

RUN --mount=type=cache,target=/root/.m2 \
 mvn -f /stress-eh/eng/code-quality-reports/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress-eh/sdk/tools/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress-eh/sdk/core/azure-core/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress-eh/sdk/core/azure-core-http-netty/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress-eh/sdk/core/azure-core-http-okhttp/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress-eh/sdk/core/azure-core-http-vertx/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress-eh/sdk/core/azure-core-http-jdk-httpclient/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress-eh/sdk/core/azure-core-test/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress-eh/sdk/core/azure-core-amqp/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress-eh/sdk/core/azure-core-http-netty/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress-eh/sdk/eventhubs/azure-messaging-eventhubs/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress-eh/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress-eh/sdk/eventhubs/azure-messaging-eventhubs-stress/pom.xml clean install ${SKIP_CHECKS}

FROM mcr.microsoft.com/openjdk/jdk:21-mariner

RUN yum update -y

WORKDIR /app
COPY --from=builder /stress-eh/sdk/eventhubs/azure-messaging-eventhubs-stress/target .

# This is never executed (since job yaml overrides it)
ENTRYPOINT ["bash"]
