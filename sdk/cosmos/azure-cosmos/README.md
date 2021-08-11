# Azure CosmosDB Client Library for Java

Azure Cosmos DB is Microsoft’s globally distributed, multi-model database service for operational and analytics workloads. It offers multi-mastering feature by automatically scaling throughput, compute, and storage.
This project provides SDK library in Java for interacting with [SQL API][sql_api_query] of [Azure Cosmos DB Database Service][cosmos_introduction].

[Source code][source_code] | [Package (Maven)][cosmos_maven] | [API reference documentation][api_documentation] | [Product documentation][cosmos_docs] |
[Samples][samples]

## Getting started
### Include the package
#### Include the BOM file

Please include the azure-sdk-bom to your project to take dependency on GA version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
To learn more about the BOM, see the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-sdk-bom</artifactId>
            <version>{bom_version_to_target}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
and then include the direct dependency in the dependencies section without the version tag.

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-cosmos</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-cosmos;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-cosmos</artifactId>
  <version>4.17.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

Refer to maven central for previous [releases][cosmos_maven]

Refer to [javadocs][api_documentation] for more details on the package

### Prerequisites

- [Java Development Kit (JDK) with version 8 or above][jdk]
- An active Azure account. If you don't have one, you can sign up for a [free account][azure_subscription]. Alternatively, you can use the [Azure Cosmos DB Emulator](https://azure.microsoft.com/documentation/articles/documentdb-nosql-local-emulator) for development and testing. As emulator https certificate is self signed, you need to import its certificate to java trusted cert store as [explained here](https://docs.microsoft.com/azure/cosmos-db/local-emulator-export-ssl-certificates)
- (Optional) SLF4J is a logging facade.
- (Optional) [SLF4J binding](https://www.slf4j.org/manual.html) is used to associate a specific logging framework with SLF4J.
- (Optional) [Maven][maven]

SLF4J is only needed if you plan to use logging, please also download an SLF4J binding which will link the SLF4J API with the logging implementation of your choice. See the [SLF4J user manual](https://www.slf4j.org/manual.html) for more information.

The SDK provides Reactor Core based async APIs. You can read more about Reactor Core and [Flux/Mono types here](https://projectreactor.io/docs/core/release/api/)

### Authenticate the client

In order to interact with the Azure CosmosDB service you'll need to create an instance of the Cosmos Client class. To make this possible you will need an url and key of the Azure CosmosDB service.

The SDK provides two clients.
1. `CosmosAsyncClient` for operations using asynchronous APIs.
2. `CosmosClient` for operations using synchronous (blocking) APIs.

#### Create CosmosAsyncClient
```java
CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
.endpoint(serviceEndpoint)
.key(key)
.buildAsyncClient();
```

#### Create CosmosClient
```java
CosmosClient cosmosClient = new CosmosClientBuilder()
.endpoint(serviceEndpoint)
.key(key)
.buildClient();
```

## Key Concepts

Azure Cosmos DB Java SDK provides client-side logical representation to access the Azure Cosmos DB SQL API.
A Cosmos DB account contains zero or more databases, a database (DB) contains zero or more containers, and a container contains zero or more items.
You may read more about databases, containers and items [here](https://docs.microsoft.com/azure/cosmos-db/databases-containers-items).
A few important properties defined at the level of the container, among them are provisioned throughput and partition key.

### Global Distribution
- Azure Cosmos DB is a globally distributed database service that's designed to provide low latency, elastic scalability of throughput, well-defined semantics for data consistency, and high availability.
In short, if your application needs guaranteed fast response time anywhere in the world, if it's required to be always online, and needs unlimited and elastic scalability of throughput and storage, you should build your application on Azure Cosmos DB.
You may read more about global distribution [here](https://docs.microsoft.com/azure/cosmos-db/distribute-data-globally).

### Throughput Provisioning
- Azure Cosmos DB allows you to set provisioned throughput on your databases and containers.
There are two types of provisioned throughput, standard (manual) or autoscale. Provisioned throughput can be selected at per-container granularity or per-database granularity, however container-level throughput specification is typically preferred.
You may read more about throughput provisioning [here](https://docs.microsoft.com/azure/cosmos-db/set-throughput).

### Request Units (RUs)
- Azure Cosmos DB supports many APIs, such as SQL, MongoDB, Cassandra, Gremlin, and Table.
Each API has its own set of database operations. These operations range from simple point reads and writes to complex queries.
Each database operation consumes system resources based on the complexity of the operation. The cost of all database operations is normalized by Azure Cosmos DB and is expressed by Request Units (or RUs, for short).
You can think of RUs per second as the currency for throughput. RUs per second is a rate-based currency. It abstracts the system resources such as CPU, IOPS, and memory that are required to perform the database operations supported by Azure Cosmos DB.
You may read more about request units [here](https://docs.microsoft.com/azure/cosmos-db/request-units).

### Partitioning
- As items are inserted into a Cosmos DB container, the database grows horizontally by adding more storage and compute to handle requests.
Storage and compute capacity are added in discrete units known as partitions, and you must choose one field in your documents to be the partition key which maps each document to a partition.
The way partitions are managed is that each partition is assigned a roughly equal slice out of the range of partition key values; therefore you are advised to choose a partition key which is relatively random or evenly-distributed.
Otherwise, some partitions will see substantially more requests (hot partition) while other partitions see substantially fewer requests (cold partition), and this is to be avoided.
You may learn more about partitioning [here](https://docs.microsoft.com/azure/cosmos-db/partitioning-overview).

## Examples

The following section provides several code snippets covering some of the most common CosmosDB SQL API tasks, including:
* [Create Cosmos Client](#create-cosmos-client "Create Cosmos Client")
* [Create Database](#create-database "Create Database")
* [Create Container](#create-container "Create Container")
* [CRUD operation on Items](#crud-operation-on-items "CRUD operation on Items")

### Create Cosmos Client
```java
// Create a new CosmosAsyncClient via the CosmosClientBuilder
// It only requires endpoint and key, but other useful settings are available
CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
    .endpoint("<YOUR ENDPOINT HERE>")
    .key("<YOUR KEY HERE>")
    .buildAsyncClient();

// Create a new CosmosClient via the CosmosClientBuilder
CosmosClient cosmosClient = new CosmosClientBuilder()
    .endpoint("<YOUR ENDPOINT HERE>")
    .key("<YOUR KEY HERE>")
    .buildClient();

// Create a new CosmosClient with customizations
CosmosClient cosmosClient = new CosmosClientBuilder()
    .endpoint(serviceEndpoint)
    .key(key)
    .directMode(directConnectionConfig, gatewayConnectionConfig)
    .consistencyLevel(ConsistencyLevel.SESSION)
    .connectionSharingAcrossClientsEnabled(true)
    .contentResponseOnWriteEnabled(true)
    .userAgentSuffix("my-application1-client")
    .preferredRegions(Collections.singletonList("West US", "East US"))
    .buildClient();
```

### Create Database
Using any one of the clients created in previous example, you can create a database like this:

```java
// Get a reference to the container
// This will create (or read) a database and its container.
client.createDatabaseIfNotExists(DATABASE_NAME)
    // TIP: Our APIs are Reactor Core based, so try to chain your calls
    .flatMap(response -> client.getDatabase(DATABASE_NAME)
    .subscribe();
```

### Create Container
Using the above created database, you can chain another operation to it for creating a container like this:

```java
client.createDatabaseIfNotExists(DATABASE_NAME)
    // TIP: Our APIs are Reactor Core based, so try to chain your calls
    .flatMap(response -> client.getDatabase(DATABASE_NAME)
    // Create Container
    .createContainerIfNotExists(CONTAINER_NAME, "/id"))
    .flatMap(response -> Mono.just(client.getDatabase(DATABASE_NAME).getContainer(CONTAINER_NAME)))
    .subscribe();
```
### CRUD operation on Items

```java

// Create an item
container.createItem(new Passenger("carla.davis@outlook.com", "Carla Davis", "SEA", "IND"))
    .flatMap(response -> {
        System.out.println("Created item: " + response.getItem());
        // Read that item 👓
        return container.readItem(response.getItem().getId(),
                                  new PartitionKey(response.getItem().getId()),
                                  Passenger.class);
    })
    .flatMap(response -> {
        System.out.println("Read item: " + response.getItem());
        // Replace that item 🔁
        Passenger p = response.getItem();
        p.setDestination("SFO");
        return container.replaceItem(p,
                                     response.getItem().getId(),
                                     new PartitionKey(response.getItem().getId()));
    })
    // delete that item 💣
    .flatMap(response -> container.deleteItem(response.getItem().getId(),
                                              new PartitionKey(response.getItem().getId())))
    .block(); // Blocking for demo purposes (avoid doing this in production unless you must)
// ...
```

We have a get started sample app available [here][getting_started].

Also, we have more examples [examples project][samples].

## Troubleshooting

### General

Azure Cosmos DB is a fast and flexible distributed database that scales seamlessly with guaranteed latency and throughput.
You do not have to make major architecture changes or write complex code to scale your database with Azure Cosmos DB.
Scaling up and down is as easy as making a single API call or SDK method call.
However, because Azure Cosmos DB is accessed via network calls there are client-side optimizations you can make to achieve peak performance when using Azure Cosmos DB Java SDK v4.

- [Performance][perf_guide] guide covers these client-side optimizations.

- [Troubleshooting guide][troubleshooting] covers common issues, workarounds, diagnostic steps, and tools when you use Azure Cosmos DB Java SDK v4 with Azure Cosmos DB SQL API accounts.

### Enable Client Logging
Azure Cosmos DB Java SDK v4 uses SLF4j as the logging facade that supports logging into popular logging frameworks such as log4j and logback.

For example, if you want to use log4j as the logging framework, add the following libs in your Java classpath.

```xml
<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-log4j12</artifactId>
  <version>${slf4j.version}</version>
</dependency>
<dependency>
  <groupId>log4j</groupId>
  <artifactId>log4j</artifactId>
  <version>${log4j.version}</version>
</dependency>
```

Also add a log4j config.

```properties
# this is a sample log4j configuration

# Set root logger level to INFO and its only appender to A1.
log4j.rootLogger=INFO, A1

log4j.category.com.azure.cosmos=INFO
#log4j.category.io.netty=OFF
#log4j.category.io.projectreactor=OFF
# A1 is set to be a ConsoleAppender.
log4j.appender.A1=org.apache.log4j.ConsoleAppender

# A1 uses PatternLayout.
log4j.appender.A1.layout=org.apache.log4j.PatternLayout
log4j.appender.A1.layout.ConversionPattern=%d %5X{pid} [%t] %-5p %c - %m%n
```

## Next Steps

- Samples are explained in detail [here][samples_readme]
- Go through [quickstart][quickstart] - Building a java app to manage CosmosDB SQL API data
- [Read more about Azure CosmosDB Service][cosmos_docs]

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
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos/src
[cosmos_introduction]: https://docs.microsoft.com/azure/cosmos-db/
[api_documentation]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-cosmos/latest/index.html
[cosmos_docs]: https://docs.microsoft.com/azure/cosmos-db/introduction
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[slf4j]: http://www.slf4j.org/
[maven]: https://maven.apache.org/
[cosmos_maven]: https://search.maven.org/artifact/com.azure/azure-cosmos
[cosmos_maven_svg]: https://img.shields.io/maven-central/v/com.azure/azure-cosmos.svg
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[azure_subscription]: https://azure.microsoft.com/free/
[samples]: https://github.com/Azure-Samples/azure-cosmos-java-sql-api-samples
[samples_readme]: https://github.com/Azure-Samples/azure-cosmos-java-sql-api-samples/blob/master/README.md
[troubleshooting]: https://docs.microsoft.com/azure/cosmos-db/troubleshoot-java-sdk-v4-sql
[perf_guide]: https://docs.microsoft.com/azure/cosmos-db/performance-tips-java-sdk-v4-sql?tabs=api-async
[sql_api_query]: https://docs.microsoft.com/azure/cosmos-db/sql-api-sql-query
[getting_started]: https://github.com/Azure-Samples/azure-cosmos-java-getting-started
[quickstart]: https://docs.microsoft.com/azure/cosmos-db/create-sql-api-java?tabs=sync
[project_reactor_schedulers]: https://projectreactor.io/docs/core/release/api/reactor/core/scheduler/Schedulers.html

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcosmos%2Fazure-cosmos%2FREADME.png)
