#!/bin/bash

echo "Deleting prior Cosmos DB connectors..."
rm -rf src/test/connectorPlugins/connectors
mkdir src/test/connectorPlugins/connectors

echo "Rebuilding Cosmos DB connectors..."
mvn clean package -DskipTests=true -Dmaven.javadoc.skip=true
find target/ . -name 'azure-cosmos-kafka-connect-*.jar' ! -name 'azure-cosmos-kafka-connect-*-sources.jar' -exec cp {} src/test/connectorPlugins/connectors \;
cd src/test/connectorPlugins

echo "Adding custom Insert UUID SMT"
cd connectors
git clone https://github.com/confluentinc/kafka-connect-insert-uuid.git insertuuid -q && cd insertuuid
mvn clean package -DskipTests=true
cp target/*.jar ../
cd .. && rm -rf insertuuid
cd ../