ARG REGISTRY="azsdkengsys.azurecr.io"
FROM ${REGISTRY}/java/jdk-mariner-mvn:jdk11-latest as builder

RUN yum -y update

# Add necessary files to the image
RUN mkdir /stress
WORKDIR /stress
ADD ./sdk/storage /stress/sdk/storage
ADD ./sdk/tools /stress/sdk/tools
ADD ./eng /stress/eng
ADD ./common /stress/common
ADD ./sdk/parents /stress/sdk/parents

# Build storage and stress tests
ARG SKIP_CHECKS="-Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests  -Dcodesnippet.skip -Dspotless.skip -Denforcer.skip"
RUN --mount=type=cache,target=/root/.m2 \
 mvn -f /stress/eng/code-quality-reports/pom.xml clean install  && \
 mvn -f /stress/sdk/parents/azure-perf-test-parent/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/common/perf-test-core/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/parents/azure-perf-test-parent/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/tools/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/storage/azure-storage-common/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/storage/azure-storage-internal-avro/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/storage/azure-storage-blob/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/storage/azure-storage-file-datalake/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/storage/azure-storage-stress/pom.xml clean install ${SKIP_CHECKS} && \
 mvn -f /stress/sdk/storage/azure-storage-file-datalake-stress/pom.xml clean install ${SKIP_CHECKS}

FROM mcr.microsoft.com/openjdk/jdk:21-mariner

# Required for `ps` command to find and stop the fault injector process
RUN yum install -y procps

# Copy target files from builder image
WORKDIR /app
COPY --from=builder /stress/sdk/storage/azure-storage-file-datalake-stress/target .

# This is never executed (since job yaml overrides it)
ENTRYPOINT ["bash"]
