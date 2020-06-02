# Azure CosmosDB Client Library for Java

[![Maven Central][cosmos_maven_svg]][cosmos_maven]

Azure Cosmos DB is Microsoft’s globally distributed, multi-model database service for operational and analytics workloads. It offers multi-mastering feature by automatically scaling throughput, compute, and storage.
This project provides SDK library in Java for interacting with [SQL API][sql_api_query] of [Azure Cosmos DB Database Service][cosmos_introduction].

[Source code][source_code] | [API reference documentation][api_documentation] | [Product documentation][cosmos_docs] |
[Samples][samples]

## Getting started
### Include the package

[//]: # ({x-version-update-start;com.microsoft.azure:azure-cosmos;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-cosmos</artifactId>
  <version>4.0.1-beta.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

Refer to maven central for previous [releases][cosmos_maven]

Refer to [javadocs][api_documentation] for more details on the package

### Prerequisites

- Java Development Kit 8
- An active Azure account. If you don't have one, you can sign up for a [free account][azure_subscription]. Alternatively, you can use the [Azure Cosmos DB Emulator](https://azure.microsoft.com/documentation/articles/documentdb-nosql-local-emulator) for development and testing. As emulator https certificate is self signed, you need to import its certificate to java trusted cert store as [explained here](https://docs.microsoft.com/en-us/azure/cosmos-db/local-emulator-export-ssl-certificates)
- (Optional) SLF4J is a logging facade.
- (Optional) [SLF4J binding](http://www.slf4j.org/manual.html) is used to associate a specific logging framework with SLF4J.
- (Optional) Maven

SLF4J is only needed if you plan to use logging, please also download an SLF4J binding which will link the SLF4J API with the logging implementation of your choice. See the [SLF4J user manual](http://www.slf4j.org/manual.html) for more information.

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
You may read more about databases, containers and items [here](https://docs.microsoft.com/en-us/azure/cosmos-db/databases-containers-items). 
A few important properties defined at the level of the container, among them are provisioned throughput and partition key.

- Azure Cosmos DB is a globally distributed database service that's designed to provide low latency, elastic scalability of throughput, well-defined semantics for data consistency, and high availability. 
In short, if your application needs guaranteed fast response time anywhere in the world, if it's required to be always online, and needs unlimited and elastic scalability of throughput and storage, you should build your application on Azure Cosmos DB.
You may read more about global distribution [here](https://docs.microsoft.com/en-us/azure/cosmos-db/distribute-data-globally).
- The provisioned throughput is measured in Request Units (RUs) which have a monetary price and are a substantial determining factor in the operating cost of the account. 
Provisioned throughput can be selected at per-container granularity or per-database granularity, however container-level throughput specification is typically preferred. 
You may read more about throughput provisioning [here](https://docs.microsoft.com/en-us/azure/cosmos-db/set-throughput).
- As items are inserted into a Cosmos DB container, the database grows horizontally by adding more storage and compute to handle requests. 
Storage and compute capacity are added in discrete units known as partitions, and you must choose one field in your documents to be the partition key which maps each document to a partition. 
The way partitions are managed is that each partition is assigned a roughly equal slice out of the range of partition key values; therefore you are advised to choose a partition key which is relatively random or evenly-distributed. 
Otherwise, some partitions will see substantially more requests (hot partition) while other partitions see substantially fewer requests (cold partition), and this is to be avoided. 
You may learn more about partitioning [here](https://docs.microsoft.com/en-us/azure/cosmos-db/partitioning-overview).

## Examples

See the complete code in [`HelloWorldDemo.java`](../azure-cosmos-examples/src/main/java/com/azure/cosmos/examples/HelloWorldDemo.java)

```java
import com.azure.cosmos.*;
import reactor.core.publisher.Mono;

import java.io.IOException;

// ...

    // Create a new CosmosAsyncClient via the CosmosClientBuilder
            // It only requires endpoint and key, but other useful settings are available
            CosmosAsyncClient client = new CosmosClientBuilder()
                .endpoint("<YOUR ENDPOINT HERE>")
                .key("<YOUR KEY HERE>")
                .buildAsyncClient();
    
            // Get a reference to the container
            // This will create (or read) a database and its container.
            CosmosAsyncContainer container = client.createDatabaseIfNotExists(DATABASE_NAME)
                // TIP: Our APIs are Reactor Core based, so try to chain your calls
                .flatMap(response -> client.getDatabase(DATABASE_NAME)
                    .createContainerIfNotExists(CONTAINER_NAME, "/id"))
                .flatMap(response -> Mono.just(client.getDatabase(DATABASE_NAME).getContainer(CONTAINER_NAME)))
                .block();
    
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

General [troubleshooting guide][troubleshooting] can be found [here][troubleshooting]

#### Common Perf Tips

There is a set of common perf tips written for our Java SDK. It is available [here][perf_guide].

To achieve better performance and higher throughput in production, there are a few more tips that are helpful to follow:

#### Use Appropriate Scheduler (Avoid stealing Eventloop IO Netty threads)

SDK uses [netty](https://netty.io/) for non-blocking IO. The SDK uses a fixed number of IO netty eventloop threads (as many CPU cores your machine has) for executing IO operations.

The Observable returned by API emits the result on one of the shared IO eventloop netty threads. So it is important to not block the shared IO eventloop netty threads. Doing CPU intensive work or blocking operation on the IO eventloop netty thread may cause deadlock or significantly reduce SDK throughput.

For example the following code executes a cpu intensive work on the eventloop IO netty thread:

```java
Mono<CosmosItemResponse> readItemMono = item.read();

readItemMono
  .subscribe(
  resourceResponse -> {
    //this is executed on eventloop IO netty thread.
    //the eventloop thread is shared and is meant to return back quickly.
    //
    // DON'T do this on eventloop IO netty thread.
    veryCpuIntensiveWork();
  });

```

After receiving result if you want to do CPU intensive work on the result you should avoid doing so on eventloop IO netty thread. You can instead provide your own Scheduler to provide your own thread for running your work.

```java

Mono<CosmosItemResponse> readItemMono = item.read();

readItemMono
  .subscribeOn(Schedulers.parallel())
  .subscribe(
  resourceResponse -> {
    // this is executed on threads provided by Scheduler.parallel()
    // Schedulers.parallel() should be used only when the work is cpu intensive and you are not doing blocking IO, thread sleep, etc. in this thread against other resources.
    veryCpuIntensiveWork();
  });

```

Based on the type of your work you should use the appropriate existing RxJava Scheduler for your work. Please read here
[`Schedulers`][project_reactor_schedulers].

#### Disable netty's logging

Netty library logging is very chatty and need to be turned off (suppressing log in the configuration may not be enough) to avoid additional CPU costs.
If you are not in debugging mode disable netty's logging altogether. So if you are using log4j to remove the additional CPU costs incurred by `org.apache.log4j.Category.callAppenders()` from netty add the following line to your codebase:

```java
org.apache.log4j.Logger.getLogger("io.netty").setLevel(org.apache.log4j.Level.OFF);
```

#### OS Open files Resource Limit

Some Linux systems (like Redhat) have an upper limit on the number of open files and so the total number of connections. Run the following to view the current limits:

```bash
ulimit -a
```

The number of open files (nofile) need to be large enough to have enough room for your configured connection pool size and other open files by the OS. It can be modified to allow for a larger connection pool size.

Open the limits.conf file:

```bash
vim /etc/security/limits.conf
```

Add/modify the following lines:

```
* - nofile 100000
```

#### Using system properties to modify default Direct TCP options

We have added the ability to modify the default Direct TCP options utilized by the SDK. In priority order we will take default Direct TCP options from:

1. The JSON value of system property `azure.cosmos.directTcp.defaultOptions`.
   Example: 
   ```bash
   java -Dazure.cosmos.directTcp.defaultOptions={\"idleEndpointTimeout\":\"PT24H\"} -jar target/cosmosdb-sdk-testing-1.0-jar-with-dependencies.jar Direct 10 0 Read
   ```

2. The contents of the JSON file located by system property `azure.cosmos.directTcp.defaultOptionsFile`.
   Example: 
   ```
   java -Dazure.cosmos.directTcp.defaultOptionsFile=/path/to/default/options/file -jar Direct 10 0 Query
   ```

3. The contents of the JSON resource file named `azure.cosmos.directTcp.defaultOptions.json`.
   Specifically, the resource file is read from this stream: 
   ```java
   RntbdTransportClient.class.getClassLoader().getResourceAsStream("azure.cosmos.directTcp.defaultOptions.json")
   ```
   Example: Contents of resource file `azure.cosmos.directTcp.defaultOptions.json`.
   ```json
   {
     "bufferPageSize": 8192,
     "connectionTimeout": "PT1M",
     "idleChannelTimeout": "PT0S",
     "idleEndpointTimeout": "PT1M10S",
     "maxBufferCapacity": 8388608,
     "maxChannelsPerEndpoint": 10,
     "maxRequestsPerChannel": 30,
     "receiveHangDetectionTime": "PT1M5S",
     "requestExpiryInterval": "PT5S",
     "requestTimeout": "PT1M",
     "requestTimerResolution": "PT0.5S",
     "sendHangDetectionTime": "PT10S",
     "shutdownTimeout": "PT15S"
   }

Values that are in error are ignored.

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
[source_code]: src
[cosmos_introduction]: https://docs.microsoft.com/en-us/azure/cosmos-db/
[api_documentation]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-cosmos/4.0.1-beta.3/index.html
[cosmos_docs]: https://docs.microsoft.com/en-us/azure/cosmos-db/introduction
[jdk]: https://docs.microsoft.com/java/azure/java-supported-jdk-runtime?view=azure-java-stable
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
[troubleshooting]: https://docs.microsoft.com/en-us/azure/cosmos-db/troubleshoot-java-sdk-v4-sql
[perf_guide]: https://docs.microsoft.com/en-us/azure/cosmos-db/performance-tips-java-sdk-v4-sql?tabs=api-async
[sql_api_query]: https://docs.microsoft.com/en-us/azure/cosmos-db/sql-api-sql-query
[getting_started]: https://github.com/Azure-Samples/azure-cosmos-java-getting-started
[quickstart]: https://docs.microsoft.com/en-us/azure/cosmos-db/create-sql-api-java?tabs=sync
[project_reactor_schedulers]: https://projectreactor.io/docs/core/release/api/reactor/core/scheduler/Schedulers.html

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcosmos%2FREADME.png)
