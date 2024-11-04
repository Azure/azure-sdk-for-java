# Dev azure cosmos kafka connect client library for Java

### Prerequisites

Ensure you have the following prerequisites installed.
- Bash shell
    - Will not work in Cloud Shell or WSL1
- Docker ([download](https://www.docker.com/products/docker-desktop))
- Git
- Java 11+ ([download](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html))
- Maven ([download](https://maven.apache.org/download.cgi))

### How to run validation

#### To run unit tests:
```
mvn -e -Dgpg.skip -Dmaven.javadoc.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true -pl ,azure-cosmos-kafka-connect test package -Punit
```

#### To run integration tests:
You can run the integration tests against a local kafka cluster by using [Test Containers](https://testcontainers.com), or you can run the integration tests against confluent cloud cluster.

To run the integration tests against a local kafka cluster, create ~/kafka-cosmos-local.properties with the following content:
```
ACCOUNT_HOST=[emulator endpoint or you cosmos masterKey]
ACCOUNT_KEY=[emulator masterKey or your cosmos masterKey]
ACCOUNT_TENANT_ID=[update if AAD auth is required in the integration tests]
ACCOUNT_AAD_CLIENT_ID=[update if AAD auth is required in the integration tests]
ACCOUNT_AAD_CLIENT_SECRET=[update is AAD auth is required in the integration tests]
SASL_JAAS=
BOOTSTRAP_SERVER=
CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR=1
CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR=1
CONNECT_STATUS_STORAGE_REPLICATION_FACTOR=1
```

To run the integration tests against a confluent cloud cluster, create ~/kafka-cosmos-local.properties with the following content:
```
ACCOUNT_HOST=[emulator endpoint or you cosmos masterKey]
ACCOUNT_KEY=[emulator masterKey or your cosmos masterKey]
ACCOUNT_TENANT_ID=[update if AAD auth is required in the integration tests]
ACCOUNT_AAD_CLIENT_ID=[update if AAD auth is required in the integration tests]
ACCOUNT_AAD_CLIENT_SECRET=[update is AAD auth is required in the integration tests]
SASL_JAAS=[credential configured on the confluent cloud cluster]
BOOTSTRAP_SERVER=[bootstrap server endpoint of the confluent cloud cluster]
SCHEMA_REGISTRY_URL=[schema registry url of the cloud cluster]
SCHEMA_REGISTRY_KEY=[schema registry key of the cloud cluster]
SCHEMA_REGISTRY_SECRET=[schema registry secret of the cloud cluster]
CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR=3
CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR=3
CONNECT_STATUS_STORAGE_REPLICATION_FACTOR=3
```
Please follow [Confluent_Cloud_Setup](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-kafka-connect/docs/Confluent_Cloud_Setup.md) to setup a confluent cloud cluster.
Integration tests are having ITest suffix. Use following command to run integration tests:
```bash
mvn -e -Dgpg.skip -Dmaven.javadoc.skip=true -Dcodesnippet.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true -pl ,azure-cosmos-kafka-connect test package -Pkafka-integration
```

### Configure Confluent Platform, Cosmos DB and validate Kafka Connectors

- [Confluent Platform Setup](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-kafka-connect/docs/Confluent_Platform_Setup.md)
- [Confluent Cloud Setup](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-kafka-connect/docs/Confluent_Cloud_Setup.md)
- [Setting up an Azure Cosmos DB Instance](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-kafka-connect/docs/CosmosDB_Setup.md)
- [Kafka Connect Cosmos DB Sink Connector](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-kafka-connect/docs/README_Sink.md)
- [Kafka Connect Cosmos DB Source Connector](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-kafka-connect/docs/README_Source.md)
