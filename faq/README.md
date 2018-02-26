
<!-- TOC depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Frequently Asked Questions](#frequently-asked-questions)
	- [Performance Guide for Prod](#performance-guide-for-prod)
		- [Use Proper Scheduler (Avoid stealing Eventloop IO Netty threads)](#use-proper-scheduler-avoid-stealing-eventloop-io-netty-threads)
		- [Disable netty's logging](#disable-nettys-logging)
		- [OS Open files Resource Limit](#os-open-files-resource-limit)
		- [Use native SSL implementation for netty](#use-native-ssl-implementation-for-netty)
	- [Future, CompletableFuture, and ListenableFuture](#future-completablefuture-and-listenablefuture)
	- [Changes in 1.0.0 compared to 0.9.0-rc2](#changes-in-100-compared-to-090-rc2)
- [Previous Releases and Corresponding Repo Branches](#previous-releases-and-corresponding-repo-branches)

<!-- /TOC -->
# Frequently Asked Questions

The SDK provide Reative Extension Observable based async API. You can read more about RxJava and Observable APIs here:
http://reactivex.io/RxJava/1.x/javadoc/rx/Observable.html

## API Code Sample

Code Sample for creating a Document:
```java
asyncClient = new AsyncDocumentClient.Builder()
				.withServiceEndpoint(TestConfigurations.HOST)
				.withMasterKey(TestConfigurations.MASTER_KEY)
				.withConnectionPolicy(ConnectionPolicy.GetDefault())
				.withConsistencyLevel(ConsistencyLevel.Session)
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
	                    System.err.println("an error happened in document creation: actual cause: "
												+ error.getMessage());
	                });
```

We have more examples in form of standalone unit tests:

Please check the [examples project](https://github.com/Azure/azure-cosmosdb-java/tree/moderakh/faq/examples/src/test/java/com/microsoft/azure/cosmosdb/rx/examples).

## Performance Guide for Prod
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
If you are not in debugging mode disable netty's logging altogether. Please note suppressing netty's loggging may not be enough. So if you are using log4j to remove the additional CPU costs incurred by ``org.apache.log4j.Category.callAppenders()`` from netty add the following line to your codebase:

```java
org.apache.log4j.Logger.getLogger("io.netty").setLevel(org.apache.log4j.Level.OFF);
```

### OS Open files Resource Limit
Some Linux systems (like Redhat) have an upper limit on the number of open files and total number of connections. Run the following to view the current limits:

```bash
ulimit -a
```

The number of open files (nofile) will need to be modified to allow for a larger connection pool size.

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

The SDK provide Reative Extension (Rx) Observable based async API You can read more about RxJava and Observable APIs here:
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
// on the result thread consider changing the scheduler see Use Proper Scheduler (Avoid Stealing Eventloop IO Netty threads) section
ListenableFuture<ResourceResponse<Document>> listenableFuture =
  ListenableFutureObservable.to(createDocObservable);

ResourceResponse<Document> rrd = listenableFuture.get();
```

For this to work you will need [RxJava Guava library dependency ](https://mvnrepository.com/artifact/io.reactivex/rxjava-guava/1.0.3) more information here https://github.com/ReactiveX/RxJavaGuava.

You can see more details on how to convert Observabels to Futures here:
https://dzone.com/articles/converting-between


## Changes in 1.0.0 compared to 0.9.0-rc2
* Release 1.0.0 has fully end to end support for non-blocking IO using netty library in Gateway mode. Direct Connectivity implementation is coming.
* Artifact id changed to ``azure-cosmosdb`` from ``azure-documentdb`` in 0.9.0-rc2.
* Java package name changed to ``com.microsoft.azure.cosmosdb`` from ``com.microsoft.azure.documentdb`` in 0.9.0-rc2
* ``FeedResponsePage`` renamed to ``FeedReponse``
* Some minor modifications to ConnectionPolicy configuration
* Some minor modification to ``FeedOptions``

# Previous Releases and Corresponding Repo Branches

| Version           | SHA1                                                                                      | Remarks                                               |
|-------------------|-------------------------------------------------------------------------------------------|-------------------------------------------------------|
| 1.0.0       | [1.0.0](https://github.com/Azure/azure-cosmosdb-java/tree/v1.0.0)               | Tagged release for 1.0.0 version of Azure Cosmos DB SDK. |
| 0.9.0-rc2       | [0.9.0-rc2](https://github.com/Azure/azure-cosmosdb-java/tree/v0.9.0-rc2)             | Release 1.0.0 deprecates this 0.9.0-rc2. Java Package name and artifact Id changed.
| 0.9-rc1       |            | Release 1.0.0 deprecate this release. Java Package name and artifact Id changed.
