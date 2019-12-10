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
documentation][event_hubs_product_docs] | [Samples][sample_examples]

## Table of contents

- [Table of contents](#table-of-contents)
- [Getting started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Adding the package to your product](#adding-the-package-to-your-product)
  - [Default SSL library](#default-ssl-library)
  - [Methods to authorize with Event Hubs](#methods-to-authorize-with-event-hubs)
  - [Create an Event Hub producer using a connection string](#create-an-event-hub-producer-using-a-connection-string)
  - [Create an Event Hub client using Microsoft identity platform (formerly Azure Active Directory)](#create-an-event-hub-client-using-microsoft-identity-platform-formerly-azure-active-directory)
- [Key concepts](#key-concepts)
- [Examples](#examples)
  - [Publish events to an Event Hub](#publish-events-to-an-event-hub)
  - [Consume events from an Event Hub partition](#consume-events-from-an-event-hub-partition)
  - [Consume events using an EventProcessorClient](#consume-events-using-an-eventprocessorclient)
- [Troubleshooting](#troubleshooting)
  - [Enable client logging](#enable-client-logging)
  - [Enable AMQP transport logging](#enable-amqp-transport-logging)
  - [Common exceptions](#common-exceptions)
  - [Other exceptions](#other-exceptions)
- [Next steps](#next-steps)
- [Contributing](#contributing)

## Getting started

### Prerequisites

- Java Development Kit (JDK) with version 8 or above
- [Maven][maven]
- Microsoft Azure subscription
  - You can create a free account at: https://azure.microsoft.com
- Azure Event Hubs instance
  - Step-by-step guide for [creating an Event Hub using the Azure Portal][event_hubs_create]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-messaging-eventhubs;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventhubs</artifactId>
    <version>5.0.0-preview.5</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level
performance for SSL operations. The Boring SSL library is an uber jar containing native libraries
for Linux/macOS/Windows, and provides better performance compared to the default SSL
implementation within the JDK. For more information, including how to reduce the dependency size,
refer to the [performance tuning][performance_tuning] section of the wiki.

### Methods to authorize with Event Hubs

For the Event Hubs client library to interact with an Event Hub, it will need to understand how to connect and authorize
with it.

### Create an Event Hub producer using a connection string

The easiest means for doing so is to use a connection string, which is created automatically when creating an Event Hubs
namespace. If you aren't familiar with shared access policies in Azure, you may wish to follow the step-by-step guide to
[get an Event Hubs connection string][event_hubs_connection_string].

Both the asynchronous and synchronous Event Hub producer and consumer clients can be created using
`EventHubClientBuilder`. Invoking `buildAsyncProducerClient()` or `buildProducerClient()` will build the asynchronous or
synchronous producers. Similarly, `buildAsyncConsumerClient()` or `buildConsumerClient()` will build the appropriate
consumers.

The snippet below creates a synchronous Event Hub producer.

```java
String connectionString = "<< CONNECTION STRING FOR THE EVENT HUBS NAMESPACE >>";
String eventHubName = "<< NAME OF THE EVENT HUB >>";
EventHubProducerClient producer = new EventHubClientBuilder()
    .connectionString(connectionString, eventHubName)
    .buildProducerClient();
```

### Create an Event Hub client using Microsoft identity platform (formerly Azure Active Directory)

Azure SDK for Java supports an Azure Identity package, making it simple get credentials from Microsoft identity
platform. First, add the package:

[//]: # ({x-version-update-start;com.azure:azure-identity;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

All the implemented ways to request a credential can be found under the `com.azure.identity.credential` package. The
sample below shows how to use an Azure Active Directory (AAD) application client secret to authorize with Azure Event
Hubs.

#### Authorizing with AAD application client secret

Follow the instructions in [Creating a service principal using Azure Portal][application_client_secret] to create a
service principal and a client secret. The corresponding `clientId` and `tenantId` for the service principal can be
obtained from the [App registration page][app_registration_page].

```java
ClientSecretCredential credential = new ClientSecretCredentialBuilder()
    .clientId("<< APPLICATION (CLIENT) ID >>")
    .clientSecret("<< APPLICATION SECRET >>")
    .tenantId("<< DIRECTORY (TENANT) ID >>")
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
  some client or server based business solution, or a web site.

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
  there is only one active consumer for a given partition and consumer group pairing. Each active reader receives all of
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

Developers can create a producer by calling `buildAsyncProducerClient()` or `buildProducerClient()`. If
`buildProducerClient()` is invoked, a synchronous `EventHubProducerClient` is created. If `buildAsyncProducerClient()`
is used, an asynchronous `EventHubProducerAsyncClient` is returned.

Specifying `CreateBatchOptions.setPartitionId(String)` will send events to a specific partition. If not specified, will
allow for automatic partition routing. In addition, specifying `CreateBatchOptions.setPartitionKey(String)` will tell
Event Hubs service to hash the events and send them to the same partition.

The snippet below creates a synchronous producer and sends events to any partition, allowing Event Hubs service to route
the event to an available partition.

```java
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

To send events to a particular partition, set the optional parameter `setPartitionId(String)` on
[`CreateBatchOptions`][CreateBatchOptions].

#### Publish events using partition identifier

Many Event Hub operations take place within the scope of a specific partition. Because partitions are owned by the Event
Hub, their names are assigned at the time of creation. To understand what partitions are available, you can use the
`getPartitionIds()` function to get the ids of all available partitions in your Event Hub instance. All clients created
using `EventHubsClientBuilder` can query for metadata about the Event Hub using `getPartitionIds()` or
`getEventHubProperties()`.

```java
EventHubProducerClient producer = new EventHubClientBuilder()
    .connectionString("<< CONNECTION STRING FOR SPECIFIC EVENT HUB INSTANCE >>")
    .buildProducerClient();

CreateBatchOptions options = new CreateBatchOptions().setPartitionId("0");
EventDataBatch batch = producer.createBatch(options);

// Add events to batch and when you want to send the batch, send it using the producer.
producer.send(batch);
```

#### Publish events using partition key

When an Event Hub producer is not associated with any specific partition, it may be desirable to request that the Event
Hubs service keep different events or batches of events together on the same partition. This can be accomplished by
setting a `partition key` when publishing the events.

```java
CreateBatchOptions batchOptions = new CreateBatchOptions().setPartitionKey("grouping-key");
EventDataBatch eventDataBatch = producer.createBatch(batchOptions);

// Add events to batch and when you want to send the batch, send it using the producer.
producer.send(eventDataBatch);
```

### Consume events from an Event Hub partition

In order to consume events, you'll need to create an [`EventHubConsumerAsyncClient`][EventHubConsumerAsyncClient] or
[`EventHubConsumerClient`][EventHubConsumerClient] for a specific consumer group. When an Event Hub is created, it
starts with a default consumer group that can be used to get started. A consumer also needs to specify where in the
event stream to begin receiving events.

#### Consume events with EventHubConsumerAsyncClient

In the snippet below, we are creating an asynchronous consumer that receives events from `partitionId` and only listens
to newest events that get pushed to the partition by invoking `receiveFromPartition(String, EventPosition)`. Developers
can begin receiving events from multiple partitions using the same EventHubConsumerAsyncClient by calling
`receiveFromPartition(String, EventPosition)` with another partition id, and subscribing to that Flux.

```java
EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
    .connectionString("<< CONNECTION STRING FOR SPECIFIC EVENT HUB INSTANCE >>")
    .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
    .buildAsyncConsumerClient();

// Receive events from partition with id "0", only getting events that are newly added to the partition.
consumer.receiveFromPartition("0", EventPosition.latest()).subscribe(event -> {
    // Process each event as it arrives.
});
```

#### Consume events with EventHubConsumerClient

Developers can create a synchronous consumer that returns events in batches using an `EventHubConsumerClient`. In the
snippet below, a consumer is created that starts reading events from the beginning of the partition's event stream.

```java
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

To consume events for all partitions of an Event Hub, you'll create an [`EventProcessorClient`][EventProcessorClient]
for a specific consumer group. When an Event Hub is created, it provides a default consumer group that can be used to
get started.

The [`EventProcessorClient`][EventProcessorClient] will delegate processing of events to a callback function that you
provide, allowing you to focus on the logic needed to provide value while the processor holds responsibility for
managing the underlying consumer operations.

In our example, we will focus on building the [`EventProcessorClient`][EventProcessorClient], use the
[`InMemoryCheckpointStore`][InMemoryCheckpointStore] available in samples, and a callback function that processes events
received from the Event Hub and writes to console.

```java
class Program {
    public static void main(String[] args) {
        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
            .consumerGroup("<< CONSUMER GROUP NAME >>")
            .connectionString("<< EVENT HUB CONNECTION STRING >>")
            .checkpointStore(new InMemoryCheckpointStore())
            .processEvent(eventContext -> {
                System.out.println("Partition id = " + eventContext.getPartitionContext().getPartitionId() + " and "
                    + "sequence number of event = " + eventContext.getEventData().getSequenceNumber());
            })
            .processError(errorContext -> {
                System.out.println("Error occurred while processing events " + errorContext.getThrowable().getMessage());
            })
            .buildEventProcessorClient();

        // This will start the processor. It will start processing events from all partitions.
        eventProcessorClient.start();

        // (for demo purposes only - adding sleep to wait for receiving events)
        TimeUnit.SECONDS.sleep(2);

        // When the user wishes to stop processing events, they can call `stop()`.
        eventProcessorClient.stop();
    }
}
```

## Troubleshooting

### Enable client logging

You can set the `AZURE_LOG_LEVEL` environment variable to view logging statements made in the client library. For
example, setting `AZURE_LOG_LEVEL=2` would show all informational, warning, and error log messages. The log levels can
be found here: [log levels][LogLevels].

### Enable AMQP transport logging

If enabling client logging is not enough to diagnose your issues. You can enable logging to a file in the underlying
AMQP library, [Qpid Proton-J][qpid_proton_j_apache]. Qpid Proton-J uses `java.util.logging`. You can enable logging by
create a configuration file with the contents below. Or set `proton.trace.level=ALL` and whichever configuration options
you want for the `java.util.logging.Handler` implementation. Implementation classes and their options can be found in
[Java 8 SDK javadoc][java_8_sdk_javadocs].

#### Sample "logging.config" file

The configuration file below logs trace output from proton-j to the file "proton-trace.log".

```
handlers=java.util.logging.FileHandler
.level=OFF
proton.trace.level=ALL
java.util.logging.FileHandler.level=ALL
java.util.logging.FileHandler.pattern=proton-trace.log
java.util.logging.FileHandler.formatter=java.util.logging.SimpleFormatter
java.util.logging.SimpleFormatter.format=[%1$tF %1$tr] %3$s %4$s: %5$s %n
```

### Common exceptions

#### AMQP exception

This is a general exception for AMQP related failures, which includes the AMQP errors as ErrorCondition and the context
that caused this exception as ErrorContext. 'isTransient' is a boolean indicating if the exception is a transient error
or not. If true, then the request can be retried; otherwise not.

[`AmqpErrorCondition`][AmqpErrorCondition] contains error conditions common to the AMQP protocol and used by Azure
services. When an AMQP exception is thrown, examining the error condition field can inform developers as to why the AMQP
exception occurred and if possible, how to mitigate this exception. A list of all the AMQP exceptions can be found in
[OASIS AMQP Version 1.0 Transport Errors][oasis_amqp_v1_error].

The [`AmqpErrorContext`][AmqpErrorContext] in the [`AmqpException`][AmqpException] provides information about the AMQP
session, link, or connection that the exception occurred in. This is useful to diagnose which level in the transport
this exception occurred at and whether it was an issue in one of the producers or consumers.

The recommended way to solve the specific exception the AMQP exception represents is to follow the
[Event Hubs Messaging Exceptions][event_hubs_messaging_exceptions] guidance.

#### Operation cancelled exception

It occurs when the underlying AMQP layer encounters an abnormal link abort or the connection is disconnected in an
unexpected fashion. It is recommended to attempt to verify the current state and retry if necessary.

#### Message size exceeded

Event data, both individual and in batches, have a maximum size allowed. This includes the data of the event, as well as
any associated metadata and system overhead. The best approach for resolving this error is to reduce the number of
events being sent in a batch or the size of data included in the message. Because size limits are subject to change,
please refer to [Azure Event Hubs quotas and limits][event_hubs_quotas] for specifics.

### Other exceptions

For detailed information about these and other exceptions that may occur, please refer to [Event Hubs Messaging
Exceptions][event_hubs_messaging_exceptions].

## Next steps

Beyond those discussed, the Azure Event Hubs client library offers support for many additional scenarios to help take
advantage of the full feature set of the Azure Event Hubs service. In order to help explore some of the these scenarios,
the following set of sample is available [here][samples_readme].


## Contributing

If you would like to become an active contributor to this project please refer to our [Contribution
Guidelines](./CONTRIBUTING.md) for more information.

<!-- Links -->
[amqp_transport_error]: https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#type-amqp-error
[api_documentation]: https://aka.ms/java-docs
[app_registration_page]: https://docs.microsoft.com/azure/active-directory/develop/howto-create-service-principal-portal#get-values-for-signing-in
[application_client_secret]: https://docs.microsoft.com/azure/active-directory/develop/howto-create-service-principal-portal#create-a-new-application-secret
[event_hubs_connection_string]: https://docs.microsoft.com/azure/event-hubs/event-hubs-get-connection-string
[event_hubs_create]: https://docs.microsoft.com/azure/event-hubs/event-hubs-create
[event_hubs_features]: https://docs.microsoft.com/azure/event-hubs/event-hubs-features
[event_hubs_messaging_exceptions]: https://docs.microsoft.com/azure/event-hubs/event-hubs-messaging-exceptions
[event_hubs_product_docs]: https://docs.microsoft.com/azure/event-hubs/
[event_hubs_quotas]: https://docs.microsoft.com/azure/event-hubs/event-hubs-quotas
[java_8_sdk_javadocs]: https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html
[maven]: https://maven.apache.org/
[oasis_amqp_v1_error]: http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#type-error
[oasis_amqp_v1]: http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-overview-v1.0-os.html
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[qpid_proton_j_apache]: http://qpid.apache.org/proton/
[samples_readme]: ./src/samples/README.md
[sample_examples]: ./src/samples/java/com/azure/messaging/eventhubs/
[sample_consume_event]: ./src/samples/java/com/azure/messaging/eventhubs/ConsumeEvents.java
[sample_consume_sequence_number]: ./src/samples/java/com/azure/messaging/eventhubs/ConsumeEventsFromKnownSequenceNumberPosition.java
[sample_event_processor]: ./src/samples/java/com/azure/messaging/eventhubs/EventProcessorSample.java
[sample_get_event_hubs_metadata]: ./src/samples/java/com/azure/messaging/eventhubs/GetEventHubMetadata.java
[sample_publish_custom_metadata]: ./src/samples/java/com/azure/messaging/eventhubs/PublishEventsWithCustomMetadata.java
[sample_publish_identity]: ./src/samples/java/com/azure/messaging/eventhubs/PublishEventsWithAzureIdentity.java
[sample_publish_partitionId]: ./src/samples/java/com/azure/messaging/eventhubs/PublishEventsToSpecificPartition.java
[sample_publish_partitionKey]: ./src/samples/java/com/azure/messaging/eventhubs/PublishEventsWithPartitionKey.java
[sample_publish_size_limited]: ./src/samples/java/com/azure/messaging/eventhubs/PublishEventsWithSizeLimitedBatches.java
[source_code]: ./
[AmqpException]: ../../core/azure-core-amqp/src/main/java/com/azure/core/amqp/exception/AmqpException.java
[AmqpErrorCondition]: ../../core/azure-core-amqp/src/main/java/com/azure/core/amqp/exception/AmqpErrorCondition.java
[AmqpErrorContext]: ../../core/azure-core-amqp/src/main/java/com/azure/core/amqp/exception/AmqpErrorContext.java
[EventHubConsumerAsyncClient]: ./src/main/java/com/azure/messaging/eventhubs/EventHubConsumerAsyncClient.java
[EventHubConsumerClient]: ./src/main/java/com/azure/messaging/eventhubs/EventHubConsumerClient.java
[EventHubProducerAsyncClient]: ./src/main/java/com/azure/messaging/eventhubs/EventHubProducerAsyncClient.java
[EventHubProducerClient]: ./src/main/java/com/azure/messaging/eventhubs/EventHubProducerClient.java
[EventProcessorClient]: ./src/main/java/com/azure/messaging/eventhubs/EventProcessorClient.java
[CreateBatchOptions]: ./src/main/java/com/azure/messaging/eventhubs/models/CreateBatchOptions.java
[InMemoryCheckpointStore]: ./src/samples/java/com/azure/messaging/eventhubs/InMemoryCheckpointStore.java
[LogLevels]: ../../core/azure-core/src/main/java/com/azure/core/util/logging/ClientLogger.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Feventhubs%2Fazure-messaging-eventhubs%2FREADME.png)
