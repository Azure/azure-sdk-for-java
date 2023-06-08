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

# Install and build test
RUN mkdir /stress
RUN mkdir /stress-blob
ADD ./azure-storage-stress stress
ADD ./azure-storage-blob-stress stress-blob
WORKDIR /stress
RUN /opt/apache-maven-3.9.2/bin/mvn clean install
WORKDIR /stress-blob
RUN /opt/apache-maven-3.9.2/bin/mvn clean install
CMD ["java", "-jar", "./target/azure-storage-blob-stress-1.0.0-beta.1-jar-with-dependencies.jar"]
