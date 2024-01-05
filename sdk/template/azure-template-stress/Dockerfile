ARG REGISTRY="azsdkengsys.azurecr.io"
FROM ${REGISTRY}/java/jdk-mariner-mvn:jdk11-latest as builder

RUN yum -y update

# Add necessary files to the image
RUN mkdir /stress
WORKDIR /stress
ADD ./sdk/tools /stress/sdk/tools
ADD ./eng /stress/eng
ADD ./common /stress/common
ADD ./sdk/parents /stress/sdk/parents
ADD ./sdk/generic-sdk-core /stress/sdk/generic-sdk-core
ADD ./sdk/template /stress/sdk/template

# Build dependencies and stress tests
RUN --mount=type=cache,target=/root/.m2 \
mvn -f /stress/eng/code-quality-reports/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests -Dcodesnippet.skip && \
mvn -f /stress/common/perf-test-core/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests -Dcodesnippet.skip && \
mvn -f /stress/sdk/parents/azure-perf-test-parent/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests -Dcodesnippet.skip && \
mvn -f /stress/sdk/tools/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests -Dcodesnippet.skip && \
mvn -f /stress/sdk/generic-sdk-core/generic-json/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests -Dcodesnippet.skip && \
mvn -f /stress/sdk/generic-sdk-core/generic-core/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests -Dcodesnippet.skip && \
mvn -f /stress/sdk/generic-sdk-core/generic-core-http-okhttp/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests -Dcodesnippet.skip && \
mvn -f /stress/sdk/template/azure-template-stress/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests -Dcodesnippet.skip

FROM mcr.microsoft.com/openjdk/jdk:11-mariner

RUN yum -y update

# Copy target files from builder image
WORKDIR /app
COPY --from=builder /stress/sdk/template/azure-template-stress/target .

# Configure monitoring
ARG APPLICATION_INSIGHTS_AGENT_VERSION=3.4.19
ARG AGENT_URL=https://github.com/microsoft/ApplicationInsights-Java/releases/download/${APPLICATION_INSIGHTS_AGENT_VERSION}/applicationinsights-agent-${APPLICATION_INSIGHTS_AGENT_VERSION}.jar
ADD ${AGENT_URL} ./applicationinsights-agent.jar

COPY --from=builder /stress/sdk/template/azure-template-stress/src/main/resources/simplehttpserver.crt ./simplehttpserver.crt
RUN keytool -import -alias simple -file ./simplehttpserver.crt -keystore /usr/lib/jvm/msopenjdk-11/lib/security/cacerts  -noprompt -keypass changeit -storepass changeit

#RUN update-ca-trust extract

# This is never executed (since job yaml overrides it)
ENTRYPOINT ["bash"]
