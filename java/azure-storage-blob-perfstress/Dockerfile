FROM maven:3-jdk-11 as build

# Copy framework and install
WORKDIR /app
COPY azure-perfstress azure-perfstress/
WORKDIR /app/azure-perfstress
RUN mvn install

# Copy app and build JAR
WORKDIR /app
COPY azure-storage-blob-perfstress azure-storage-blob-perfstress/
WORKDIR /app/azure-storage-blob-perfstress
RUN mvn package


FROM openjdk:11-jre AS runtime
WORKDIR /app
COPY --from=build /app/azure-storage-blob-perfstress/target/azure-storage-blob-perfstress-1.0-SNAPSHOT-jar-with-dependencies.jar ./
ENTRYPOINT ["java", "-jar", "azure-storage-blob-perfstress-1.0-SNAPSHOT-jar-with-dependencies.jar"]
