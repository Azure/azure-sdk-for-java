# Azure Event Hubs client library for Java

Azure Event Hubs is a highly scalable publish-subscribe service that can ingest millions of events per second and stream
them to multiple consumers. This lets you process and analyze the massive amounts of data produced by your connected
devices and applications. Once Event Hubs has collected the data, you can retrieve, transform, and store it by using any
real-time analytics provider or with batching/storage adapters. If you would like to know more about Azure Event Hubs,
you may wish to review: [What is Event Hubs](https://docs.microsoft.com/azure/event-hubs/event-hubs-about)?

The Azure Event Hubs client library allows for publishing and consuming of Azure Event Hubs events and may be used to:

- Emit telemetry about your application for business intelligence and diagnostic purposes.
- Publish facts about the state of your application which interested parties may observe and use as a trigger for taking
  action.
- Observe interesting operations and interactions happening within your business or other ecosystem, allowing loosely
  coupled systems to interact without the need to bind them together.
- Receive events from one or more publishers, transform them to better meet the needs of your ecosystem, then publish
  the transformed events to a new stream for consumers to observe.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product
documentation][event_hubs_product_docs] | [Samples][sample_examples] | [Troubleshooting][troubleshooting]

## Table of contents

- [Getting started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Include the package](#include-the-package)
    - [Include the BOM file](#include-the-bom-file)
    - [Include direct dependency](#include-direct-dependency)
  - [Authenticate the client](#authenticate-the-client)
    - [Create an Event Hub producer using a connection string](#create-an-event-hub-producer-using-a-connection-string)
    - [Create an Event Hub client using Microsoft identity platform (formerly Azure Active Directory)](#create-an-event-hub-client-using-microsoft-identity-platform-formerly-azure-active-directory)
    - [Authorizing with DefaultAzureCredential](#authorizing-with-defaultazurecredential)
- [Key concepts](#key-concepts)
- [Examples](#examples)
  - [Publish events to an Event Hub](#publish-events-to-an-event-hub)
    - [Create an Event Hub producer and publish events](#create-an-event-hub-producer-and-publish-events)
    - [Publish events using partition identifier](#publish-events-using-partition-identifier)
    - [Publish events using partition key](#publish-events-using-partition-key)
  - [Consume events from an Event Hub partition](#consume-events-from-an-event-hub-partition)
    - [Consume events with EventHubConsumerAsyncClient](#consume-events-with-eventhubconsumerasyncclient)
    - [Consume events with EventHubConsumerClient](#consume-events-with-eventhubconsumerclient)
  - [Consume events using an EventProcessorClient](#consume-events-using-an-eventprocessorclient)
- [Troubleshooting][troubleshooting]
- [Next steps](#next-steps)
- [Contributing](#contributing)

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Maven][maven]
- Microsoft Azure subscription
  - You can create a free account at: [https://azure.microsoft.com](https://azure.microsoft.com)
- Azure Event Hubs instance
  - Step-by-step guide for [creating an Event Hub using the Azure Portal][event_hubs_create]

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
    <artifactId>azure-messaging-eventhubs</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-messaging-eventhubs;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventhubs</artifactId>
    <version>5.15.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authenticate the client

For the Event Hubs client library to interact with an Event Hub, it will need to understand how to connect and authorize
with it.

#### Create an Event Hub producer using a connection string

The easiest means for doing so is to use a connection string, which is created automatically when creating an Event Hubs
namespace. If you aren't familiar with shared access policies in Azure, you may wish to follow the step-by-step guide to
[get an Event Hubs connection string][event_hubs_connection_string].

Both the asynchronous and synchronous Event Hub producer and consumer clients can be created using
`EventHubClientBuilder`. Invoking `build*Client()` creates a synchronous producer or consumer while
`build*AsyncClient()` creates its asynchronous counterpart.

The snippet below creates a synchronous Event Hub producer.

```java readme-sample-createSynchronousEventHubProducer
String connectionString = "<< CONNECTION STRING FOR THE EVENT HUBS NAMESPACE >>";
String eventHubName = "<< NAME OF THE EVENT HUB >>";
EventHubProducerClient producer = new EventHubClientBuilder()
    .connectionString(connectionString, eventHubName)
    .buildProducerClient();
```

#### Create an Event Hub client using Microsoft identity platform (formerly Azure Active Directory)

Azure SDK for Java supports an Azure Identity package, making it easy to get credentials from Microsoft identity
platform. First, add the package:

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.8.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

All the implemented ways to request a credential can be found under the `com.azure.identity.credential` package. The
sample below shows how to use an Azure Active Directory (AAD) application client secret to authorize with Azure Event
Hubs.

#### Authorizing with DefaultAzureCredential

Authorization is easiest using [DefaultAzureCredential][wiki_identity]. It finds the best credential to use in its
running environment. For more information about using Azure Active Directory authorization with Event Hubs, please refer
to [the associated documentation][aad_authorization].

```java readme-sample-useAadAuthorization
TokenCredential credential = new DefaultAzureCredentialBuilder()
    .build();

// The fully qualified namespace for the Event Hubs instance. This is likely to be similar to:
// {your-namespace}.servicebus.windows.net
String fullyQualifiedNamespace = "my-test-eventhubs.servicebus.windows.net";
String eventHubName = "<< NAME OF THE EVENT HUB >>";
EventHubProducerClient client = new EventHubClientBuilder()
    .credential(fullyQualifiedNamespace, eventHubName, credential)
    .buildProducerClient();
```

## Key concepts

- An **Event Hub producer** is a source of telemetry data, diagnostics information, usage logs, or other log data, as
  part of an embedded device solution, a mobile device application, a game title running on a console or other device,
  some client or server based business solution, or a website.

- An **Event Hub consumer** picks up such information from the Event Hub and processes it. Processing may involve
  aggregation, complex computation, and filtering. Processing may also involve distribution or storage of the
  information in a raw or transformed fashion. Event Hub consumers are often robust and high-scale platform
  infrastructure parts with built-in analytics capabilities, like Azure Stream Analytics, Apache Spark, or Apache Storm.

- A **partition** is an ordered sequence of events that is held in an Event Hub. Azure Event Hubs provides message
  streaming through a partitioned consumer pattern in which each consumer only reads a specific subset, or partition, of
  the message stream. As newer events arrive, they are added to the end of this sequence. The number of partitions is
  specified at the time an Event Hub is created and cannot be changed.

- A **consumer group** is a view of an entire Event Hub. Consumer groups enable multiple consuming applications to each
  have a separate view of the event stream, and to read the stream independently at their own pace and from their own
  position. There can be at most 5 concurrent readers on a partition per consumer group; however it is recommended that
  there is only one active consumer for a given partition and consumer group pairing. Each active reader receives
  the events from its partition; if there are multiple readers on the same partition, then they will receive duplicate
  events.

For more concepts and deeper discussion, see: [Event Hubs Features][event_hubs_features]. Also, the concepts for AMQP
are well documented in [OASIS Advanced Messaging Queuing Protocol (AMQP) Version 1.0][oasis_amqp_v1].

## Examples

### Publish events to an Event Hub

To publish events, you'll need to create an asynchronous [`EventHubProducerAsyncClient`][EventHubProducerAsyncClient] or
a synchronous [`EventHubProducerClient`][EventHubProducerClient]. Each producer can send events to either, a specific
partition, or allow the Event Hubs service to decide which partition events should be published to. It is recommended to
use automatic routing when the publishing of events needs to be highly available or when event data should be
distributed evenly among the partitions.

#### Create an Event Hub producer and publish events

Developers can create a producer using `EventHubClientBuilder` and calling `buildProducer*Client()`. Specifying
`CreateBatchOptions.setPartitionId(String)` will send events to a specific partition. If `partitionId` is not specified,
events are automatically routed to a partition. Specifying `CreateBatchOptions.setPartitionKey(String)` will tell Event
Hubs service to hash the events and send them to the same partition.

The snippet below creates a synchronous producer and sends events to any partition, allowing Event Hubs service to route
the event to an available partition.

```java readme-sample-publishEvents
EventHubProducerClient producer = new EventHubClientBuilder()
    .connectionString("<< CONNECTION STRING FOR SPECIFIC EVENT HUB INSTANCE >>")
    .buildProducerClient();

List<EventData> allEvents = Arrays.asList(new EventData("Foo"), new EventData("Bar"));
EventDataBatch eventDataBatch = producer.createBatch();

for (EventData eventData : allEvents) {
    if (!eventDataBatch.tryAdd(eventData)) {
        producer.send(eventDataBatch);
        eventDataBatch = producer.createBatch();

        // Try to add that event that couldn't fit before.
        if (!eventDataBatch.tryAdd(eventData)) {
            throw new IllegalArgumentException("Event is too large for an empty batch. Max size: "
                + eventDataBatch.getMaxSizeInBytes());
        }
    }
}
// send the last batch of remaining events
if (eventDataBatch.getCount() > 0) {
    producer.send(eventDataBatch);
}
```
Note that `EventDataBatch.tryAdd(EventData)` is not thread-safe. Please make sure to synchronize the method access
when using multiple threads to add events.

#### Publish events using partition identifier

Many Event Hub operations take place within the scope of a specific partition. Any client can call
`getPartitionIds()` or `getEventHubProperties()` to get the partition ids and metadata about in their Event Hub
instance.

```java readme-sample-publishEventsToPartition
EventHubProducerClient producer = new EventHubClientBuilder()
    .connectionString("<< CONNECTION STRING FOR SPECIFIC EVENT HUB INSTANCE >>")
    .buildProducerClient();

CreateBatchOptions options = new CreateBatchOptions().setPartitionId("0");
EventDataBatch batch = producer.createBatch(options);

// Add events to batch and when you want to send the batch, send it using the producer.
producer.send(batch);
```

#### Publish events using partition key

When a set of events are not associated with any specific partition, it may be desirable to request that the Event
Hubs service keep different events or batches of events together on the same partition. This can be accomplished by
setting a `partition key` when publishing the events.

```java readme-sample-publishEventsWithPartitionKey
EventHubProducerClient producer = new EventHubClientBuilder()
    .connectionString("<< CONNECTION STRING FOR SPECIFIC EVENT HUB INSTANCE >>")
    .buildProducerClient();

CreateBatchOptions batchOptions = new CreateBatchOptions().setPartitionKey("grouping-key");
EventDataBatch eventDataBatch = producer.createBatch(batchOptions);

// Add events to batch and when you want to send the batch, send it using the producer.
producer.send(eventDataBatch);
```

### Consume events from an Event Hub partition

To consume events, create an [`EventHubConsumerAsyncClient`][EventHubConsumerAsyncClient] or
[`EventHubConsumerClient`][EventHubConsumerClient] for a specific consumer group. In addition, a consumer needs to
specify where in the event stream to begin receiving events.

#### Consume events with EventHubConsumerAsyncClient

In the snippet below, we create an asynchronous consumer that receives events from `partitionId` and only listens
to the newest events that get pushed to the partition. Developers can begin receiving events from multiple partitions using
the same `EventHubConsumerAsyncClient` by calling `receiveFromPartition(String, EventPosition)` with another partition
id.

```java readme-sample-consumeEventsFromPartition
EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
    .connectionString("<< CONNECTION STRING FOR SPECIFIC EVENT HUB INSTANCE >>")
    .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
    .buildAsyncConsumerClient();

// Receive newly added events from partition with id "0". EventPosition specifies the position
// within the Event Hub partition to begin consuming events.
consumer.receiveFromPartition("0", EventPosition.latest()).subscribe(event -> {
    // Process each event as it arrives.
});
// add sleep or System.in.read() to receive events before exiting the process.
```

#### Consume events with EventHubConsumerClient

Developers can create a synchronous consumer that returns events in batches using an `EventHubConsumerClient`. In the
snippet below, a consumer is created that starts reading events from the beginning of the partition's event stream.

```java readme-sample-consumeEventsFromPartitionUsingSyncClient
EventHubConsumerClient consumer = new EventHubClientBuilder()
    .connectionString("<< CONNECTION STRING FOR SPECIFIC EVENT HUB INSTANCE >>")
    .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
    .buildConsumerClient();

String partitionId = "<< EVENT HUB PARTITION ID >>";

// Get the first 15 events in the stream, or as many events as can be received within 40 seconds.
IterableStream<PartitionEvent> events = consumer.receiveFromPartition(partitionId, 15,
    EventPosition.earliest(), Duration.ofSeconds(40));
for (PartitionEvent event : events) {
    System.out.println("Event: " + event.getData().getBodyAsString());
}
```

### Consume events using an EventProcessorClient

To consume events for all partitions of an Event Hub, you can create an [`EventProcessorClient`][EventProcessorClient]
for a specific consumer group.

The [`EventProcessorClient`][EventProcessorClient] will delegate processing of events to a callback function that you
provide, allowing you to focus on the logic needed to provide value while the processor holds responsibility for
managing the underlying consumer operations.

In our example, we will focus on building the [`EventProcessorClient`][EventProcessorClient], use the
[`SampleCheckpointStore`][SampleCheckpointStore] available in samples, and a callback function that processes events
received from the Event Hub and writes to console. For production applications, it's recommended to use a durable
store like [Checkpoint Store with Azure Storage Blobs][BlobCheckpointStore].

```java readme-sample-consumeEventsUsingEventProcessor
EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
    .consumerGroup("<< CONSUMER GROUP NAME >>")
    .connectionString("<< EVENT HUB CONNECTION STRING >>")
    .checkpointStore(new SampleCheckpointStore())
    .processEvent(eventContext -> {
        System.out.println("Partition id = " + eventContext.getPartitionContext().getPartitionId() + " and "
            + "sequence number of event = " + eventContext.getEventData().getSequenceNumber());
    })
    .processError(errorContext -> {
        System.out
            .println("Error occurred while processing events " + errorContext.getThrowable().getMessage());
    })
    .buildEventProcessorClient();

// This will start the processor. It will start processing events from all partitions.
eventProcessorClient.start();

// (for demo purposes only - adding sleep to wait for receiving events)
TimeUnit.SECONDS.sleep(2);

// This will stop processing events.
eventProcessorClient.stop();
```

## Troubleshooting

See [TROUBLESHOOTING.md][troubleshooting].

## Next steps

Beyond those discussed, the Azure Event Hubs client library offers support for many other scenarios to take
advantage of the full feature set of the Azure Event Hubs service. To explore some of these scenarios, check out the
[samples README][samples_readme].

## Contributing

If you would like to become an active contributor to this project please refer to our [Contribution
Guidelines](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/CONTRIBUTING.md) for more information.

<!-- Links -->
[aad_authorization]: https://docs.microsoft.com/azure/event-hubs/authorize-access-azure-active-directory
[amqp_transport_error]: https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#type-amqp-error
[AmqpErrorCondition]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-amqp/src/main/java/com/azure/core/amqp/exception/AmqpErrorCondition.java
[AmqpErrorContext]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-amqp/src/main/java/com/azure/core/amqp/exception/AmqpErrorContext.java
[AmqpException]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-amqp/src/main/java/com/azure/core/amqp/exception/AmqpException.java
[AmqpRetryOptions]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-amqp/src/main/java/com/azure/core/amqp/AmqpRetryOptions.java
[api_documentation]: https://aka.ms/java-docs
[app_registration_page]: https://docs.microsoft.com/azure/active-directory/develop/howto-create-service-principal-portal#get-values-for-signing-in
[application_client_secret]: https://docs.microsoft.com/azure/active-directory/develop/howto-create-service-principal-portal#create-a-new-application-secret
[BlobCheckpointStore]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/README.md
[CreateBatchOptions]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/main/java/com/azure/messaging/eventhubs/models/CreateBatchOptions.java
[event_hubs_connection_string]: https://docs.microsoft.com/azure/event-hubs/event-hubs-get-connection-string
[event_hubs_create]: https://docs.microsoft.com/azure/event-hubs/event-hubs-create
[event_hubs_features]: https://docs.microsoft.com/azure/event-hubs/event-hubs-features
[event_hubs_messaging_exceptions]: https://docs.microsoft.com/azure/event-hubs/event-hubs-messaging-exceptions
[event_hubs_product_docs]: https://docs.microsoft.com/azure/event-hubs/
[event_hubs_quotas]: https://docs.microsoft.com/azure/event-hubs/event-hubs-quotas
[EventHubConsumerAsyncClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/main/java/com/azure/messaging/eventhubs/EventHubConsumerAsyncClient.java
[EventHubConsumerClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/main/java/com/azure/messaging/eventhubs/EventHubConsumerClient.java
[EventHubProducerAsyncClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/main/java/com/azure/messaging/eventhubs/EventHubProducerAsyncClient.java
[EventHubProducerClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/main/java/com/azure/messaging/eventhubs/EventHubProducerClient.java
[EventProcessorClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/main/java/com/azure/messaging/eventhubs/EventProcessorClient.java
[SampleCheckpointStore]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/samples/java/com/azure/messaging/eventhubs/SampleCheckpointStore.java
[java_8_sdk_javadocs]: https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[logging]: https://docs.microsoft.com/azure/developer/java/sdk/logging-overview
[maven]: https://maven.apache.org/
[oasis_amqp_v1_error]: https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#type-error
[oasis_amqp_v1]: https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-overview-v1.0-os.html
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[qpid_proton_j_apache]: https://qpid.apache.org/proton/
[sample_examples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/samples/java/com/azure/messaging/eventhubs/
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/samples/README.md
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/
[wiki_identity]: https://github.com/Azure/azure-sdk-for-java/wiki/Identity-and-Authentication
[troubleshooting]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/TROUBLESHOOTING.md

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Feventhubs%2Fazure-messaging-eventhubs%2FREADME.png)
