FROM maven:3-jdk-11 as build

# Copy framework and install
WORKDIR /app
COPY azure-perfstress azure-perfstress/
WORKDIR /app/azure-perfstress
RUN mvn install

# Copy app and build JAR
WORKDIR /app
COPY azure-storage-blob-v8-perfstress azure-storage-blob-v8-perfstress/
WORKDIR /app/azure-storage-blob-v8-perfstress
RUN mvn package


FROM openjdk:11-jre AS runtime
WORKDIR /app
COPY --from=build /app/azure-storage-blob-v8-perfstress/target/azure-storage-blob-v8-perfstress-1.0-SNAPSHOT-jar-with-dependencies.jar ./
ENTRYPOINT ["java", "-jar", "azure-storage-blob-v8-perfstress-1.0-SNAPSHOT-jar-with-dependencies.jar"]
