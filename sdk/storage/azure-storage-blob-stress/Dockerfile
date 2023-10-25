FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu

# Set up base dependencies
RUN mkdir /stress-storage
WORKDIR /stress-storage
ADD ./sdk/storage /stress-storage/sdk/storage
RUN apt update -y
ADD ./sdk/tools /stress-storage/sdk/tools
ADD ./eng /stress-storage/eng
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
RUN wget https://dlcdn.apache.org/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.tar.gz
RUN mkdir -p /opt
RUN tar xzvf apache-maven-3.9.5-bin.tar.gz -C /opt/
ENV MAVEN_HOME /opt/apache-maven-3.9.5
ENV PATH="${PATH}:/opt/apache-maven-3.9.5/bin"

# Install and build test
RUN --mount=type=cache,target=/root/.m2 \
 mvn -f /stress-storage/eng/code-quality-reports/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-storage/sdk/tools/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-storage/sdk/storage/azure-storage-common/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-storage/sdk/storage/azure-storage-internal-avro/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-storage/sdk/storage/azure-storage-blob/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-storage/sdk/storage/azure-storage-stress/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests && \
 mvn -f /stress-storage/sdk/storage/azure-storage-blob-stress/pom.xml clean install -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -DskipTests

CMD ["java", "-jar", "/stress-storage/sdk/storage/azure-storage-blob-stress/target/azure-storage-blob-stress-1.0.0-beta.1-jar-with-dependencies.jar"]
