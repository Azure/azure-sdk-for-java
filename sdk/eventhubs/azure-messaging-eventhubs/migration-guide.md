# Migration Guide (Event Hubs v3 to v5)

This document is intended for users that are familiar with v3 of the Java SDK for Event Hubs library
([`azure-eventhubs`][azure-eventhubs] and [`azure-eventhubs-eph`][azure-eventhubs-eph]) and wish to migrate their
application to V5 of the same library.

For users new to the Java SDK for Event Hubs, please see the [README for azure-messaging-eventhubs][README].

## Table of contents

- [Prerequisites](#prerequisites)
- [Updated Maven dependencies](#updated-maven-dependencies)
- [General changes](#general-changes)
  - [Converting core classes](#converting-core-classes)
  - [Sending events](#sending-events)
  - [Receiving events](#receiving-events)
  - [Minor renames](#minor-renames)
- [Migration samples](#migration-samples)
  - [Migrating from `PartitionSender` or `EventHubClient` to `EventHubProducerAsyncClient` for sending events](#migrating-from-partitionsender-or-eventhubclient-to-eventhubproducerasyncclient-for-sending-events)
  - [Migrating code from `PartitionReceiver` to `EventHubConsumerClient` for receiving events](#migrating-code-from-partitionreceiver-to-eventhubconsumerclient-for-receiving-events)
  - [Migrating code from `EventProcessorHost` to `EventHubConsumerClient` for receiving events](#migrating-code-from-eventprocessorhost-to-eventhubconsumerclient-for-receiving-events)
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

  <!-- Contains Azure Blob Storage checkpoint store when using EventProcessorClient -->
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventhubs-checkpointstore-blob</artifactId>
    <version>1.0.0-beta.4</version>
  </dependency>
</dependencies>
```

## General changes

In the interest of simplifying the API surface, we've made three clients, each with an asynchronous and synchronous
variant. One client for sending events, `EventHubProducerAsyncClient`, and two for receiving events.
`EventProcessorClient` is the production level consumer and `EventHubConsumerAsyncClient` for exploration and
lower-level control of `EventData` consumption.

[EventProcessorClient][EventProcessorClient] supports checkpointing and load balancing through a plugin model.
Currently, only Azure Blob storage is supported through
[azure-messaging-eventhubs-checkpointstore-blob][azure-messaging-eventhubs-checkpointstore-blob], but support for other
durable storage (i.e. Cosmos DB, Redis) can be added in the future.

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
| `EventProcessorHost.EventProcessorHostBuilder.newBuilder()` | `new EventProcessorClientBuilder().buildEventProcessorClient()` | [EventProcessorClient with Blob storage][EventProcessorClientInstantiation] |

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

* [Sending events](#migrating-from-partitionsender-or-eventhubclient-to-eventhubproducerasyncclient-for-sending-events)
* [Receiving events](#migrating-code-from-partitionreceiver-to-eventhubconsumerclient-for-receiving-events)
* [Receiving events with checkpointing](#migrating-code-from-eventprocessorhost-to-eventhubconsumerclient-for-receiving-events)

### Migrating from `PartitionSender` or `EventHubClient` to `EventHubProducerAsyncClient` for sending events
In v3, there were multiple options on how to publish events to an Event Hub.

In v5, this has been consolidated into a more efficient `send(EventDataBatch)` method. Batching merges information from
multiple events into a single sent message, reducing the amount of network communication needed vs sending events one at
a time.

The code below assumes all events fit into a single batch. For a more complete example, see sample: [Publishing events
to specific partition][PublishEventsToSpecificPartition].

So in v3:
```java
EventHubClient client = EventHubClient.createFromConnectionString("connection-string-for-an-event-hub",
    Executors.newScheduledThreadPool(4)).get();

List<EventData> events = Arrays.asList(EventData.create("foo".getBytes()), EventData.create("bar".getBytes()));

CompletableFuture<Void> sendFuture = client.createPartitionSender("my-partition-id")
    .thenCompose(sender -> {
        EventDataBatch batch = sender.createBatch();
        for (EventData event : events) {
            try {
                // Assuming all events fit into a single batch. This returns false if it does not.
                // If that is the case, we'll send the full batch then create another one to continue adding events to.
                batch.tryAdd(event);
            } catch (PayloadSizeExceededException e) {
                System.err.println("Event is too large for batch. Exception: " + e);
            }
        }

        return sender.send(batch);
    });
sendFuture.get();
```

In v5:
```java
List<EventData> events = Arrays.asList(EventData.create("foo".getBytes()), EventData.create("bar".getBytes()));

EventHubProducerAsyncClient producer = new EventHubClientBuilder()
    .connectionString("connection-string-for-an-event-hub")
    .buildAsyncProducerClient();

CreateBatchOptions options = new CreateBatchOptions()
    .setPartitionId("my-partition-id");

Mono<Void> sendOperation = producer.createBatch(options).flatMap(batch -> {
    for (EventData event : data) {
        // Assuming all events fit into a single batch. This returns false if it does not.
        // If that is the case, we'll send the full batch then create another one to continue adding events to.
        batch.tryAdd(event);
    }
    return producer.send(batch);
});

sendOperation.block();
```

### Migrating code from `PartitionReceiver` to `EventHubConsumerClient` for receiving events

In V2, event handlers were passed as positional arguments to `receive`.

In V5, event handlers are passed as part of a `SubscriptionEventHandlers` shaped object.

For example, this code which receives from a partition in V2:

```typescript
const client = EventHubClient.createFromConnectionString(connectionString);
const rcvHandler = client.receive(partitionId, onMessageHandler, onErrorHandler, {
  eventPosition: EventPosition.fromStart(),
  consumerGroup: consumerGroupName
});
await rcvHandler.stop();
```

Becomes this in V5:

```typescript
const eventHubConsumerClient = new EventHubConsumerClient(consumerGroupName, connectionString);

const subscription = eventHubConsumerClient.subscribe(
  partitionId, {
 processInitialize: (initContext) => {
 initContext.setStartingPosition(EventPosition.fromStart());
 },
 processEvents: onMessageHandler,
 processError: onErrorHandler
});

await subscription.close();
```

See [`receiveEvents.ts`](https://github.com/Azure/azure-sdk-for-js/blob/master/sdk/eventhub/event-hubs/samples/receiveEvents.ts)
for a sample program demonstrating this.

In V5, this has been consolidated into a more efficient `sendBatch` method.
Batching merges information from multiple messages into a single send, reducing
the amount of network communication needed vs sending messages one at a time.

So in V2:
```java
const eventsToSend = [
  // events go here
];

const client = EventHubClient.createFromConnectionString(connectionString);

// Would fail if the total size of events exceed the max size supported by the library.
await client.sendBatch(eventsToSend, partitionId);
```

In V5:
```typescript
const producer = new EventHubProducerClient(connectionString);

const eventsToSend = [
  // events go here
];

let batch = await producer.createBatch();
let i = 0;

while (i < eventsToSend.length) {
  // messages can fail to be added to the batch if they exceed the maximum size configured for
  // the EventHub.
  const isAdded = batch.tryAdd(eventsToSend[i]);

  if (isAdded) {
 console.log(`Added event number ${i} to the batch`);
 ++i;
 continue;
  }

  if (batch.count === 0) {
 // If we can't add it and the batch is empty that means the message we're trying to send
 // is too large, even when it would be the _only_ message in the batch.
 //
 // At this point you'll need to decide if you're okay with skipping this message entirely
 // or find some way to shrink it.
 console.log(`Message was too large and can't be sent until it's made smaller. Skipping...`);
 ++i;
 continue;
  }

  // otherwise this just signals a good spot to send our batch
  console.log(`Batch is full - sending ${batch.count} messages as a single batch.`);
  await producer.sendBatch(batch);

  // and create a new one to house the next set of messages
  batch = await producer.createBatch();
}

// send any remaining messages, if any.
if (batch.count > 0) {
  console.log(`Sending remaining ${batch.count} messages as a single batch.`)
  await producer.sendBatch(batch);
}
```

### Migrating code from `EventProcessorHost` to `EventHubConsumerClient` for receiving events

In V2, `EventProcessorHost` allowed you to balance the load between multiple instances of
your program when receiving events.

In V5, `EventHubConsumerClient` allows you to do the same with the `subscribe()` method if you
pass a `CheckpointStore` to the constructor.

So in V2:
```typescript
const eph = EventProcessorHost.createFromConnectionString(
  EventProcessorHost.createHostName(ephName),
  storageConnectionString,
  storageContainerName,
  ehConnectionString,
  {
 eventHubPath: eventHubName,
 onEphError: (error) => {
 // This is your error handler for errors occuring during load balancing.
 console.log("Error when running EPH: %O", error);
 }
  }
);

// In V2, you get a single event passed to your callback. If you had asynchronous code running in your callback,
// it is not awaited before the callback is called for the next event.
const onMessage = (context, event) => { /** your code here **/ }

// This is your error handler for errors occurring when receiving events.
const onError = (error) => {
  console.log("Received Error: %O", error);
};

await eph.start(onMessage, onError);
```

And in V5:
```typescript
import { EventHubConsumerClient, CheckpointStore } from "@azure/event-hubs";
import { ContainerClient } from "@azure/storage-blob";
import { BlobCheckpointStore } from "@azure/eventhubs-checkpointstore-blob";

const containerClient = new ContainerClient(storageConnectionString, storageContainerName);
const checkpointStore : CheckpointStore = new BlobCheckpointStore(containerClient);
const eventHubConsumerClient = new EventHubConsumerClient(consumerGroupName, ehConnectionString, eventHubName);

const subscription = eventHubConsumerClient.subscribe(
  partitionId, {
 // In V5 we deliver events in batches, rather than a single message at a time.
 // You can control the batch size via the options passed to the client.
 //
 // If your callback is an async function or returns a promise, it will be awaited before the
 // callback is called for the next batch of events.
 processEvents: (events, context) => { /** your code here **/ },

 // Prior to V5 errors were handled by separate callbacks depending
 // on where they were thrown i.e when managing different partitions vs receiving from each partition.
 //
 // In V5 you only need a single error handler for all of those cases.
 processError: (error, context) => {
 if (context.partitionId) {
 console.log("Error when receiving events from partition %s: %O", context.partitionId, error)
 } else {
 console.log("Error from the consumer client: %O", error);
 }
 }
});

await subscription.close();
```

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
[PublishEventsToSpecificPartition]: src/samples/java/com/azure/messaging/eventhubs/PublishEventsToSpecificPartition.java
[PublishEventsWithAzureIdentity]: src/samples/java/com/azure/messaging/eventhubs/PublishEventsWithAzureIdentity.java
[PublishEventsWithCustomMetadata]: src/samples/java/com/azure/messaging/eventhubs/PublishEventsWithCustomMetadata.java
[README]: README.md
