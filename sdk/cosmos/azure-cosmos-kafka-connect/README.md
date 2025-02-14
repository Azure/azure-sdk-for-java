# Apache Kafka Connect source and sink connectors for Azure Cosmos DB Java SDK for Java
The Azure Cosmos DB connectors allow moving data between Azure Cosmos DB and Kafka. The Cosmos DB Sink connector writes data from a Kafka topic to a Cosmos DB container. The Cosmos DB Source connector writes changes from a Cosmos DB container to a Kafka topic.

[Source code][kafka_source_code] | [Package (Maven)][cosmos_kafka_maven] | [Product documentation][cosmos_docs] 

## Getting started
### Include the package

[//]: # ({x-version-update-start;com.azure.kafka-connect:azure-cosmos-kafka-connect;current})
```xml
<dependency>
  <groupId>com.azure.kafka.connect</groupId>
  <artifactId>azure-cosmos-kafka-connect</artifactId>
  <version>2.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

Refer to maven central for previous [releases][cosmos_kafka_maven]

### Prerequisites

- [Java Development Kit (JDK) with version 8 or above][jdk]
- An active Azure account. If you don't have one, you can sign up for a [free account][azure_subscription]. Alternatively, you can use the [Azure Cosmos DB Emulator](https://learn.microsoft.com/azure/cosmos-db/local-emulator) for development and testing. As emulator HTTPS certificate is self-signed, you need to import its certificate to java trusted cert store as [explained here](https://learn.microsoft.com/azure/cosmos-db/local-emulator-export-ssl-certificates)
- (Optional) SLF4J is a logging facade.
- (Optional) [SLF4J binding](https://www.slf4j.org/manual.html) is used to associate a specific logging framework with SLF4J.
- (Optional) [Maven][maven]

SLF4J is only needed if you plan to use logging, please also download an SLF4J binding which will link the SLF4J API with the logging implementation of your choice. See the [SLF4J user manual](https://www.slf4j.org/manual.html) for more information.

The SDK provides Reactor Core-based async APIs. You can read more about Reactor Core and [Flux/Mono types here](https://projectreactor.io/docs/core/release/api/)

## Key concepts

TBD

## Examples
TBD

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
[cosmos_kafka_maven]: https://central.sonatype.com/artifact/com.azure.kafka.connect/azure-cosmos-kafka-connect
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[azure_subscription]: https://azure.microsoft.com/free/
[troubleshooting]: https://learn.microsoft.com/azure/cosmos-db/troubleshoot-java-sdk-v4-sql
[perf_guide]: https://learn.microsoft.com/azure/cosmos-db/performance-tips-java-sdk-v4-sql?tabs=api-async
[sql_api_query]: https://learn.microsoft.com/azure/cosmos-db/sql-api-sql-query
[quickstart]: https://learn.microsoft.com/azure/cosmos-db/create-sql-api-java?tabs=sync


