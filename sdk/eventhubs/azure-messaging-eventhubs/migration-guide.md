# Migration Guide (Event Hubs v3 to v5)

This document is intended for users that are familiar with v3 of the Java SDK for Event Hubs library
([`azure-eventhubs`][azure-eventhubs] and [`azure-eventhubs-eph`][azure-eventhubs-eph]) and wish to migrate their
application to V5 of the same library.

For users new to the Java SDK for Event Hubs, please see the [README for azure-messaging-eventhubs][README].

## Table of contents

- [Table of contents](#table-of-contents)
- [Prerequisites](#prerequisites)
- [Updated Maven dependencies](#updated-maven-dependencies)
- [General changes](#general-changes)
  - [Converting core classes](#converting-core-classes)
  - [Sending events](#sending-events)
  - [Receiving events](#receiving-events)
  - [Minor renames](#minor-renames)
- [Migration samples](#migration-samples)
  - [Migrating code from `PartitionSender` to `EventHubProducerAsyncClient` for sending events to a partition](#migrating-code-from-partitionsender-to-eventhubproducerasyncclient-for-sending-events-to-a-partition)
  - [Migrating code from `EventHubClient` to `EventHubProducerAsyncClient` for sending events using automatic routing](#migrating-code-from-eventhubclient-to-eventhubproducerasyncclient-for-sending-events-using-automatic-routing)
  - [Migrating code from `EventHubClient` to `EventHubProducerAsyncClient` for sending events with partition key](#migrating-code-from-eventhubclient-to-eventhubproducerasyncclient-for-sending-events-with-partition-key)
  - [Migrating code from `PartitionReceiver` to `EventHubConsumerAsyncClient` for receiving events in batches](#migrating-code-from-partitionreceiver-to-eventhubconsumerasyncclient-for-receiving-events-in-batches)
  - [Migrating code from `EventProcessorHost` to `EventProcessorClient` for receiving events](#migrating-code-from-eventprocessorhost-to-eventprocessorclient-for-receiving-events)
- [Additional samples](#additional-samples)

## Prerequisites
Java Development Kit (JDK) with version 8 or above

## Updated Maven dependencies

Dependencies for Event Hubs has been updated to:
```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventhubs</artifactId>
    <version>5.0.0-beta.6</version>
  </dependency>

  <!-- Contains Azure Storage Blobs checkpoint store when using EventProcessorClient -->
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventhubs-checkpointstore-blob</artifactId>
    <version>1.0.0-beta.4</version>
  </dependency>
</dependencies>
```

## General changes

In the interest of simplifying the API surface, we've made three clients, each with an asynchronous and synchronous
variant. One client is for producing events, `EventHubProducerAsyncClient`, while two are intended for reading events.
`EventProcessorClient` is the production-level consumer and `EventHubConsumerAsyncClient` for exploration and
lower-level control of `EventData` consumption.

[EventProcessorClient][EventProcessorClient] supports checkpointing and load balancing using a plugin model.
Currently, only Azure Storage Blobs is supported through
[azure-messaging-eventhubs-checkpointstore-blob][azure-messaging-eventhubs-checkpointstore-blob], but support for other
durable storage (i.e. Cosmos DB, Redis) may be added in the future.

| Operation | Asynchronous client | Synchronous client |
|---|---|---|
| Producing events | [EventHubProducerAsyncClient][EventHubProducerAsyncClient] | [EventHubProducerClient][EventHubProducerClient] |
| Consuming events (supports checkpointing and load balancing) | [EventProcessorClient][EventProcessorClient] | |
| Consuming events | [EventHubConsumerAsyncClient][EventHubConsumerAsyncClient] | [EventHubConsumerClient][EventHubConsumerClient] |

### Converting core classes

Creation of producers or consumers is done through either [EventHubClientBuilder][EventHubClientBuilder] or
[EventProcessorClientBuilder][EventProcessorClientBuilder]. Asynchronous clients are created by invoking
`builder.build*AsyncClient()`. Synchronous clients are created by invoking `builder.build*Client()`.

| In v3 | Equivalent in v5 | Sample |
|---|---|---|
| `EventHubClient.createFromConnectionString()` | `var builder = new EventHubClientBuilder().connectionString();`<br/>then either `builder.buildProducerAsyncClient();` or<br/>`builder.consumerGroup().buildConsumerAsyncClient();` | [Publishing events][PublishEventsWithCustomMetadata], [Consuming events][ConsumeEvents] |
| `EventHubClient.createWithAzureActiveDirectory()` | `var builder = new EventHubClientBuilder().tokenCredential();`<br/>then either `builder.buildProducerAsyncClient();` or<br/>`builder.consumerGroup().buildConsumerAsyncClient();` | [Publishing events with Azure AD][PublishEventsWithAzureIdentity] |
| `EventProcessorHost.EventProcessorHostBuilder`<br/>`.newBuilder()` | `new EventProcessorClientBuilder().buildEventProcessorClient()` | [EventProcessorClient with Azure Storage Blobs][EventProcessorClientInstantiation] |

### Sending events

`EventHubProducerAsyncClient` and `EventHubProducerClient` can publish events to a single partition or allow the service
to load balance events between all the partitions. The behaviour is determined when using
[`CreateBatchOptions`][CreateBatchOptions] in `producer.createBatch(CreateBatchOptions)`.

| In v3 | Equivalent in v5 | Sample |
|---|---|---|
| `PartitionSender.send(...)` | `EventHubProducerAsyncClient.send()` | [Publishing events to a specific partition][PublishEventsToSpecificPartition] |
| `EventHubClient.send(...)` | `EventHubProducerAsyncClient.send()` | [Publishing events][PublishEventsWithCustomMetadata] |

### Receiving events

| In v3 | Equivalent in v5 | Sample |
|---|---|---|
| `PartitionReceiver.receive()` | `EventHubConsumerAsyncClient.receiveFromPartition()` | [Consuming events][ConsumeEvents] |
| `PartitionReceiver.setReceiveHandler()` | `EventHubConsumerAsyncClient.receiveFromPartition()` | [Consuming events][ConsumeEvents] |

### Minor renames

| In v3 | Equivalent in v5 |
|---|---|
| `EventPosition.fromStartOfStream()` | `EventPosition.earliest()` |
| `EventPosition.fromEndOfStream()` | `EventPosition.latest()` |

## Migration samples

- [Migrating code from `PartitionSender` to `EventHubProducerAsyncClient` for sending events to a partition](#migrating-code-from-partitionsender-to-eventhubproducerasyncclient-for-sending-events-to-a-partition)
- [Migrating code from `EventHubClient` to `EventHubProducerAsyncClient` for sending events using automatic routing](#migrating-code-from-eventhubclient-to-eventhubproducerasyncclient-for-sending-events-using-automatic-routing)
- [Migrating code from `EventHubClient` to `EventHubProducerAsyncClient` for sending events with partition key](#migrating-code-from-eventhubclient-to-eventhubproducerasyncclient-for-sending-events-with-partition-key)
- [Migrating code from `PartitionReceiver` to `EventHubConsumerAsyncClient` for receiving events in batches](#migrating-code-from-partitionreceiver-to-eventhubconsumerasyncclient-for-receiving-events-in-batches)
- [Migrating code from `EventProcessorHost` to `EventProcessorClient` for receiving events](#migrating-code-from-eventprocessorhost-to-eventprocessorclient-for-receiving-events)

### Migrating code from `PartitionSender` to `EventHubProducerAsyncClient` for sending events to a partition

In v3, events could be published to a single partition using `PartitionSender`.

In v5, this has been consolidated into a more efficient `send(EventDataBatch)` method. Batching merges information from
multiple events into a single sent message, reducing the amount of network communication needed vs sending events one at
a time. Events are published to a specific partition [`CreateBatchOptions.setPartitionId()`][CreateBatchOptions] is set
before calling `createBatch(CreateBatchOptions)`.

The code below assumes all events fit into a single batch. For a more complete example, see sample: [Publishing events
to specific partition][PublishEventsToSpecificPartition].

So in v3:
```java
EventHubClient client = EventHubClient.createFromConnectionStringSync(
    "connection-string-for-an-event-hub", Executors.newScheduledThreadPool(4));
List<EventData> events = Arrays.asList(EventData.create("foo".getBytes()));

CompletableFuture<Void> sendFuture = client.createPartitionSender("my-partition-id")
    .thenCompose(producer -> {
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

sendFuture.get();
```

In v5:
```java
List<EventData> events = Arrays.asList(EventData.create("foo".getBytes()));

EventHubProducerAsyncClient producer = new EventHubClientBuilder()
    .connectionString("connection-string-for-an-event-hub")
    .buildAsyncProducerClient();

CreateBatchOptions options = new CreateBatchOptions()
    .setPartitionId("my-partition-id");

Mono<Void> sendOperation = producer.createBatch(options).flatMap(batch -> {
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

sendOperation.block();
```

### Migrating code from `EventHubClient` to `EventHubProducerAsyncClient` for sending events using automatic routing

In v3, events could be published to an Event Hub that allowed the service to automatically route events to an available partition.

In v5, this has been consolidated into a more efficient `send(EventDataBatch)` method. Batching merges information from
multiple events into a single sent message, reducing the amount of network communication needed vs sending events one at
a time. Automatic routing occurs when an `EventDataBatch` is created using `createBatch()`.

So in v3:
```java
EventHubClient client = EventHubClient.createFromConnectionStringSync(
    "connection-string-for-an-event-hub", Executors.newScheduledThreadPool(4));
List<EventData> events = Arrays.asList(EventData.create("foo".getBytes()));

EventDataBatch batch = client.createBatch();
for (EventData event : events) {
    // Assuming all events fit into a single batch. This returns false if it does not.
    // If it returns false, we send the batch, create another, and continue to add events.
    try {
        batch.tryAdd(event);
    } catch (PayloadSizeExceededException e) {
        System.err.println("Event is larger than maximum allowed size. Exception: " + e);
    }
}

client.send(batch).get();
```

In v5:
```java
List<EventData> events = Arrays.asList(EventData.create("foo".getBytes()));

EventHubProducerAsyncClient producer = new EventHubClientBuilder()
    .connectionString("connection-string-for-an-event-hub")
    .buildAsyncProducerClient();

Mono<Void> sendOperation = producer.createBatch().flatMap(batch -> {
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

sendOperation.block();
```

### Migrating code from `EventHubClient` to `EventHubProducerAsyncClient` for sending events with partition key

In v3, events could be published with a partition key.

In v5, this has been consolidated into a more efficient `send(EventDataBatch)` method. Batching merges information from
multiple events into a single sent message, reducing the amount of network communication needed vs sending events one at
a time. Events are published with a partition key when [`CreateBatchOptions.setPartitionKey()`][CreateBatchOptions] is
set before calling `createBatch(CreateBatchOptions)`.

So in v3:
```java
EventHubClient client = EventHubClient.createFromConnectionStringSync(
    "connection-string-for-an-event-hub",
    Executors.newScheduledThreadPool(5));

BatchOptions batchOptions = new BatchOptions().with(options -> options.partitionKey = "a-key");
EventDataBatch batch = client.createBatch(batchOptions);

// Fill batch with events then send it.
client.send(batch).get();
```

In v5:
```java
EventHubProducerAsyncClient producer = new EventHubClientBuilder()
    .connectionString("connection-string-for-an-event-hub")
    .buildAsyncProducerClient();

CreateBatchOptions options = new CreateBatchOptions()
    .setPartitionKey("a-key");
EventDataBatch batch = producer.createBatch(options).block();

// Fill batch with events then send it.
producer.send(batch).block();
```

### Migrating code from `PartitionReceiver` to `EventHubConsumerAsyncClient` for receiving events in batches

In v3, events were received by creating a `PartitionReceiver` and invoking `receive(int)` multiple times to receive
events up to a certain number.

In v5, [project Reactor][project-reactor] is used, so events can be streamed as they come in without having to use a
batched receive approach.

This code which receives from a partition in v3:

```java
EventHubClient client = EventHubClient.createFromConnectionStringSync("connection-string-for-an-event-hub",
    Executors.newScheduledThreadPool(5));
PartitionReceiver consumer = client.createReceiverSync("my-consumer-group", "my-partition-id",
    EventPosition.fromStartOfStream());

// Gets 100 events or until the receive timeout elapses.
consumer.receive(100).thenAccept(events -> {
    for (EventData event : events) {
        System.out.println("Sequence number: " + event.getSystemProperties().getSequenceNumber());
        System.out.println("Contents: " + new String(event.getBytes(), StandardCharsets.UTF_8));
    }
}).get();

// Gets the next 50 events or until the receive timeout elapses.
consumer.receive(50).thenAccept(events -> {
    for (EventData event : events) {
        System.out.println("Sequence number: " + event.getSystemProperties().getSequenceNumber());
        System.out.println("Contents: " + new String(event.getBytes(), StandardCharsets.UTF_8));
    }
}).get();
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
```

See [`ConsumeEvents.java`][ConsumeEvents] for a sample program demonstrating this.

### Migrating code from `EventProcessorHost` to `EventProcessorClient` for receiving events

In v3, `EventProcessorHost` allowed you to balance the load between multiple instances of your program and checkpoint
events when receiving. Developers would have to create and register a concrete implementation of `IEventProcessor` to
begin consuming events.

In v5, `EventProcessorClient` allows you to do the same and includes a plugin model, so other durable stores can be used
if desired. The development model is made simpler by registering functions that would be invoked for each event. To use
Azure Storage Blobs for checkpointing, include
[azure-messaging-eventhubs-checkpointstore-blob][azure-messaging-eventhubs-checkpointstore-blob] as a dependency.

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

And in v5, implementing `MyEventProcessor` is not necessary. The callbacks are invoked for each respective event that
occurs on an owned partition.

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
#### V3 Checkpoints
In order to align with the goal of supporting cross-language checkpoints and a more efficient means of tracking 
partition ownership, V5 Event Processor Client does not consider or apply checkpoints created with the legacy Event
Processor Host family of types. To migrate the checkpoints created by the V3 Event Processor Host, the new Event 
Processor Client provides an option to do a one-time initialization of checkpoints as shown in the sample below.

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
- [Event Hubs samples](../azure-messaging-eventhubs/src/samples/README.md)
- [Event Hubs Azure Storage checkpoint store samples](../azure-messaging-eventhubs-checkpointstore-blob/src/samples/README.md)

<!-- Links -->
[azure-eventhubs-eph]: https://search.maven.org/artifact/com.microsoft.azure/azure-eventhubs-eph
[azure-eventhubs]: https://search.maven.org/artifact/com.microsoft.azure/azure-eventhubs
[azure-messaging-eventhubs-checkpointstore-blob]: https://search.maven.org/artifact/com.azure/azure-messaging-eventhubs-checkpointstore-blob
[ConsumeEvents]: src/samples/java/com/azure/messaging/eventhubs/ConsumeEvents.java
[CreateBatchOptions]: src/main/java/com/azure/messaging/eventhubs/models/CreateBatchOptions.java
[EventHubClientBuilder]: src/main/java/com/azure/messaging/eventhubs/EventHubClientBuilder.java
[EventHubConsumerAsyncClient]: src/main/java/com/azure/messaging/eventhubs/EventHubConsumerAsyncClient.java
[EventHubConsumerClient]: src/main/java/com/azure/messaging/eventhubs/EventHubConsumerClient.java
[EventHubProducerAsyncClient]: src/main/java/com/azure/messaging/eventhubs/EventHubProducerAsyncClient.java
[EventHubProducerClient]: src/main/java/com/azure/messaging/eventhubs/EventHubProducerClient.java
[EventProcessorClient]: src/main/java/com/azure/messaging/eventhubs/EventProcessorClient.java
[EventProcessorClientBuilder]: src/main/java/com/azure/messaging/eventhubs/EventProcessorClientBuilder.java
[EventProcessorClientInstantiation]: ../azure-messaging-eventhubs-checkpointstore-blob/src/samples/java/com/azure/messaging/eventhubs/checkpointstore/blob/BlobCheckpointStoreSample.java
[project-reactor]: https://projectreactor.io/
[PublishEventsToSpecificPartition]: src/samples/java/com/azure/messaging/eventhubs/PublishEventsToSpecificPartition.java
[PublishEventsWithAzureIdentity]: src/samples/java/com/azure/messaging/eventhubs/PublishEventsWithAzureIdentity.java
[PublishEventsWithCustomMetadata]: src/samples/java/com/azure/messaging/eventhubs/PublishEventsWithCustomMetadata.java
[README]: README.md

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Feventhubs%2Fazure-messaging-eventhubs%2Fmigration-guide.png)
