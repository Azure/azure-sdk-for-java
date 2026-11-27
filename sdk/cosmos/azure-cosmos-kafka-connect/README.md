# Apache Kafka Connect source and sink connectors for Azure Cosmos DB Java SDK for Java
The Azure Cosmos DB connectors allow moving data between Azure Cosmos DB and Kafka. The Cosmos DB Sink connector writes data from a Kafka topic to a Cosmos DB container. The Cosmos DB Source connector writes changes from a Cosmos DB container to a Kafka topic.

[Source code][kafka_source_code] | [Package (Maven)][cosmos_kafka_maven] | [Product documentation][cosmos_docs] 

## Getting started
### Include the package

[//]: # ({x-version-update-start;com.azure.cosmos.kafka:azure-cosmos-kafka-connect;current})
```xml
<dependency>
  <groupId>com.azure.cosmos.kafka</groupId>
  <artifactId>azure-cosmos-kafka-connect</artifactId>
  <version>3.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

Refer to maven central for previous [releases][cosmos_kafka_maven]

### Prerequisites

- [Java Development Kit (JDK) 17 or newer][jdk]; Java 21 is recommended. Connector builds and tests cover JDK 17 and 21.
- Kafka Connect 4.2.2 is the validated worker target for connector 3.x. The local demo and automated worker containers use Confluent Platform 8.2.5 (Kafka 4.2.2).
- An active Azure account. If you don't have one, you can sign up for a [free account][azure_subscription]. Alternatively, you can use the [Azure Cosmos DB Emulator](https://learn.microsoft.com/azure/cosmos-db/local-emulator) for development and testing. As emulator HTTPS certificate is self-signed, you need to import its certificate to java trusted cert store as [explained here](https://learn.microsoft.com/azure/cosmos-db/local-emulator-export-ssl-certificates)
- (Optional) [Maven][maven]

For a Kafka Connect deployment, use the worker's logging implementation; do not install another SLF4J binding in the connector plugin directory. Confluent Platform 8.2 uses Log4j2. See the [SLF4J user manual](https://www.slf4j.org/manual.html) for standalone Java logging information.

Install the shaded connector JAR in an isolated directory on the worker's `plugin.path`; the Maven dependency alone does not install or upgrade a worker. The Kafka `connect-api` dependency is **provided** by the worker; `connect-runtime` and Log4j dependencies are **test** dependencies, not a bundled worker or broker. Upgrading this artifact does not patch a customer's Kafka services.

For existing deployments, read the [2.x-to-3.x migration guide](docs/migration-2-to-3.md) before changing Java, worker images, or plugins. The Maven coordinates, connector classes, configuration keys, and offset/metadata formats remain unchanged.

The SDK provides Reactor Core-based async APIs. You can read more about Reactor Core and [Flux/Mono types here](https://projectreactor.io/docs/core/release/api/)

## Key concepts

- [Source connector quickstart](docs/README_Source.md): publish Cosmos DB change feed data to Kafka.
- [Sink connector quickstart](docs/README_Sink.md): write Kafka records to Cosmos DB.
- [Connector configuration reference](docs/configuration-reference.md).

## Examples
Start the three-service [local Confluent Platform demo](docs/Confluent_Platform_Setup.md), then follow the source and sink REST API quickstarts above. The demo is development-only and is not a production broker migration procedure.

## Troubleshooting

### General

Azure Cosmos DB is a fast and flexible distributed database that scales seamlessly with guaranteed latency and throughput.
You do not have to make major architecture changes or write complex code to scale your database with Azure Cosmos DB.
Scaling up and down is as easy as making a single API call or SDK method call.
However, because Azure Cosmos DB is accessed via network calls there are client-side optimizations you can make to achieve peak performance when using Azure Cosmos DB Java SDK v4.

- [Performance][perf_guide] guide covers these client-side optimizations.

- [Troubleshooting Guide][troubleshooting] covers common issues, workarounds, diagnostic steps, and tools when you use Azure Cosmos DB Java SDK v4 with Azure Cosmos DB SQL API accounts.

## Next steps

- Quick start of Cosmos DB core java sdk [quickstart][quickstart] - Building a java app to manage Cosmos DB SQL API data
- [Read more about Azure Cosmos DB Service][cosmos_docs]
- [Contributor setup and Testcontainers validation](dev/setup.md)
- [Migrating connector 2.x to 3.x](docs/migration-2-to-3.md)

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a
[Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights
to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate
the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to
do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq]
or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[kafka_source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-kafka-connect/src
[cosmos_introduction]: https://learn.microsoft.com/azure/cosmos-db/
[cosmos_docs]: https://learn.microsoft.com/azure/cosmos-db/introduction
[jdk]: https://learn.microsoft.com/java/azure/jdk/
[maven]: https://maven.apache.org/
[cosmos_kafka_maven]: https://central.sonatype.com/artifact/com.azure.cosmos.kafka/azure-cosmos-kafka-connect
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[azure_subscription]: https://azure.microsoft.com/free/
[troubleshooting]: https://learn.microsoft.com/azure/cosmos-db/troubleshoot-java-sdk-v4-sql
[perf_guide]: https://learn.microsoft.com/azure/cosmos-db/performance-tips-java-sdk-v4-sql?tabs=api-async
[sql_api_query]: https://learn.microsoft.com/azure/cosmos-db/sql-api-sql-query
[quickstart]: https://learn.microsoft.com/azure/cosmos-db/create-sql-api-java?tabs=sync


