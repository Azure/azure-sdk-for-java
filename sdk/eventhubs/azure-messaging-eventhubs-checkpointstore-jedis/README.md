# Azure Event Hubs Checkpoint Store client library for Java using the Jedis Client Library for Redis

Azure Event Hubs Checkpoint Store can be used for storing checkpoints while processing events from Azure Event Hubs.
This package makes use of Redis as a persistent store for maintaining checkpoints and partition ownership information.
The `JedisRedisCheckpointStore` provided in this package can be plugged in to `EventProcessorClient`.

[Source code][source_code]| [API reference documentation][api_documentation] | [Product
documentation][event_hubs_product_docs] | [Samples][sample_examples]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
  - Here are details about [Java 8 client compatibility with Azure Certificate Authority](https://learn.microsoft.com/azure/security/fundamentals/azure-ca-details?tabs=root-and-subordinate-cas-list#client-compatibility-for-public-pkis).
- [Maven][maven]
- Microsoft Azure subscription
    - You can create a free account at: [https://azure.microsoft.com](https://azure.microsoft.com)
- Azure Event Hubs instance
    - Step-by-step guide for [creating an Event Hub using the Azure Portal][event_hubs_create]
- Azure Redis Cache or a suitable alternative Redis server
    - Step-by-step guide for [creating a Redis Cache using the Azure Portal][redis_quickstart]

### Include the package
#### Include the BOM file

Please include the azure-sdk-bom to your project to take dependency on the General Availability (GA) version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
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
and then include the direct dependency in the dependencies section without the version tag as shown below.

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventhubs-checkpointstore-jedis</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-messaging-eventhubs-checkpointstore-jedis;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventhubs-checkpointstore-jedis</artifactId>
    <version>1.0.0-beta.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authenticate the storage container client

In order to create an instance of `JedisCheckpointStore`, a `JedisPool` object must be created. To make this `JedisPool`
object, a hostname String and a primary key String are required. These can be used as shown below to create a
`JedisPool` object.

## Key concepts

Key concepts are explained in detail [here][key_concepts].

## Examples
- [Create and run an instance of JedisRedisCheckpointStore][sample_jedis_client]
- [Consume events from all Event Hub partitions][sample_event_processor]

### Create an instance of JedisPool

To create an instance of JedisPool using Azure Redis Cache, follow the instructions in
[Use Azure Cache for Redis in Java][redis_quickstart_java] to fetch the hostname and access key.  Otherwise, use
connection information from a running Redis instance.

```java readme-sample-createJedis
JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
    .password("<YOUR_REDIS_PRIMARY_ACCESS_KEY>")
    .ssl(true)
    .build();

String redisHostName = "<YOUR_REDIS_HOST_NAME>.redis.cache.windows.net";
HostAndPort hostAndPort = new HostAndPort(redisHostName, 6380);
JedisPool jedisPool = new JedisPool(hostAndPort, clientConfig);

// Do things with JedisPool.

// Finally, dispose of resource
jedisPool.close();
```

### Consume events using an Event Processor Client

To consume events for all partitions of an Event Hub, you'll create an
[`EventProcessorClient`][source_eventprocessorclient] for a specific consumer group. When an Event Hub is created, it
provides a default consumer group that can be used to get started.

The [`EventProcessorClient`][source_eventprocessorclient] will delegate processing of events to a callback function
that you provide, allowing you to focus on the logic needed to provide value while the processor holds responsibility
for managing the underlying consumer operations.

In our example, we will focus on building the [`EventProcessor`][source_eventprocessorclient], use the
[`JedisRedisCheckpointStore`][source_jedisredischeckpointstore], and a simple callback function to process the events
received from the Event Hubs, writes to console and updates the checkpoint in Blob storage after each event.

```java readme-sample-createCheckpointStore
JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
    .password("<YOUR_REDIS_PRIMARY_ACCESS_KEY>")
    .ssl(true)
    .build();

String redisHostName = "<YOUR_REDIS_HOST_NAME>.redis.cache.windows.net";
HostAndPort hostAndPort = new HostAndPort(redisHostName, 6380);
JedisPool jedisPool = new JedisPool(hostAndPort, clientConfig);

EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
    .consumerGroup("<< CONSUMER GROUP NAME >>")
    .connectionString("<< EVENT HUB NAMESPACE CONNECTION STRING >>")
    .eventHubName("<< EVENT HUB NAME >>")
    .checkpointStore(new JedisCheckpointStore(jedisPool))
    .processEvent(eventContext -> {
        System.out.println("Partition id = " + eventContext.getPartitionContext().getPartitionId() + " and "
            + "sequence number of event = " + eventContext.getEventData().getSequenceNumber());
    })
    .processError(context -> {
        System.out.println("Error occurred while processing events " + context.getThrowable().getMessage());
    })
    .buildEventProcessorClient();

// This will start the processor. It will start processing events from all partitions.
eventProcessorClient.start();

// (for demo purposes only - adding sleep to wait for receiving events)
// Your application will probably keep the eventProcessorClient alive until the program ends.
TimeUnit.SECONDS.sleep(2);

// When the user wishes to stop processing events, they can call `stop()`.
eventProcessorClient.stop();

// Dispose of JedisPool resource.
jedisPool.close();
```

## Troubleshooting

### Enable client logging

Azure SDK for Java offers a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

## Next steps

Get started by exploring the samples [here][samples_readme].

## Contributing

If you would like to become an active contributor to this project please refer to our [Contribution
Guidelines][guidelines] for more information.

<!-- Links -->
[api_documentation]: https://azure.github.io/azure-sdk-for-java
[event_hubs_create]: https://docs.microsoft.com/azure/event-hubs/event-hubs-create
[event_hubs_product_docs]: https://docs.microsoft.com/azure/event-hubs/
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[key_concepts]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/README.md#key-concepts
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[maven]: https://maven.apache.org/
[redis_quickstart]: https://learn.microsoft.com/azure/azure-cache-for-redis/quickstart-create-redis
[redis_quickstart_java]: https://learn.microsoft.com/azure/azure-cache-for-redis/cache-java-get-started
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-jedis
[sample_jedis_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-jedis/src/samples/java/com/azure/messaging/eventhubs/checkpointstore/jedis/JedisCheckpointStoreSample.java
[sample_event_processor]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-jedis/src/samples/java/com/azure/messaging/eventhubs/checkpointstore/jedis/EventProcessorClientJedisSample.java
[sample_examples]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-jedis/src/samples
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-jedis
[source_eventprocessorclient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/main/java/com/azure/messaging/eventhubs/EventProcessorClient.java
[source_jedisredischeckpointstore]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-jedis/src/main/java/com/azure/messaging/eventhubs/checkpointstore/jedis/JedisCheckpointStore.java
[guidelines]: https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md
