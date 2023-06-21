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
ADD . .

RUN --mount=type=cache,target=/root/.m2
RUN apt-get install -y python3
RUN python3 eng/scripts/generate_build_pom.py --pl com.azure:azure-storage-blob

RUN mvn clean install -f eng/code-quality-reports/
RUN mvn clean install -f ClientBuildPom.xml -DskipTests -Dgpg.skip -Dmaven.javadoc.skip=true -Dcodesnippet.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true -DtrimStackTrace=false -Djacoco.skip=true -T 2C

# RUN --mount=type=cache,target=/root/.cache/go-build \

#  cd ../storage && mvn clean install -D skipTests
# CMD ["java", "-jar", "./target/Icm387357955-1.0-SNAPSHOT-jar-with-dependencies.jar"]
#0 279.7 [ERROR] Failed to execute goal on project azure-core-perf: Could not resolve dependencies for project com.azure:azure-core-perf:jar:1.0.0-beta.1: The following artifacts could not be resolved: com.azure:perf-test-core:jar:1.0.0-beta.1 (absent): Could not find artifact com.azure:perf-test-core:jar:1.0.0-beta.1 in ossrh (https://oss.sonatype.org/content/repositories/snapshots/) -> [Help 1]
