ARG REGISTRY="azsdkengsys.azurecr.io"
FROM ${REGISTRY}/java/jdk-mariner-mvn:jdk11-latest as builder

RUN yum update -y
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
 mvn -f /stress-sb/sdk/core/azure-core-metrics-opentelemetry/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-sb/sdk/servicebus/azure-messaging-servicebus/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-sb/sdk/servicebus/azure-messaging-servicebus-stress/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests

FROM mcr.microsoft.com/openjdk/jdk:21-mariner

RUN yum update -y

WORKDIR /app
COPY --from=builder /stress-sb/sdk/servicebus/azure-messaging-servicebus-stress/target .

# This is never executed (since job yaml overrides it)
ENTRYPOINT ["bash"]
