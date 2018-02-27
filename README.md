<!-- TOC depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Java SDK for Document API of Azure Cosmos DB](#java-sdk-for-document-api-of-azure-cosmos-db)
	- [Consuming the official Microsoft Azure Cosmos DB Java SDK](#consuming-the-official-microsoft-azure-cosmos-db-java-sdk)
	- [Minimum Requirements](#minimum-requirements)
	- [API Documentation](#api-documentation)
	- [Usage Code Sample](#usage-code-sample)
	- [Guide for Prod](#guide-for-prod)
		- [Use Proper Scheduler (Avoid stealing Eventloop IO Netty threads)](#use-proper-scheduler-avoid-stealing-eventloop-io-netty-threads)
		- [Disable netty's logging](#disable-nettys-logging)
		- [OS Open files Resource Limit](#os-open-files-resource-limit)
		- [Use native SSL implementation for netty](#use-native-ssl-implementation-for-netty)
	- [Future, CompletableFuture, and ListenableFuture](#future-completablefuture-and-listenablefuture)
	- [Checking out the Code and Examples](#checking-out-the-code-and-examples)
		- [Eclipse](#eclipse)
		- [Command line](#command-line)
	- [Release changes](#release-changes)
	- [License](#license)

<!-- /TOC -->


# Java SDK for Document API of Azure Cosmos DB

![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb.svg)
[![Build Status](https://api.travis-ci.org/Azure/azure-cosmosdb-java.svg?branch=master)](https://travis-ci.org/Azure/azure-cosmosdb-java)
[![Coverage Status](https://img.shields.io/codecov/c/github/Azure/azure-cosmosdb-java.svg)](https://codecov.io/gh/Azure/azure-cosmosdb-java)
![](https://img.shields.io/github/issues/azure/azure-cosmosdb-java.svg)

## Consuming the official Microsoft Azure Cosmos DB Java SDK

Jar dependency binary information for maven and gradle can be found here at [maven]( https://mvnrepository.com/artifact/com.microsoft.azure/azure-cosmosdb/1.0.0)

For example, using maven, you can add the following dependency to your maven pom file:

```xml
<dependency>
  <groupId>com.microsoft.azure</groupId>
  <artifactId>azure-cosmosdb</artifactId>
  <version>LATEST</version>
</dependency>
```

## Minimum Requirements
* Java Development Kit 8

## API Documentation
Javadoc is available [here](https://azure.github.io/azure-cosmosdb-java).

The SDK provide Reactive Extension Observable based async API. You can read more about RxJava and Observable APIs here:
http://reactivex.io/RxJava/1.x/javadoc/rx/Observable.html

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

We have more examples in form of standalone unit tests:

Please check the [examples project](https://github.com/Azure/azure-cosmosdb-java/tree/moderakh/faq/examples/src/test/java/com/microsoft/azure/cosmosdb/rx/examples).

## Guide for Prod
To achieve better performance and higher throughput there are a few tips that are helpful to follow:

### Use Proper Scheduler (Avoid stealing Eventloop IO Netty threads)
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
    veryCpuIntensiveWork();
  });

```

[``Schedulers.computation()``](http://reactivex.io/RxJava/javadoc/rx/schedulers/Schedulers.html#computation--) is the simplest schdeduler you can use which is suitable for CPU intensive work. If you are doing non cpu intensive work with blocking nature, e.g, blocking IO (reading or writing to files, etc), you should use [``Schedulers.io``](http://reactivex.io/RxJava/javadoc/rx/schedulers/Schedulers.html#io--)
or provide your own customized scheduler.


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

The number of open files (nofile) will need have enough room for your configured connection pool size and other open files by the OS. It can be modified to allow for a larger connection pool size.

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

For other platforms or more details please refer to these instructions https://netty.io/wiki/forked-tomcat-native.html


## Future, CompletableFuture, and ListenableFuture

The SDK provide Reactive Extension (Rx) Observable based async API You can read more about RxJava and Observable APIs here:
http://reactivex.io/RxJava/1.x/javadoc/rx/Observable.html

RX API has some advantages over Future based APIs. But if you wish to use ``Future`` you can translate Observables to Java native Futures.

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

For this to work you will need [RxJava Guava library dependency ](https://mvnrepository.com/artifact/io.reactivex/rxjava-guava/1.0.3) more information here https://github.com/ReactiveX/RxJavaGuava.

You can see more details on how to convert Observables to Futures here:
https://dzone.com/articles/converting-between

## Checking out the Code and Examples
 Clone the Repo
```bash
git clone https://github.com/Azure/azure-cosmosdb-java.git
cd azure-cosmosdb-java
```

You can run the samples either using Eclipse or from Command Line using Maven:

### Eclipse

* Load the main parent project pom file in Eclipse (That should automatically load examples).
* For running the samples you need a proper Azure Cosmos DB Endpoint. The endpoints are picked up from [TestConfigurations.java](https://github.com/Azure/azure-cosmosdb-java/blob/master/examples/src/test/java/com/microsoft/azure/cosmosdb/rx/examples/TestConfigurations.java).
* You can pass your endpoint credentials as VM Arguments in Eclipse JUnit Run Config:
```bash
 -DACCOUNT_HOST="https://REPLACE_ME.documents.azure.com:443/" -DACCOUNT_KEY="REPLACE_ME"
 ```
* or you can simply put your endpoint credentials in TestConfigurations.java
* Now you can run the samples as JUnit tests in Eclipse.

### Command line

The other way for running samples is to use maven:

* Run Maven and pass your Azure Cosmos DB Endpoint credentials:
```bash
mvn test -DACCOUNT_HOST="https://REPLACE_ME_WITH_YOURS.documents.azure.com:443/" -DACCOUNT_KEY="REPLACE_ME_WITH_YOURS"
```

## Release changes
Release changelog is available [here](changelog/).

## License
MIT License
Copyright (c) 2018 Copyright (c) Microsoft Corporation
