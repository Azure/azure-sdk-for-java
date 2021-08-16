# This script should be executed from azure-sdk-for-java/sdk/cosmos/azure-cosmos-benchmark
#   docker build -t linkedin-ctl-workload -f ctl/linkedin/Dockerfile .
#

FROM centos:7
#
# UTF-8 by default
#
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8
ENV JAVA_HOME=/usr/lib/jvm/zulu-11

#
# Pull Zulu OpenJDK binaries from official repository:
#
RUN rpm --import http://repos.azulsystems.com/RPM-GPG-KEY-azulsystems
RUN curl -o /etc/yum.repos.d/zulu.repo http://repos.azulsystems.com/rhel/zulu.repo
RUN yum -q -y update
RUN yum -q -y install zulu-11-11.35+15-1

COPY ./azure-cosmos-benchmark*-jar-with-dependencies.jar /usr/app/azure-cosmos-benchmark-jar-with-dependencies.jar
COPY ./run_benchmark.sh /usr/app/
WORKDIR /usr/app
RUN chmod +x run_benchmark.sh
CMD /bin/sh ./run_benchmark.sh
