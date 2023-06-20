FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu

# Set up base dependencies
RUN apt update -y
RUN apt-get install -y wget

# Set up dotnet and http-fault-injector tool
RUN apt-get install -y libicu66
RUN wget https://dot.net/v1/dotnet-install.sh -O dotnet-install.sh
RUN chmod +x ./dotnet-install.sh
RUN ./dotnet-install.sh --version latest # --runtime aspnetcore
ENV DOTNET_ROOT=/root/.dotnet
ENV PATH="${PATH}:${DOTNET_ROOT}:${DOTNET_ROOT}/tools"
RUN dotnet tool install azure.sdk.tools.httpfaultinjector --global --prerelease --add-source https://pkgs.dev.azure.com/azure-sdk/public/_packaging/azure-sdk-for-net/nuget/v3/index.json

# Install maven
RUN wget https://dlcdn.apache.org/maven/maven-3/3.9.2/binaries/apache-maven-3.9.2-bin.tar.gz
RUN mkdir -p /opt
RUN tar xzvf apache-maven-3.9.2-bin.tar.gz -C /opt/
ENV PATH="${PATH}:/opt/apache-maven-3.9.2/bin"

# Install and build test
RUN mkdir /stress
WORKDIR /stress
ADD ./sdk/tools ./sdk/tools
ADD ./sdk/parents ./sdk/parents
ADD ./sdk/core ./sdk/core
ADD ./sdk/storage ./sdk/storage
ADD ./eng ./eng

# RUN --mount=type=cache,target=/root/.cache/go-build \

RUN --mount=type=cache,target=/root/.m2 \
  mvn clean install -DskipTests -f eng/code-quality-reports/pom.xml && \
  mvn clean install -DskipTests -f ./sdk/tools && \
  mvn clean install -DskipTests -f ./sdk/core

#  cd ../storage && mvn clean install -D skipTests
# CMD ["java", "-jar", "./target/Icm387357955-1.0-SNAPSHOT-jar-with-dependencies.jar"]
#0 279.7 [ERROR] Failed to execute goal on project azure-core-perf: Could not resolve dependencies for project com.azure:azure-core-perf:jar:1.0.0-beta.1: The following artifacts could not be resolved: com.azure:perf-test-core:jar:1.0.0-beta.1 (absent): Could not find artifact com.azure:perf-test-core:jar:1.0.0-beta.1 in ossrh (https://oss.sonatype.org/content/repositories/snapshots/) -> [Help 1]
