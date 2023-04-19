# Azure Cosmos Test client library for Java
Library containing core fault injection classes used to test Azure Cosmos DB SDK libraries.

## Getting started
### Include the package

[//]: # ({x-version-update-start;com.azure:azure-cosmos-test;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-cosmos-test</artifactId>
  <version>1.0.0-beta.2</version>
</dependency>
```
[//]: # ({x-version-update-end})


### Prerequisites

- [Java Development Kit (JDK) with version 8 or above][jdk]
- An active Azure account. If you don't have one, you can sign up for a [free account][azure_subscription]. Alternatively, you can use the [Azure Cosmos DB Emulator](https://docs.microsoft.com/azure/cosmos-db/local-emulator) for development and testing. As emulator HTTPS certificate is self-signed, you need to import its certificate to java trusted cert store as [explained here](https://docs.microsoft.com/azure/cosmos-db/local-emulator-export-ssl-certificates)
- (Optional) SLF4J is a logging facade.
- (Optional) [SLF4J binding](https://www.slf4j.org/manual.html) is used to associate a specific logging framework with SLF4J.
- (Optional) [Maven][maven]

SLF4J is only needed if you plan to use logging, please also download an SLF4J binding which will link the SLF4J API with the logging implementation of your choice. See the [SLF4J user manual](https://www.slf4j.org/manual.html) for more information.

The SDK provides Reactor Core-based async APIs. You can read more about Reactor Core and [Flux/Mono types here](https://projectreactor.io/docs/core/release/api/)

## Key concepts
The Azure Cosmos Test library can be used to inject failure into Azure Cosmos SDK for Java.

## Examples
The following section provides several code snippets covering how to create some of the most common failure injection scenario, including:
* [High Channel Acquisition Scenario](#high-channel-acquisition-scenario "High channel acquisition scenario")
* [Broken Connection Scenario](#broken-connection-scenario "Broken connection scenario")
* [Server Return Gone Scenario](#server-return-gone-scenario "Server gone scenario")
* [Random Connection Close Scenario](#random-connection-close-scenario "Random connection close scenario")

### High Channel Acquisition Scenario

```java readme-sample-highChannelAcquisitionScenario
FaultInjectionRule serverConnectionDelayRule =
    new FaultInjectionRuleBuilder("<YOUR RULE ID>")
        .condition(
            new FaultInjectionConditionBuilder()
                .operationType(FaultInjectionOperationType.CREATE_ITEM)
                .build()
        )
        .result(
            FaultInjectionResultBuilders
                .getResultBuilder(FaultInjectionServerErrorType.CONNECTION_DELAY)
                .delay(Duration.ofSeconds(6)) // default connection timeout is 5s
                .times(1)
                .build()
        )
        .duration(Duration.ofMinutes(5))
        .build();

CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(serverConnectionDelayRule)).block();
```

### Broken Connection Scenario
```java readme-sample-brokenConnectionScenario
FaultInjectionRule timeoutRule =
    new FaultInjectionRuleBuilder("<YOUR RULE ID>")
        .condition(
            new FaultInjectionConditionBuilder()
                .operationType(FaultInjectionOperationType.READ_ITEM)
                .build()
        )
        .result(
            FaultInjectionResultBuilders
                .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                .times(1)
                .delay(Duration.ofSeconds(6)) // the default time out is 5s
                .build()
        )
        .duration(Duration.ofMinutes(5))
        .build();

CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(timeoutRule)).block();
```

### Server Return Gone Scenario
```java readme-sample-serverReturnGoneScenario
FaultInjectionRule serverErrorRule =
    new FaultInjectionRuleBuilder("<YOUR RULE ID>")
        .condition(
            new FaultInjectionConditionBuilder()
                .operationType(FaultInjectionOperationType.READ_ITEM)
                .build()
        )
        .result(
            FaultInjectionResultBuilders
                .getResultBuilder(FaultInjectionServerErrorType.GONE)
                .times(1)
                .build()
        )
        .duration(Duration.ofMinutes(5))
        .build();

CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(serverErrorRule)).block();
```
### Random Connection Close Scenario

```java readme-sample-randomConnectionCloseScenario
FaultInjectionRule connectionErrorRule =
    new FaultInjectionRuleBuilder("<YOUR RULE ID>")
        .condition(
            new FaultInjectionConditionBuilder()
                .operationType(FaultInjectionOperationType.CREATE_ITEM)
                .endpoints(new FaultInjectionEndpointBuilder(FeedRange.forLogicalPartition(new PartitionKey("<YOUR PARTITION KEY>"))).build())
                .build()
        )
        .result(
            FaultInjectionResultBuilders
                .getResultBuilder(FaultInjectionConnectionErrorType.CONNECTION_CLOSE)
                .interval(Duration.ofSeconds(1))
                .threshold(1.0)
                .build()
        )
        .duration(Duration.ofSeconds(2))
        .build();

CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(connectionErrorRule)).block();
```

## Troubleshooting

### General

Azure Cosmos DB is a fast and flexible distributed database that scales seamlessly with guaranteed latency and throughput.
You do not have to make major architecture changes or write complex code to scale your database with Azure Cosmos DB.
Scaling up and down is as easy as making a single API call or SDK method call.
However, because Azure Cosmos DB is accessed via network calls there are client-side optimizations you can make to achieve peak performance when using Azure Cosmos DB Java SDK v4.

- [Performance][perf_guide] guide covers these client-side optimizations.

- [Troubleshooting Guide][troubleshooting] covers common issues, workarounds, diagnostic steps, and tools when you use Azure Cosmos DB Java SDK v4 with Azure Cosmos DB SQL API accounts.

### Enable Client Logging

## Next steps

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
[cosmos_introduction]: https://docs.microsoft.com/azure/cosmos-db/
[api_documentation]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-cosmos/latest/index.html
[cosmos_docs]: https://docs.microsoft.com/azure/cosmos-db/introduction
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[maven]: https://maven.apache.org/
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[azure_subscription]: https://azure.microsoft.com/free/
[troubleshooting]: https://docs.microsoft.com/azure/cosmos-db/troubleshoot-java-sdk-v4-sql
[perf_guide]: https://docs.microsoft.com/azure/cosmos-db/performance-tips-java-sdk-v4-sql?tabs=api-async
[quickstart]: https://docs.microsoft.com/azure/cosmos-db/create-sql-api-java?tabs=sync
