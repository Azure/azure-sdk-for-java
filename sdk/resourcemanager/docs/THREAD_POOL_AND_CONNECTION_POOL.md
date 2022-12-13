# Guidance on thread pool and connection pool configuration for Azure Client

There are four types of pools used by Azure client:
1. Http connection pool (used by HttpClient)
2. Http thread pool (used by HttpClient)
3. Azure Identity thread pool (used by Azure Identity client for token acquisition)
4. Reactor thread pool (default thread pool to subscribe on)

In the following guide, we'll break down on how to control the pool size to meet your need.

## Prerequisites
* Read [README.md](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/resourcemanager#getting-started) to get start with Azure client.
* Basic concepts of [Project Reactor](https://projectreactor.io/).

## Code Example
In the code example, you can find:
1. [How to configure connection pool size for the singleton HttpClient](#httpconnectionpool)
2. [How to configure thread pool size for the singleton HttpClient](#httpthreadpool)
3. [How to configure thread pool size and HttpClient used by the singleton Azure Identity client](#azureidentity)
4. [How to configure Azure client to use the singleton HttpClient and Azure Identity client to share a common thread pool and connection pool](#azureclient)
5. [How to configure reactor thread pool size](#reactor)

### <a name="httpconnectionpool"></a>Configure connection pool for the singleton HttpClient:
By default, HttpClient uses a “fixed” connection pool with 500 as the maximum number of active channels and 1000 as the maximum number of further channel acquisition attempts allowed to be kept in a pending state.
```java
NettyAsyncHttpClientBuilder singletonHttpClientBuilder = new NettyAsyncHttpClientBuilder();
singletonHttpClientBuilder 
    // Connection pool configuration.  
	// Official Reactor Netty documentation for defaults: https://projectreactor.io/docs/netty/release/reference/#_connection_pool_2  
	.connectionProvider(  
		 ConnectionProvider.builder("connection-pool")  
         // By default, HttpClient uses a “fixed” connection pool with 500 as the maximum number of active channels  
		 // and 1000 as the maximum number of further channel acquisition attempts allowed to be kept in a pending state.  
		.maxConnections(500)  
         // When the maximum number of channels in the pool is reached, up to specified new attempts to  
		 // acquire a channel are delayed (pending) until a channel is returned to the pool again, and further attempts are declined with an error.  
		.pendingAcquireMaxCount(1000)  
        .maxIdleTime(Duration.ofSeconds(20)) // Configures the maximum time for a connection to stay idle to 20 seconds.  
		.maxLifeTime(Duration.ofSeconds(60)) // Configures the maximum time for a connection to stay alive to 60 seconds.  
		.pendingAcquireTimeout(Duration.ofSeconds(60)) // Configures the maximum time for the pending acquire operation to 60 seconds.  
		.evictInBackground(Duration.ofSeconds(120)) // Every two minutes, the connection pool is regularly checked for connections that are applicable for removal.  
		.build())
```

### <a name="httpthreadpool"></a>Configure thread pool for the singleton HttpClient:
By default the TCP client uses an “Event Loop Group”, where the number of the worker threads equals the number of processors available to the runtime on initialization (but with a minimum value of 4). When you need a different configuration, you can use one of the LoopResource#create methods.
```java
// Thread pool configuration.  
// Official Reactor Netty documentation for defaults: https://projectreactor.io/docs/netty/release/reference/#client-tcp-level-configurations-event-loop-group  
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

### <a name="azureidentity"></a>Configure Azure Identity client to use the singleton HttpClient (DefaultAzureCredential for example):
By default, Azure Identity uses `ForkJoinPool.commonPool()` for token acquisition. The pool size equals the number of processors available to the runtime on initialization minus 1 (with a minimum of 1). It's a singleton pool and is shared by default. Default pool configuration should be sufficient for most use cases.
```java
HttpClient singletonHttpClient = singletonHttpClientBuilder.build();
// Use the singleton httpClient for Azure Identity
final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);  
final TokenCredential credential = new DefaultAzureCredentialBuilder()  
    .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())  
    .executorService(ForkJoinPool.commonPool()) // thread pool for executing token acquisition, usually we leave it default  
    .httpClient(singletonHttpClient)
  .build();
```

### <a name="azureclient"></a>Configure Azure Clients to use the singleton HttpClient and Azure Identity client:
```java
// Use the singleton httpClient for your Azure client
AzureResourceManager azureResourceManager = AzureResourceManager  
	.configure()  
    .withLogLevel(HttpLogDetailLevel.BASIC)  
    .withHttpClient(singletonHttpClient)
    .authenticate(credential, profile)  
    .withSubscription(subscriptionId); // your subscription ID, can be different for different Azure clients
```

### <a name="reactor"></a>Configure Reactor thread pool size:
By default, Azure Client subscribes on Schedulers.parallel(), which has a thread pool of size equal to available processor count. It is a static pool and is shared in nature.
You can change the pool size by specifying the variable `reactor.schedulers.defaultPoolSize` in environment variables, whether through command line arguments, or System.setProperty.
```java
System.setProperty("reactor.schedulers.defaultPoolSize", NettyRuntime.availableProcessors() + "");
```