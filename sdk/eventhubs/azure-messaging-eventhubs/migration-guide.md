# Guide for migrating to `azure-messaging-eventhubs` from `azure-eventhubs` and `azure-eventhubs-eph`

This guide assists in the migration to `azure-messaging-eventhubs` from `azure-eventhubs` and
`azure-eventhubs-eph`. It will focus on side-by-side comparisons for similar operations between the two packages.

Familiarity with `azure-eventhubs` and `azure-eventhubs-eph` package is assumed. For those new to the Event Hubs client
library for Java, please refer to the [README.md][README] rather than this guide.

## Table of contents

- [Guide for migrating to `azure-messaging-eventhubs` from `azure-eventhubs` and `azure-eventhubs-eph`](#guide-for-migrating-to-azure-messaging-eventhubs-from-azure-eventhubs-and-azure-eventhubs-eph)
  - [Table of contents](#table-of-contents)
  - [Migration benefits](#migration-benefits)
    - [Cross Service SDK improvements](#cross-service-sdk-improvements)
  - [Important changes](#important-changes)
    - [Group id, artifact id, and package names](#group-id-artifact-id-and-package-names)
    - [Client hierarchy](#client-hierarchy)
    - [Send events to an Event Hub](#send-events-to-an-event-hub)
    - [Receiving events from all partitions](#receiving-events-from-all-partitions)
    - [Receive events from a single partition](#receive-events-from-a-single-partition)
  - [Additional samples](#additional-samples)

## Migration benefits

A natural question to ask when considering whether or not to adopt a new version or library is its benefits. As Azure has matured and been embraced by a more diverse group of developers, we have been focused on learning the patterns and practices to best support developer productivity and to understand the gaps that the Java
client libraries have.

There were several areas of consistent feedback expressed across the Azure client library ecosystem. The most important is that the client libraries for different Azure services have not had a consistent organization, naming, and API structure. Additionally, many developers have felt that the learning curve was difficult, and the APIs did not offer a good, approachable, and consistent onboarding story for those learning Azure or exploring a specific Azure service.

To improve the development experience across Azure services, a set of uniform [design guidelines][Guidelines] was created for all languages to drive a
consistent experience with established API patterns for all services. A set of [Java design guidelines][GuidelinesJava] was introduced to ensure that Java clients have a natural and idiomatic feel with respect to the Java ecosystem. Further details are available in the guidelines
for those interested.

### Cross Service SDK improvements

The modern Event Hubs client library also provides the ability to share in some of the cross-service improvements made to the Azure development experience, such as

- Using the new azure-identity library to share a single authentication approach between clients.
- A unified logging and diagnostics pipeline offering a common view of the activities across each of the client libraries.
- A unified asynchronous programming model using [Project Reactor][project-reactor].
- A unified method of creating clients via client builders to interact with Azure services.

## Important changes

### Group id, artifact id, and package names

Group ids, artifact ids, and package names for the modern Azure client libraries for Java have changed. They follow the [Java SDK naming guidelines][GuidelinesJavaDesign]. Each will have the group id `com.azure`, an artifact id following the pattern `azure-[area]-[service]`, and the root package name `com.azure.[area].[Service]`. The legacy clients have a group id `com.microsoft.azure` and their package names followed the pattern `com.microsoft. Azure.[service]`. This provides a quick and accessible means to help understand, at a glance, whether you are using modern or legacy clients.

In Event Hubs, the modern client libraries have packages and namespaces that begin with `com.azure.messaging.eventhubs` and were released starting with version 5. The legacy client libraries have package names starting with `com.microsoft.azure.eventhubs` and a version of 3.x.x or below.

### Client hierarchy

In the interest of simplifying the API surface, we've made separate clients for sending and receiving events:

- [EventHubProducerClient][EventHubProducerClient] is the sync client to send events. [EventHubProducerAsyncClient][EventHubProducerAsyncClient] is the corresponding async client.
- [EventProcessorClient][EventProcessorClient] is what you would typically use to read events from all partitions with an optional, but recommended checkpointing feature.
- [EventHubConsumerClient][EventHubConsumerClient] is the sync client you would use for lower-level control of [EventData][EventData] consumption from a single partition. [EventHubConsumerAsyncClient][EventHubConsumerAsyncClient] is the corresponding async client.

#### Instantiating clients

In v3, the `EventHubClient` was instantiated via static overloads. The client contained both sync and async methods. A `PartitionReceiver` or `PartitionSender` could be created from this client. Reading events from all partitions was done via the `EventProcessorHost`.

In v5, the creation of producer and consumer clients is done through the [EventHubClientBuilder][EventHubClientBuilder]. Reading events from all partitions is done through [EventProcessorClient][EventProcessorClient]. The processor is created via [EventProcessorClientBuilder][EventProcessorClientBuilder].

In v3:

```java
public static void main(String[] args) throws Exception {
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    CompletableFuture<EventHubClient> clientFuture =
        EventHubClient.createFromConnectionString("connection-string-for-an-event-hub",
        scheduler);

    CompletableFuture<Void> workFuture = clientFuture.thenComposeAsync(client -> {
        // Do things with the client.
        // Dispose of client.
        return client.close();
    });

    workFuture.get();
    scheduler.shutdown();
  }
```

In v5:

```java
public static void main(String[] args) throws Exception {
    // This builds an async client. The sync version can be created by
    // calling buildProducerClient() to create an EventHubProducerClient.
    EventHubProducerAsyncClient client = new EventHubClientBuilder()
        .connectionString("connection-string-for-an-event-hub")
        .buildAsyncProducerClient();

    // Do things with the client.
    // Dispose of client.
    client.close();
  }
```

### Send events to an Event Hub

In v3, events could be published to a specific partition using the `send` or `sendSync` methods on the `PartitionSender` or automatically routed to an available partition via similar methods on the `EventHubClient`. You could either send a single event, a list of events or an event batch.

In v5, the option to send single events is dropped to encourage sending events in batches for better throughput.
You can send a list of events or create and send an event batch as before. Instead of using different classes, sending to a specific partition and making use of the automatic routing is done using overloads of the same methods. For example:

- Passing a list of events to the `send` method with no options makes use of automatic routing. If `partitionId` is passed in the options to this method, the events are sent to the given partition.
- Creating a batch of events using the `createBatch` method without setting the `partitionId` in the options makes use of automatic routing. If `partitionId` is passed in the options to this method, the events are sent to the given partition.

Batching merges information from multiple events into a single sent message, reducing the amount of network communication needed vs sending events one at a time.

The code below assumes all events fit into a single batch. For a more complete example, see sample: [Publishing events to specific partition][PublishEventsToSpecificPartition].

So in v3:

```java
List<EventData> events = Arrays.asList(EventData.create("foo".getBytes()));

ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
EventHubClient client = EventHubClient.createFromConnectionStringSync(
    "connection-string-for-an-event-hub", scheduler);

// Creating a batch where all messages to "my-partition-id" is done by creating a PartitionSender.
CompletableFuture<PartitionSender> partitionSender = client.createPartitionSender("my-partition-id");
CompletableFuture<Void> partitionBatchFuture = partitionSender.thenCompose(producer -> {
    EventDataBatch batch = producer.createBatch();
    // Assuming all events fit into a single batch. This returns false if it does not.
    // If it returns false, we send the batch, create another, and continue to add events.
    for (EventData event : events) {
        try {
            batch.tryAdd(event);
        } catch (PayloadSizeExceededException e) {
            System.err.println("Event is larger than maximum allowed size. Exception: " + e);
        }
    }

    return producer.send(batch);
});

// We block here to turn this future into a synchronous operation.
partitionBatchFuture.get();

// Creating a batch where messages are automatically routed is done via EventHubClient.createBatch().
EventDataBatch batch = client.createBatch();
for (EventData event : events) {
    try {
        batch.tryAdd(event);
    } catch (PayloadSizeExceededException e) {
        System.err.println("Event is larger than maximum allowed size. Exception: " + e);
    }
}

client.sendSync(batch);

// Dispose of any resources.
partitionSender.thenComposeAsync(producer -> producer.close()).get();
client.close();
scheduler.shutdown();
```

In v5:

```java
List<EventData> events = Arrays.asList(EventData.create("foo".getBytes()));

EventHubProducerAsyncClient producer = new EventHubClientBuilder()
    .connectionString("connection-string-for-an-event-hub")
    .buildAsyncProducerClient();

// Creating a batch where all messages to "my-partition-id" by setting CreateBatchOptions.
CreateBatchOptions options = new CreateBatchOptions()
    .setPartitionId("my-partition-id");
Mono<Void> partitionBatchOperation = producer.createBatch(options).flatMap(batch -> {
    for (EventData event : events) {
        // Assuming all events fit into a single batch. This returns false if it does not.
        // If it returns false, we send the batch, create another, and continue to add events.
        try {
            batch.tryAdd(event);
        } catch (AmqpException e) {
            System.err.println("Event is larger than maximum allowed size. Exception: " + e);
        }
    }
    return producer.send(batch);
});

// The send operation is an async operation that does not execute unless it is subscribed to.
// We block here to turn this into a synchronous operation.
partitionBatchOperation.block();

// Creating a batch where messages are automatically routed is done using createBatch()
// or with default CreateBatchOptions.
Mono<Void> sendOperation = producer.createBatch(new CreateBatchOptions()).flatMap(batch -> {
    for (EventData event : events) {
        try {
            batch.tryAdd(event);
        } catch (AmqpException e) {
            System.err.println("Event is larger than maximum allowed size. Exception: " + e);
        }
    }
    return producer.send(batch);
});

sendOperation.block();

// Dispose of any resources.
producer.close();
```

### Receiving events from all partitions

In v3, `EventProcessorHost` allowed you to balance the load between multiple instances of your program and checkpoint events when receiving. Developers would have to create and register a concrete implementation of `IEventProcessor` to begin consuming events.

In v5, [EventProcessorClient][EventProcessorClient] allows you to do the same and includes a plugin model, so other durable stores can be used if desired. The development model is made simpler by registering functions that would be invoked for each event. To use Azure Storage Blobs for checkpointing, include
[azure-messaging-eventhubs-checkpointstore-blob][azure-messaging-eventhubs-checkpointstore-blob] as a dependency.

Other durable stores are supported by implementing [CheckpointStore][CheckpointStore] and passing an instance of it when creating the processor using [EventProcessorClientBuilder.checkpointStore(CheckpointStore)][EventProcessorClientBuilder-CheckpointStore]

The following code in v3:

```java
private static void main(String[] args) throws Exception {
    EventProcessorHost processor = EventProcessorHost.EventProcessorHostBuilder
            .newBuilder("a-processor-name", "my-consumer-group")
            .useAzureStorageCheckpointLeaseManager("storage-connection-string", "storage-container-name", "prefix")
            .useEventHubConnectionString("connection-string-for-an-event-hub")
            .build();

    processor.registerEventProcessor(MyEventProcessor.class).get();

    // When you are finished processing events.
    processor.unregisterEventProcessor();
}

public class MyEventProcessor implements IEventProcessor {
    @Override
    public void onOpen(PartitionContext context) {
        System.out.println("Started receiving on partition: " + context.getPartitionId());
    }

    @Override
    public void onClose(PartitionContext context, CloseReason reason)  {
        System.out.printf("Stopped receiving on partition: %s. Reason: %s%n", context.getPartitionId(), reason);
    }

    @Override
    public void onEvents(PartitionContext context, Iterable<EventData> events) {
        System.out.println("Received events from partition: " + context.getPartitionId());
        for (EventData event : events) {
            System.out.println("Sequence number: " + event.getSystemProperties().getSequenceNumber());
            System.out.println("Contents: " + new String(event.getBytes(), StandardCharsets.UTF_8));
        }
    }

    @Override
    public void onError(PartitionContext context, Throwable error) {
        System.err.printf("Error occurred on partition: %s. Error: %s%n", context.getPartitionId(), error);
    }
}
```

And in v5, implementing `MyEventProcessor` is not necessary. The callbacks are invoked for each respective event that occurs on an owned partition.

```java
private static void main(String[] args) {
    BlobContainerAsyncClient blobClient = new BlobContainerClientBuilder()
            .connectionString("storage-connection-string")
            .containerName("storage-container-name")
            .buildAsyncClient();

    EventProcessorClient processor = new EventProcessorClientBuilder()
            .connectionString("connection-string-for-an-event-hub")
            .consumerGroup("my-consumer-group")
            .checkpointStore(new BlobCheckpointStore(blobClient))
            .processEvent(eventContext -> onEvent(eventContext))
            .processError(context -> {
                System.err.printf("Error occurred on partition: %s. Error: %s%n",
                        context.getPartitionContext().getPartitionId(), context.getThrowable());
            })
            .processPartitionInitialization(initializationContext -> {
                System.out.printf("Started receiving on partition: %s%n",
                        initializationContext.getPartitionContext().getPartitionId());
            })
            .processPartitionClose(closeContext -> {
                System.out.printf("Stopped receiving on partition: %s. Reason: %s%n",
                        closeContext.getPartitionContext().getPartitionId(),
                        closeContext.getCloseReason());
            })
            .buildEventProcessorClient();

    processor.start();

    // When you are finished processing events.
    processor.stop();
}

private static void onEvent(EventContext eventContext) {
    PartitionContext partition = eventContext.getPartitionContext();
    System.out.println("Received events from partition: " + partition.getPartitionId());

    EventData event = eventContext.getEventData();
    System.out.println("Sequence number: " + event.getSequenceNumber());
    System.out.println("Contents: " + new String(event.getBody(), StandardCharsets.UTF_8));
}
```

### Receive events from a single partition

In v3, events were received by creating a `PartitionReceiver` and setting the `PartitionReceiveHandler` in
`setReceiveHandler()`.

In v5, [project Reactor][project-reactor] is used, so events can be streamed as they come in. Project Reactor offers
many reactive transformations that can be applied to the stream of events.

This code which receives from a partition in v3:

```java
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
EventHubClient client = EventHubClient.createFromConnectionStringSync(
    "connection-string-for-an-event-hub", scheduler);
PartitionReceiver consumer = client.createReceiverSync("my-consumer-group", "my-partition-id",
    EventPosition.fromStartOfStream());

consumer.setReceiveHandler(new PartitionReceiveHandler() {
    @Override
    public int getMaxEventCount() {
        return 100;
    }

    @Override
    public void onReceive(Iterable<EventData> events) {
        for (EventData event : events) {
            System.out.println("Sequence number: " + event.getSystemProperties().getSequenceNumber());
            System.out.println("Contents: " + new String(event.getBytes(), StandardCharsets.UTF_8));
        }
    }

    @Override
    public void onError(Throwable error) {
        System.err.println("Error while receiving messages: " + error);
    }
});

System.out.println("Enter any key to stop.");
System.in.read();

// Setting to null will stop the partition pump.
consumer.setReceiveHandler(null);

// Dispose of any resources.
consumer.closeSync();
client.close();
scheduler.shutdown();
```

Becomes this in v5:

```java
EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
        .connectionString("connection-string-for-an-event-hub")
        .consumerGroup("my-consumer-group")
        .buildAsyncConsumerClient();

// This is a non-blocking call. It'll subscribe and return a Disposable. This will stream events as they come
// in, starting from the beginning of the partition.
Disposable subscription = consumer.receiveFromPartition("my-partition-id", EventPosition.earliest())
        .subscribe(partitionEvent -> {
            EventData event = partitionEvent.getData();
            System.out.println("Sequence number: " + event.getSequenceNumber());
            System.out.println("Contents: " + new String(event.getBody(), StandardCharsets.UTF_8));
        });

// Keep fetching events
// When you are finished, dispose of the subscription.
subscription.dispose();

// Dispose of any resources.
consumer.close();
```

#### Checkpoints migration

In order to align with the goal of supporting cross-language checkpoints and a more efficient means of tracking
partition ownership, V5 Event Processor Client does not consider or apply checkpoints created with the legacy Event Processor Host family of types. To migrate the checkpoints created by the V3 Event Processor Host, the new Event Processor Client provides an option to do a one-time initialization of checkpoints as shown in the sample below.

```java
private static void main(String[] args) {
    BlobContainerAsyncClient blobClient = new BlobContainerClientBuilder()
            .connectionString("storage-connection-string")
            .containerName("storage-container-name")
            .buildAsyncClient();

    // Get the legacy checkpoint offsets and convert them into a map of partitionId and EventPosition
    Map<String, EventPosition> initialPartitionEventPosition = getLegacyPartitionOffsetMap()
            .entrySet()
            .stream()
            .map(partitionOffsetEntry -> new AbstractMap.SimpleEntry<>(partitionOffsetEntry.getKey(),
                EventPosition.fromOffset(partitionOffsetEntry.getValue())))
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

    // Set the initial partition event positions in EventProcessorClientBuilder
    EventProcessorClient processor = new EventProcessorClientBuilder()
            .connectionString("connection-string-for-an-event-hub")
            .consumerGroup("my-consumer-group")
            .checkpointStore(new BlobCheckpointStore(blobClient))
            .initialPartitionEventPosition(initialPartitionEventPosition)
            .processEvent(eventContext -> onEvent(eventContext))
            .processError(context -> {
                System.err.printf("Error occurred on partition: %s. Error: %s%n",
                        context.getPartitionContext().getPartitionId(), context.getThrowable());
            })
            .processPartitionInitialization(initializationContext -> {
                System.out.printf("Started receiving on partition: %s%n",
                        initializationContext.getPartitionContext().getPartitionId());
            })
            .processPartitionClose(closeContext -> {
                System.out.printf("Stopped receiving on partition: %s. Reason: %s%n",
                        closeContext.getPartitionContext().getPartitionId(),
                        closeContext.getCloseReason());
            })
            .buildEventProcessorClient();

    processor.start();

    // When you are finished processing events.
    processor.stop();
}

private static Map<String, Long> getLegacyPartitionOffsetMap() {
    // read the offsets of legacy checkpoint for each partition from blob storage and
    // return a map of partitionId-offset
}

private static void onEvent(EventContext eventContext) {
    PartitionContext partition = eventContext.getPartitionContext();
    System.out.println("Received events from partition: " + partition.getPartitionId());

    EventData event = eventContext.getEventData();
    System.out.println("Sequence number: " + event.getSequenceNumber());
    System.out.println("Contents: " + new String(event.getBody(), StandardCharsets.UTF_8));
}
```

## Additional samples

More examples can be found at:

- [Event Hubs samples][README-Samples]
- [Event Hubs Azure Storage checkpoint store samples][README-Samples-Blobs]

<!-- Links -->
[azure-messaging-eventhubs-checkpointstore-blob]: https://search.maven.org/artifact/com.azure/azure-messaging-eventhubs-checkpointstore-blob
[CheckpointStore]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-messaging-eventhubs/latest/com/azure/messaging/eventhubs/CheckpointStore.html
[CreateBatchOptions]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-messaging-eventhubs/latest/com/azure/messaging/eventhubs/models/CreateBatchOptions.html
[EventData]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-messaging-eventhubs/latest/com/azure/messaging/eventhubs/EventData.html
[EventDataBatch]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-messaging-eventhubs/latest/com/azure/messaging/eventhubs/EventDataBatch.html
[EventHubClientBuilder]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-messaging-eventhubs/latest/com/azure/messaging/eventhubs/EventHubClientBuilder.html
[EventHubConsumerAsyncClient]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-messaging-eventhubs/latest/com/azure/messaging/eventhubs/EventHubConsumerAsyncClient.html
[EventHubConsumerClient]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-messaging-eventhubs/latest/com/azure/messaging/eventhubs/EventHubConsumerClient.html
[EventHubProducerAsyncClient]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-messaging-eventhubs/latest/com/azure/messaging/eventhubs/EventHubProducerAsyncClient.html
[EventHubProducerClient]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-messaging-eventhubs/latest/com/azure/messaging/eventhubs/EventHubProducerClient.html
[EventProcessorClient]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-messaging-eventhubs/latest/com/azure/messaging/eventhubs/EventProcessorClient.html
[EventProcessorClientBuilder]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-messaging-eventhubs/latest/com/azure/messaging/eventhubs/EventProcessorClientBuilder.html
[EventProcessorClientBuilder-CheckpointStore]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-messaging-eventhubs/latest/com/azure/messaging/eventhubs/EventProcessorClientBuilder.html#checkpointStore-com.azure.messaging.eventhubs.CheckpointStore-
[Guidelines]: https://azure.github.io/azure-sdk/general_introduction.html
[GuidelinesJava]: https://azure.github.io/azure-sdk/java_introduction.html
[GuidelinesJavaDesign]: https://azure.github.io/azure-sdk/java_introduction.html#namespaces
[project-reactor]: https://projectreactor.io/
[PublishEventsToSpecificPartition]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/samples/java/com/azure/messaging/eventhubs/PublishEventsToSpecificPartition.java
[README-Samples-Blobs]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/src/samples/README.md
[README-Samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/samples/README.md
[README]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/README.md

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Feventhubs%2Fazure-messaging-eventhubs%2Fmigration-guide.png)
