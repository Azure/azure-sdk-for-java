#!/bin/bash
echo "Shutting down Docker Compose orchestration..."
docker-compose down

echo "Deleting prior Cosmos DB connectors..."
rm -rf connectors
mkdir connectors
cd ../../

echo "Rebuilding Cosmos DB connectors..."
mvn clean package -DskipTests=true -Dmaven.javadoc.skip=true
find target/ . -name 'azure-cosmos-kafka-connect-*.jar' ! -name 'azure-cosmos-kafka-connect-*-sources.jar' -exec cp {} src/docker/connectors \;
cd src/docker

echo "Adding custom Insert UUID SMT"
cd connectors
git clone https://github.com/confluentinc/kafka-connect-insert-uuid.git insertuuid -q && cd insertuuid
mvn clean package -DskipTests=true
cp target/*.jar ../
cd .. && rm -rf insertuuid
cd ../

echo "Building Cosmos DB Kafka Connect Docker image"
docker build . -t cosmosdb-kafka-connect:latest

echo "Starting Docker Compose..."
docker-compose up -d
