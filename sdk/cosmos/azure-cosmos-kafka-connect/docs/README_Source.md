# Kafka Connect Cosmos DB Source Connector

The Azure Cosmos DB Source connector provides the capability to read data from the Cosmos DB Change Feed and publish this data to a Kafka topic.

## Topics covered

- [Quickstart](#quickstart)
- [Source configuration properties](#source-configuration-properties)
- [Migrating from 2.x](migration-2-to-3.md)

## Quickstart

### Prerequisites

- Start with the three-service [Confluent Platform 8.2.5 demo](Confluent_Platform_Setup.md), or install a compatible Kafka Connect 4.2.2 worker and configure its Kafka broker access and connector plugins yourself.
- Cosmos DB instance ([setup guide](CosmosDB_Setup.md)).
- Bash shell
  - Will not work in Cloud Shell or WSL1
- Java 17+; Java 21 is recommended ([download](https://learn.microsoft.com/java/azure/jdk/)).
- Maven ([download](https://maven.apache.org/download.cgi))

### Install source connector

If you are using the Confluent Platform setup from this repo, the Cosmos DB Source Connector is included in the installation and you can skip this step.

Otherwise, you can use JAR file from latest [Release](https://mvnrepository.com/artifact/com.azure.cosmos.kafka/azure-cosmos-kafka-connect) and install the connector manually, refer to these [instructions](https://docs.confluent.io/current/connect/managing/install.html#install-connector-manually).

You can also package a new JAR file from the source code.

```bash
# clone the repository if you haven't done so already
git clone https://github.com/Azure/azure-sdk-for-java.git
cd azure-sdk-for-java
mvn -f sdk/cosmos/azure-cosmos-kafka-connect/pom.xml -Pbuild-kafka clean package -DskipTests=true

# install only the shaded connector JAR, not sources, javadoc, or original-* JARs
ls sdk/cosmos/azure-cosmos-kafka-connect/target/azure-cosmos-kafka-connect-3*.jar

```

### Create Kafka topic

For this quickstart, create a Kafka topic named `apparels` for JSON data (without an embedded schema). Run from the demo's `src/docker` directory:

```bash
docker compose exec -T broker kafka-topics --bootstrap-server broker:29092 --create --if-not-exists --replication-factor 1 --partitions 1 --topic apparels
```

For another cluster, use its Kafka CLI with `--bootstrap-server` and the appropriate client authentication configuration.

### Create the source connector

#### Create the Cosmos DB Source Connector in Kafka Connect

The following JSON body defines the config for the Cosmos DB Source Connector.

>Note: You will need to replace placeholder values for below properties which you should have saved from the [Cosmos DB setup guide](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-kafka-connect/docs/CosmosDB_Setup.md).  
>`azure.cosmos.account.endpoint`  
>`azure.cosmos.account.key`  

Refer to the [source properties](#source-configuration-properties) section for more information on each of these configuration properties.

```json
{
  "name": "cosmosdb-source-connector-v2",
  "config": {
    "connector.class": "com.azure.cosmos.kafka.connect.CosmosSourceConnector",
    "tasks.max": "5",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "false",
    "key.converter": "org.apache.kafka.connect.json.JsonConverter",
    "key.converter.schemas.enable": "false",
    "azure.cosmos.account.endpoint":"{endpoint}",
    "azure.cosmos.account.key":"{masterKey}",
    "azure.cosmos.application.name": "{applicationName}",
    "azure.cosmos.source.database.name":"{database}",
    "azure.cosmos.source.containers.includedList":"{container}",
    "azure.cosmos.source.changeFeed.maxItemCountHint":"500",
    "azure.cosmos.source.containers.topicMap":"{topic}#{container}",
    "azure.cosmos.source.metadata.storage.type":"Cosmos",
    "azure.cosmos.source.metadata.storage.name":"{metadataContainerName}"
  }
}

```

Once you have all the values filled out, save the JSON file somewhere locally. You can use this file to create the connector using the REST API.

#### Create connector using REST API

Create the source connector using the Connect REST API

```bash

# Curl to Kafka connect service
curl --fail --silent --show-error -H "Content-Type: application/json" -X POST --data-binary @source.json http://localhost:8083/connectors
curl --fail --silent --show-error http://localhost:8083/connectors/cosmosdb-source-connector-v2/status

```

Save the completed JSON body above as `source.json` (protect the account key). Confirm both the connector and its tasks are RUNNING. The `-v2` name is retained for continuity and does not select connector 2.x; for an existing deployment follow the [migration guide](migration-2-to-3.md) rather than creating another connector.

### Insert document in to Cosmos DB

Use [Cosmos DB setup guide](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-kafka-connect/docs/CosmosDB_Setup.md) to create and set up Cosmos DB Instance.

- Sign into the [Azure portal](https://portal.azure.com/learn.docs.microsoft.com) using the account you activated.
- On the Azure portal menu (left hand side blue lines at the top), select All services.
- Select Databases > Azure Cosmos DB. Then select the DB you just created, click Data Explorer at the top.
- To create a new JSON document, in the SQL API pane, expand `kafka`, select Items, then select New Item in the toolbar.
- Now, add a document to the container with the following structure. Paste the following sample JSON block into the Items tab, overwriting the current content:

  ``` json

  {
    "id": "2",
    "productId": "33218897",
    "category": "Women's Outerwear",
    "manufacturer": "Contoso",
    "description": "Black wool pea-coat",
    "price": "49.99",
    "shipping": {
      "weight": 2,
      "dimensions": {
        "width": 8,
        "height": 11,
        "depth": 3
      }
    }
  }

  ```

- Select Save.
- Confirm the document has been saved by clicking Items on the left-hand menu.

### Confirm data written to Kafka Topic

From the demo's `src/docker` directory, consume the topic and verify that the document inserted into Cosmos DB appears:

```bash
docker compose exec -T broker kafka-console-consumer --bootstrap-server broker:29092 --topic apparels --from-beginning --timeout-ms 30000
```

The consumer exits on timeout if no more records arrive. `--from-beginning` here is only a read-only demonstration consumer, not a connector offset reset.

### Cleanup

Delete only the connector created for this quickstart using the Connect REST API. This is demo cleanup, not an upgrade step.

```bash

# Curl to Kafka connect service
curl --fail --silent --show-error -X DELETE http://localhost:8083/connectors/cosmosdb-source-connector-v2

```

To delete the created Azure Cosmos DB service and its resource group using Azure CLI, refer to these [steps](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-kafka-connect/docs/CosmosDB_Setup.md#cleanup).

## Source configuration properties

The following settings are used to configure the Cosmos DB Kafka Source Connector. These configuration values determine which Cosmos DB container is consumed, which Kafka topics data is written into and formats to serialize the data. For an example configuration file with the default values, refer to [this config](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-kafka-connect/src/docker/resources/source.example.json).

- [Generic Configs For Sink And Source](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-kafka-connect/docs/configuration-reference.md#generic-configurations)
- [Configs only for Source](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-kafka-connect/docs/configuration-reference.md#source-connector-configurations)
