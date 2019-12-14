# Java SDK for SQL API of Azure Cosmos DB

[![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmos.svg)](https://search.maven.org/artifact/com.microsoft.azure/azure-cosmos)
[![Known Vulnerabilities](https://snyk.io/test/github/Azure/azure-cosmosdb-java/badge.svg?targetFile=sdk%2Fpom.xml)](https://snyk.io/test/github/Azure/azure-cosmosdb-java?targetFile=sdk%2Fpom.xml)

<!--[![Coverage Status](https://img.shields.io/codecov/c/github/Azure/azure-cosmos-java.svg)](https://codecov.io/gh/Azure/azure-cosmosdb-java)
![](https://img.shields.io/github/issues/azure/azure-cosmosdb-java.svg)
 -->

<!-- TOC depthFrom:2 depthTo:2 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Consuming the official Microsoft Azure Cosmos DB Java SDK](#consuming-the-official-microsoft-azure-cosmos-db-java-sdk)
- [Prerequisites](#prerequisites)
- [API Documentation](#api-documentation)
- [Usage Code Sample](#usage-code-sample)
- [Guide for Prod](#guide-for-prod)
- [FAQ](#faq)
- [Release changes](#release-changes)
- [Contribution and Feedback](#contribution-and-feedback)
- [License](#license)

<!-- /TOC -->

## Example

See the complete code for the above sample in [`HelloWorldDemo.java`](./microsoft-azure-cosmos-examples/src/main/java/com/azure/data/cosmos/examples/HelloWorldDemo.java)

```java
import com.azure.data.cosmos.*;
import reactor.core.publisher.Mono;

import java.io.IOException;

// ...

    // Create a new CosmosClient via the builder
    // It only requires endpoint and key, but other useful settings are available
    CosmosClient client = CosmosClient.builder()
        .endpoint("<YOUR ENDPOINT HERE>")
        .key("<YOUR KEY HERE>")
        .build();

    // Get a reference to the container
    // This will create (or read) a database and its container.
    CosmosContainer container = client.createDatabaseIfNotExists("contoso-travel")
        // TIP: Our APIs are Reactor Core based, so try to chain your calls
        .flatMap(response -> response.database()
                .createContainerIfNotExists("passengers", "/id"))
        .flatMap(response -> Mono.just(response.container()))
        .block(); // Blocking for demo purposes (avoid doing this in production unless you must)

    // Create an item
    container.createItem(new Passenger("carla.davis@outlook.com", "Carla Davis", "SEA", "IND"))
        .flatMap(response -> {
            System.out.println("Created item: " + response.properties().toJson());
            // Read that item
            return response.item().read();
        })
        .flatMap(response -> {
            System.out.println("Read item: " + response.properties().toJson());
            // Replace that item
            try {
                Passenger p = response.properties().getObject(Passenger.class);
                p.setDestination("SFO");
                return response.item().replace(p);
            } catch (IOException e) {
                System.err.println(e);
                return Mono.error(e);
            }
        })
        // delete that item
        .flatMap(response -> response.item().delete())
        .block(); // Blocking for demo purposes (avoid doing this in production unless you must)
// ...
```

We have a get started sample app available [here](https://github.com/Azure-Samples/azure-cosmos-db-sql-api-async-java-getting-started).

Also We have more examples in form of standalone unit tests in [examples project](examples/src/test/java/com/microsoft/azure/cosmosdb/rx/examples).

## Consuming the official Microsoft Azure Cosmos DB Java SDK

This project provides a SDK library in Java for interacting with [SQL API](https://docs.microsoft.com/en-us/azure/cosmos-db/sql-api-sql-query) of [Azure Cosmos DB
Database Service](https://azure.microsoft.com/en-us/services/cosmos-db/). This project also includes samples, tools, and utilities.

Jar dependency binary information for maven and gradle can be found here at [maven](https://mvnrepository.com/artifact/com.microsoft.azure/azure-cosmos).

For example, using maven, you can add the following dependency to your maven pom file:

[//]: # ({x-version-update-start;com.microsoft.azure:azure-cosmos;current})
```xml
<dependency>
  <groupId>com.microsoft.azure</groupId>
  <artifactId>azure-cosmos</artifactId>
  <version>3.4.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

Useful links:

- [Sample Get Started APP](https://github.com/Azure-Samples/azure-cosmos-db-sql-api-async-java-getting-started)
- [Introduction to Resource Model of Azure Cosmos DB Service](https://docs.microsoft.com/en-us/azure/cosmos-db/sql-api-resources)
- [Introduction to SQL API of Azure Cosmos DB Service](https://docs.microsoft.com/en-us/azure/cosmos-db/sql-api-sql-query)
- [Reactor Core JavaDoc API](https://projectreactor.io/docs/core/release/api/)
- [SDK FAQ](faq/)

## Prerequisites

- Java Development Kit 8
- An active Azure account. If you don't have one, you can sign up for a [free account](https://azure.microsoft.com/free/). Alternatively, you can use the [Azure Cosmos DB Emulator](https://azure.microsoft.com/documentation/articles/documentdb-nosql-local-emulator) for development and testing. As emulator https certificate is self signed, you need to import its certificate to java trusted cert store as [explained here](https://docs.microsoft.com/en-us/azure/cosmos-db/local-emulator-export-ssl-certificates)
- (Optional) SLF4J is a logging facade.
- (Optional) [SLF4J binding](http://www.slf4j.org/manual.html) is used to associate a specific logging framework with SLF4J.
- (Optional) Maven

SLF4J is only needed if you plan to use logging, please also download an SLF4J binding which will link the SLF4J API with the logging implementation of your choice. See the [SLF4J user manual](http://www.slf4j.org/manual.html) for more information.

<!-- TODO - update once JavaDoc is published
## API Documentation

Javadoc is available [here](https://azure.github.io/azure-cosmosdb-java/2.4.0/com/microsoft/azure/cosmosdb/rx/AsyncDocumentClient.html).

The SDK provides Reactor Core based async APIs. You can read more about Reactor Core and [Flux/Mono types here](https://projectreactor.io/docs/core/release/api/).
-->

## Guide for Production

To achieve better performance and higher throughput there are a few tips that are helpful to follow:

### Use Appropriate Scheduler (Avoid stealing Eventloop IO Netty threads)

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

After result is received if you want to do CPU intensive work on the result you should avoid doing so on eventloop IO netty thread. You can instead provide your own Scheduler to provide your own thread for running your work.

```java
import rx.schedulers;

Mono<CosmosItemResponse> readItemMono = item.read();

readItemMono
  .subscribeOn(Schedulers.computation())
  .subscribe(
  resourceResponse -> {
    // this is executed on threads provided by Scheduler.computation()
    // Schedulers.computation() should be used only the work is cpu intensive and you are not doing blocking IO, thread sleep, etc. in this thread against other resources.
    veryCpuIntensiveWork();
  });

```

Based on the type of your work you should use the appropriate existing RxJava Scheduler for your work. Please read here
[`Schedulers`](https://projectreactor.io/docs/core/release/api/reactor/core/scheduler/Schedulers.html).

### Disable netty's logging

Netty library logging is very chatty and need to be turned off (suppressing log in the configuration may not be enough) to avoid additional CPU costs.
If you are not in debugging mode disable netty's logging altogether. So if you are using log4j to remove the additional CPU costs incurred by `org.apache.log4j.Category.callAppenders()` from netty add the following line to your codebase:

```java
org.apache.log4j.Logger.getLogger("io.netty").setLevel(org.apache.log4j.Level.OFF);
```

### OS Open files Resource Limit

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

### Use native SSL implementation for netty

Netty can use OpenSSL directly for SSL implementation stack to achieve better performance.
In the absence of this configuration netty will fall back to Java's default SSL implementation.

on Ubuntu:

```bash
sudo apt-get install openssl
sudo apt-get install libapr1
```

and add the following dependency to your project maven dependencies:

```xml
<dependency>
  <groupId>io.netty</groupId>
  <artifactId>netty-tcnative</artifactId>
  <version>2.0.25.Final</version>
  <classifier>linux-x86_64</classifier>
</dependency>
```

For other platforms (Redhat, Windows, Mac, etc) please refer to these instructions https://netty.io/wiki/forked-tomcat-native.html

### Common Perf Tips

There is a set of common perf tips written for our Java SDK. It is available [here](https://docs.microsoft.com/en-us/azure/cosmos-db/performance-tips-async-java).

## FAQ

We have a frequently asked questions which is maintained [here](faq/).

## Release changes

Release changelog is available [here](changelog/).

## Contribution and Feedback

This is an open source project and we welcome contributions. If you would like to become an active contributor to this project please follow the instructions provided in [Azure Projects Contribution Guidelines](http://azure.github.io/guidelines/). Instructions on how to fetch and build the code can be found in [dev.md](./dev.md). Our PRs have CI that will run after a contributor has reviewed your code. You can run those same tests locally via the instructions in [dev.md](./dev.md). 

If you encounter any bugs with the SDK please file an [issue](https://github.com/Azure/azure-cosmosdb-java/issues) in the Issues section of the project.

## License

[MIT License](LICENSE)

Copyright (c) 2018 Copyright (c) Microsoft Corporation

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcosmos%2FREADME.png)
