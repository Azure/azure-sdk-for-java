FROM stresspgs7b6dif73rup6.azurecr.io/java-tools/mvn:java11-mvn3.8.8 as builder

# Add necessary files to the image
RUN mkdir /stress
WORKDIR /stress
ADD ./sdk/storage /stress/sdk/storage
ADD ./sdk/tools /stress/sdk/tools
ADD ./eng /stress/eng

# Build storage and stress tests
RUN --mount=type=cache,target=/root/.m2 \
 mvn -f /stress/eng/code-quality-reports/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress/sdk/tools/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress/sdk/storage/azure-storage-common/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress/sdk/storage/azure-storage-internal-avro/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress/sdk/storage/azure-storage-blob/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress/sdk/storage/azure-storage-stress/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress/sdk/storage/azure-storage-blob-stress/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests

FROM mcr.microsoft.com/openjdk/jdk:11-mariner

# Copy target files from builder image
WORKDIR /app
COPY --from=builder /stress/sdk/storage/azure-storage-blob-stress/target .

# Configure monitoring
#commenting these two lines temporarily
#ARG AGENT_URL=https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.4.17/applicationinsights-agent-3.4.17.jar
#ADD ${AGENT_URL} ./BOOT-INF/classes/

# This is never executed (since job yaml overrides it)
ENTRYPOINT ["bash"]
