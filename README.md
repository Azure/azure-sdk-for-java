

# Java SDK for SQL API of Azure Cosmos DB
![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb.svg)
[![Build Status](https://api.travis-ci.org/Azure/azure-cosmosdb-java.svg?branch=master)](https://travis-ci.org/Azure/azure-cosmosdb-java)
<!--[![Coverage Status](https://img.shields.io/codecov/c/github/Azure/azure-cosmosdb-java.svg)](https://codecov.io/gh/Azure/azure-cosmosdb-java)
![](https://img.shields.io/github/issues/azure/azure-cosmosdb-java.svg)
 -->

<!-- TOC depthFrom:2 depthTo:2 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Consuming the official Microsoft Azure Cosmos DB Java SDK](#consuming-the-official-microsoft-azure-cosmos-db-java-sdk)
- [Prerequisites](#prerequisites)
- [API Documentation](#api-documentation)
- [Usage Code Sample](#usage-code-sample)
- [Guide for Prod](#guide-for-prod)
- [Future, CompletableFuture, and ListenableFuture](#future-completablefuture-and-listenablefuture)
- [Checking out the Source Code](#checking-out-the-source-code)
- [FAQ](#faq)
- [Release changes](#release-changes)
- [Contribution and Feedback](#contribution-and-feedback)
- [License](#license)

<!-- /TOC -->


## Consuming the official Microsoft Azure Cosmos DB Java SDK

This project provides a SDK library in Java for interacting with [SQL API](https://docs.microsoft.com/en-us/azure/cosmos-db/sql-api-sql-query) of [Azure Cosmos DB
Database Service](https://azure.microsoft.com/en-us/services/cosmos-db/). This project also includes samples, tools, and utilities.

Jar dependency binary information for maven and gradle can be found here at [maven]( https://mvnrepository.com/artifact/com.microsoft.azure/azure-cosmosdb/1.0.1).

For example, using maven, you can add the following dependency to your maven pom file:

```xml
<dependency>
  <groupId>com.microsoft.azure</groupId>
  <artifactId>azure-cosmosdb</artifactId>
  <version>1.0.1</version>
</dependency>
```



Useful links:
- [Sample Get Started APP](https://github.com/Azure-Samples/azure-cosmos-db-sql-api-async-java-getting-started)
- [Introduction to Resource Model of Azure Cosmos DB Service]( https://docs.microsoft.com/en-us/azure/cosmos-db/sql-api-resources)
- [Introduction to SQL API of Azure Cosmos DB Service](https://docs.microsoft.com/en-us/azure/cosmos-db/sql-api-sql-query)
- [SDK JavaDoc API](https://azure.github.io/azure-cosmosdb-java/1.0.1/com/microsoft/azure/cosmosdb/rx/AsyncDocumentClient.html)
- [RxJava Observable JavaDoc API](http://reactivex.io/RxJava/1.x/javadoc/rx/Observable.html)
- [SDK FAQ](faq/)

## Prerequisites
* Java Development Kit 8
* An active Azure account. If you don't have one, you can sign up for a [free account](https://azure.microsoft.com/free/). Alternatively, you can use the [Azure Cosmos DB Emulator](https://azure.microsoft.com/documentation/articles/documentdb-nosql-local-emulator) for development and testing. As emulator https certificate is self signed, you need to import its certificate to java trusted cert store as [explained here](https://docs.microsoft.com/en-us/azure/cosmos-db/local-emulator-export-ssl-certificates)
* (Optional) SLF4J is a logging facade.
* (Optional) [SLF4J binding](http://www.slf4j.org/manual.html) is used to associate a specific logging framework with SLF4J.
* (Optional) Maven

SLF4J is only needed if you plan to use logging, please also download an SLF4J binding which will link the SLF4J API with the logging implementation of your choice. See the [SLF4J user manual](http://www.slf4j.org/manual.html) for more information.


## API Documentation
Javadoc is available [here](https://azure.github.io/azure-cosmosdb-java/1.0.1/com/microsoft/azure/cosmosdb/rx/AsyncDocumentClient.html).

The SDK provide Reactive Extension Observable based async API. You can read more about RxJava and [Observable APIs here](http://reactivex.io/RxJava/1.x/javadoc/rx/Observable.html).


## Usage Code Sample

Code Sample for creating a Document:
```java
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

asyncClient = new AsyncDocumentClient.Builder()
				.withServiceEndpoint(HOST)
				.withMasterKey(MASTER_KEY)
				.withConnectionPolicy(ConnectionPolicy.GetDefault())
				.withConsistencyLevel(ConsistencyLevel.Eventual)
				.build();

Document doc = new Document(String.format("{ 'id': 'doc%d', 'counter': '%d'}", 1, 1));

Observable<ResourceResponse<Document>> createDocumentObservable =
	asyncClient.createDocument(collectionLink, doc, null, false);
	createDocumentObservable
	            .single()           // we know there will be one response
	            .subscribe(

	                documentResourceResponse -> {
	                    System.out.println(documentResourceResponse.getRequestCharge());
	                },

	                error -> {
	                    System.err.println("an error happened: " + error.getMessage());
	                });
```

We have a get started sample app available [here](https://github.com/Azure-Samples/azure-cosmos-db-sql-api-async-java-getting-started).

Also We have more examples in form of standalone unit tests in [examples project](examples/src/test/java/com/microsoft/azure/cosmosdb/rx/examples).


## Guide for Prod
To achieve better performance and higher throughput there are a few tips that are helpful to follow:

### Use Appropriate Scheduler (Avoid stealing Eventloop IO Netty threads)
SDK uses [netty](https://netty.io/) for non-blocking IO. The SDK uses a fixed number of IO netty eventloop threads (as many CPU cores your machine has) for executing IO operations.

 The Observable returned by API emits the result on one of the shared IO eventloop netty threads. So it is important to not block the shared IO eventloop netty threads. Doing CPU intensive work or blocking operation on the IO eventloop netty thread may cause deadlock or significantly reduce SDK throughput.

For example the following code executes a cpu intensive work on the eventloop IO netty thread:


```java
Observable<ResourceResponse<Document>> createDocObs = asyncDocumentClient.createDocument(
  collectionLink, document, null, true);

createDocObs.subscribe(
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

Observable<ResourceResponse<Document>> createDocObs = asyncDocumentClient.createDocument(
  collectionLink, document, null, true);

createDocObs.subscribeOn(Schedulers.computation())
subscribe(
  resourceResponse -> {
    // this is executed on threads provided by Scheduler.computation()
    // Schedulers.computation() should be used only the work is cpu intensive and you are not doing blocking IO, thread sleep, etc. in this thread against other resources.
    veryCpuIntensiveWork();
  });

```

Based on the type of your work you should use the appropriate existing RxJava Scheduler for your work. Please read here
[``Schedulers``](http://reactivex.io/RxJava/1.x/javadoc/rx/schedulers/Schedulers.html).


### Disable netty's logging
Netty library logging is very chatty and need to be turned off (suppressing log in the configuration may not be enough) to avoid additional CPU costs.
If you are not in debugging mode disable netty's logging altogether. So if you are using log4j to remove the additional CPU costs incurred by ``org.apache.log4j.Category.callAppenders()`` from netty add the following line to your codebase:

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
  <version>2.0.7.Final</version>
  <classifier>linux-x86_64</classifier>
</dependency>
```

For other platforms (Redhat, Windows, Mac, etc) please refer to these instructions https://netty.io/wiki/forked-tomcat-native.html

### Common Perf Tips
There is a set of common perf tips written for our sync SDK. The majority of them also apply to the async SDK. It is available [here](https://docs.microsoft.com/en-us/azure/cosmos-db/performance-tips-java).

## Future, CompletableFuture, and ListenableFuture

The SDK provide Reactive Extension (Rx) [Observable](http://reactivex.io/RxJava/1.x/javadoc/rx/Observable.html) based async API.


RX API has advantages over Future based APIs. But if you wish to use ``Future`` you can translate Observables to Java native Futures:

```java
// You can convert an Observable to a ListenableFuture.
// ListenableFuture (part of google guava library) is a popular extension
// of Java's Future which allows registering listener callbacks:
// https://github.com/google/guava/wiki/ListenableFutureExplained

import rx.observable.ListenableFutureObservable;

Observable<ResourceResponse<Document>> createDocObservable = asyncClient.createDocument(
  collectionLink, document, null, false);

// NOTE: if you are going to do CPU intensive work
// on the result thread consider changing the scheduler see Use Proper Scheduler
// (Avoid Stealing Eventloop IO Netty threads) section
ListenableFuture<ResourceResponse<Document>> listenableFuture =
  ListenableFutureObservable.to(createDocObservable);

ResourceResponse<Document> rrd = listenableFuture.get();
```

For this to work you will need [RxJava Guava library dependency ](https://mvnrepository.com/artifact/io.reactivex/rxjava-guava/1.0.3). More information available here https://github.com/ReactiveX/RxJavaGuava.

You can see more details on how to convert Observables to Futures here:
https://dzone.com/articles/converting-between

## Checking out the Source Code
The SDK is open source and is available here [sdk](sdk/).

 Clone the Repo
```bash
git clone https://github.com/Azure/azure-cosmosdb-java.git
cd azure-cosmosdb-java
```

### How to Build from Command Line

* Run the following maven command to build:

```bash
maven clean package -DskipTests
```

#### Running Tests from Command Line

Running tests require Azure Cosmos DB Endpoint credentials:

```bash
mvn test -DACCOUNT_HOST="https://REPLACE_ME_WITH_YOURS.documents.azure.com:443/" -DACCOUNT_KEY="REPLACE_ME_WITH_YOURS"
```

### Import into Intellij or Eclipse

* Load the main parent project pom file in Intellij/Eclipse (That should automatically load examples).
* For running the samples you need a proper Azure Cosmos DB Endpoint. The endpoints are picked up from [TestConfigurations.java](examples/src/test/java/com/microsoft/azure/cosmosdb/rx/examples/TestConfigurations.java). There is a similar endpoint config file for the sdk tests [here](sdk/src/test/java/com/microsoft/azure/cosmosdb/rx/TestConfigurations.java).
* You can pass your endpoint credentials as VM Arguments in Eclipse JUnit Run Config:
```bash
 -DACCOUNT_HOST="https://REPLACE_ME.documents.azure.com:443/" -DACCOUNT_KEY="REPLACE_ME"
 ```
* or you can simply put your endpoint credentials in TestConfigurations.java
* The SDK tests are written using TestNG framework, if you use Eclipse you may have to
  add TestNG plugin to your eclipse IDE as explained [here](http://testng.org/doc/eclipse.html).
  Intellij has builtin support for TestNG.
* Now you can run the tests in your Intellij/Eclipse IDE.


## FAQ
We have a frequently asked questions which is maintained [here](faq/).

## Release changes
Release changelog is available [here](changelog/).


## Contribution and Feedback

This is an open source project and we welcome contributions.

If you would like to become an active contributor to this project please follow the instructions provided in [Azure Projects Contribution Guidelines](http://azure.github.io/guidelines/).

We have [travis build CI](https://travis-ci.org/Azure/azure-cosmosdb-java) which should pass for any PR.

If you encounter any bugs with the SDK please file an [issue](https://github.com/Azure/azure-cosmosdb-java/issues) in the Issues section of the project.


## License
MIT License
Copyright (c) 2018 Copyright (c) Microsoft Corporation
