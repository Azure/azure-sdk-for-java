# Guidance on thread pool and connection pool configuration for Azure Client

There are four types of pools used by Azure client:
1. Http connection pool (used by `HttpClient`)
2. Http thread pool (used by `HttpClient`)
3. Azure Identity thread pool (used by Azure Identity client for token acquisition)
4. Reactor thread pool (if you are using [azure-core-http-netty](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core/azure-core-http-netty) as the `HttpClient` implementation)

The `HttpClient` is a generic interface for sending HTTP requests and getting responses.
  * [azure-core-http-netty](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core/azure-core-http-netty) provides a Netty derived HTTP client.
  * [azure-core-http-okhttp](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core/azure-core-http-okhttp) provides an OkHttp derived HTTP client.

In this guide, we use [azure-core-http-netty](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core/azure-core-http-netty) for `HttpClient` implementation as demonstration.

Now, we'll break down on how to control these pool size to meet your need.

## Prerequisites
* [Azure Resource Manager](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/resourcemanager#getting-started) to get start with Azure client
* [Azure Core Netty HTTP client](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core/azure-core-http-netty) for Reactor Netty implementation of [Azure Core](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core) HttpClient
* [Azure Identity](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity) for Microsoft Entra ID token authentication support across the Azure SDK
* Basic concepts of [Project Reactor](https://projectreactor.io/)

## Code Example
In the code example, you can find:
1. [How to configure connection pool size for the singleton HttpClient(NettyAsyncHttpClient as implementation)](#configure-connection-pool-for-the-singleton-httpclient)
2. [How to configure thread pool size for the singleton HttpClient(NettyAsyncHttpClient as implementation)](#configure-thread-pool-for-the-singleton-httpclient)
3. [How to configure thread pool size and HttpClient used by the singleton Azure Identity client](#configure-azure-identity-client-to-use-the-singleton-httpclient-defaultazurecredential-for-example)
4. [How to configure Azure client to use the singleton HttpClient and Azure Identity client to share a common thread pool and connection pool](#configure-azure-clients-to-use-the-singleton-httpclient-and-azure-identity-client)
5. [How to configure reactor thread pool size](#configure-reactor-thread-pool-size)

For step 1 and 2, there are configurations for other HttpClient implementations as well, e.g. [Azure Core OkHttp HTTP client](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core/azure-core-http-okhttp).

### Configure connection pool for the singleton HttpClient:
By default, Reactor Netty HttpClient uses a "fixed" connection pool with 500 as the maximum number of active channels and 1000 as the maximum number of further channel acquisition attempts allowed to be kept in a pending state.
```java readme-sample-azureClientConnectionPoolReactorNetty
NettyAsyncHttpClientBuilder singletonHttpClientBuilder = new NettyAsyncHttpClientBuilder();
singletonHttpClientBuilder
    // Connection pool configuration.
    .connectionProvider(
        ConnectionProvider.builder("connection-pool")
            // By default, HttpClient uses a "fixed" connection pool with 500 as the maximum number of active channels
            // and 1000 as the maximum number of further channel acquisition attempts allowed to be kept in a pending state.
            .maxConnections(500)
            // When the maximum number of channels in the pool is reached, up to specified new attempts to
            // acquire a channel are delayed (pending) until a channel is returned to the pool again, and further attempts are declined with an error.
            .pendingAcquireMaxCount(1000)
            .maxIdleTime(Duration.ofSeconds(20)) // Configures the maximum time for a connection to stay idle to 20 seconds.
            .maxLifeTime(Duration.ofSeconds(60)) // Configures the maximum time for a connection to stay alive to 60 seconds.
            .pendingAcquireTimeout(Duration.ofSeconds(60)) // Configures the maximum time for the pending acquire operation to 60 seconds.
            .evictInBackground(Duration.ofSeconds(120)) // Every two minutes, the connection pool is regularly checked for connections that are applicable for removal.
            .build());
```
Reference: [Reactor Netty Connection Pool](https://projectreactor.io/docs/netty/release/reference/#_connection_pool_2)

### Configure thread pool for the singleton HttpClient:
By default the Reactor Netty HttpClient uses an "Event Loop Group", where the number of the worker threads equals the number of processors available to the runtime on initialization (but with a minimum value of 4). When you need a different configuration, you can use one of the LoopResource#create methods.
```java readme-sample-azureClientThreadPoolReactorNetty
// Thread pool configuration.
singletonHttpClientBuilder
    .eventLoopGroup(LoopResources
        .create(
            "client-thread-pool", // thread pool name
            Runtime.getRuntime().availableProcessors() * 2, // thread pool size
            true)
        // we use our custom event loop here, disable the native one
        .onClient(false))
    .build();
```
Reference: [Reactor Netty Event Loop Group](https://projectreactor.io/docs/netty/release/reference/#client-tcp-level-configurations-event-loop-group)

### Configure Azure Identity client to use the singleton HttpClient (DefaultAzureCredential for example):
By default, Azure Identity uses `ForkJoinPool.commonPool()` for token acquisition. The pool size equals the number of processors available to the runtime on initialization minus 1 (with a minimum of 1). 

There are known issues with this approach:
 - [[BUG] ClientSecretCredential.getToken().block() will hang when parallelism is high](https://github.com/Azure/azure-sdk-for-java/issues/39676)
 - [[FAQ] My app uses Java's Security Manager and I have granted it all Permissions, yet it tells me it doesn't have the permission to do something](https://github.com/Azure/azure-sdk-for-java/wiki/Frequently-Asked-Questions#my-app-uses-javas-security-manager-and-i-have-granted-it-all-permissions-yet-it-tells-me-it-doesnt-have-the-permission-to-do-something)

Simplest solution for above issues is to use a dedicated `ExecutorService` for the `TokenCredential` e.g. `Executors.newCachedThreadPool()`:
```java readme-sample-azureIdentityThreadpool
// Use the singleton httpClient and a dedicated ExecutorService for Azure Identity
final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
final TokenCredential credential = new DefaultAzureCredentialBuilder()
    .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
    .executorService(Executors.newCachedThreadPool()) // use a dedicated `ExecutorService` for the `TokenCredential`
    .httpClient(singletonHttpClient) // use the singleton HttpClient
    .build();
```
Reference: 
* [Azure Identity](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity)
* [ForkJoinPool.commonPool()](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/ForkJoinPool.html#commonPool())

### Configure Azure Clients to use the singleton HttpClient and Azure Identity client:
```java readme-sample-azureClientHttpClient
// Use the singleton httpClient for your Azure client
AzureResourceManager azureResourceManager = AzureResourceManager
    .configure()
    .withLogLevel(HttpLogDetailLevel.BASIC)
    .withHttpClient(singletonHttpClient)
    .authenticate(credential, profile)
    .withSubscription(subscriptionId); // your subscription ID, can be different for different Azure clients
```
Reference: [Azure Resource Manager](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/resourcemanager#include-the-recommended-packages)

### Configure Reactor thread pool size:
The reactive stream will be subscribed on `Schedulers.parallel()` if:
* You are using [azure-core-http-netty](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core/azure-core-http-netty) as the `HttpClient` implementation, which this guide is
* You are calling long-running operations such as `create()`
* You are using operators such as `Flux::delayElements` on the stream that will introduce `Schedulers.parallel()`

`Schedulers.parallel()` is a static thread pool and is shared in nature. By default, the pool size is equal to available processor count.
You can change the pool size by specifying the environment variable `reactor.schedulers.defaultPoolSize`.

Reference: 
* [Replacing default Schedulers](https://projectreactor.io/docs/core/release/reference/#scheduler-factory)
* [Javadocs for Schedulers.parallel()](https://projectreactor.io/docs/core/release/api/reactor/core/scheduler/Schedulers.html#parallel--)
* [DEFAULT_POOL_SIZE for Schedulers.parallel()](https://github.com/reactor/reactor-core/blob/3.4.x/reactor-core/src/main/java/reactor/core/scheduler/Schedulers.java#L72-L81)

## Other JVM thread configurations you might be interested:
* [Compiler threads for JIT compiler](https://docs.oracle.com/en/java/javase/17/docs/specs/man/java.html#advanced-jit-compiler-options-for-java)
* [GC threads](https://docs.oracle.com/en/java/javase/17/docs/specs/man/java.html#advanced-garbage-collection-options-for-java)
