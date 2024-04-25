# ------------------------------------
# Copyright (c) Microsoft Corporation.
# Licensed under the MIT License.
# ------------------------------------
# docker can't tell when the repo has changed and will therefore cache this layer

# internal users should provide MCR registry to build via 'docker build . --build-arg REGISTRY="mcr.microsoft.com/mirror/docker/library/"'
# public OSS users should simply leave this argument blank or ignore its presence entirely
ARG REGISTRY=""

FROM ${REGISTRY}alpine:3.19 as repo
RUN apk --no-cache add git
RUN git clone https://github.com/Azure/azure-sdk-for-java --single-branch --depth 1 /azure-sdk-for-java


FROM openjdk:17-oracle
COPY target/identity-test-container-1.0-SNAPSHOT-jar-with-dependencies.jar /identity-test.jar

CMD [ "java", "-version" ]
