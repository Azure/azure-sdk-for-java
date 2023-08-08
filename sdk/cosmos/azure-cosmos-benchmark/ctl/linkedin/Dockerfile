# This script should be executed from azure-sdk-for-java/sdk/cosmos/azure-cosmos-benchmark
#   docker build -t linkedin-ctl-workload -f ctl/linkedin/Dockerfile .
#

FROM mcr.microsoft.com/mirror/docker/library/centos:7
#
# UTF-8 by default
#
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

#
# Pull Zulu OpenJDK binaries from official repository:
#

ARG ZULU_REPO_VER=1.0.0-1

RUN rpm --import http://repos.azulsystems.com/RPM-GPG-KEY-azulsystems && \
    curl -sLO https://cdn.azul.com/zulu/bin/zulu-repo-${ZULU_REPO_VER}.noarch.rpm && \
    rpm -ivh zulu-repo-${ZULU_REPO_VER}.noarch.rpm && \
    yum -q -y update && \
    yum -q -y upgrade && \
    yum -q -y install yum-plugin-versionlock tzdata && \
    yum versionlock zulu11-*-11.0.18-* && \
    yum -q -y install zulu11-jdk && \
    yum clean all && \
    rm -rf /var/cache/yum zulu-repo_${ZULU_REPO_VER}.noarch.rpm

ENV JAVA_HOME=/usr/lib/jvm/zulu11

COPY ./azure-cosmos-benchmark*-jar-with-dependencies.jar /usr/app/azure-cosmos-benchmark-jar-with-dependencies.jar
COPY ./run_benchmark.sh /usr/app/
WORKDIR /usr/app
RUN chmod +x run_benchmark.sh
CMD /bin/sh ./run_benchmark.sh
