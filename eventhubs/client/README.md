# Azure Event Hubs client library for Java

Azure Event Hubs is a highly scalable publish-subscribe service that can ingest millions of events per second and stream
them to multiple consumers. This lets you process and analyze the massive amounts of data produced by your connected
devices and applications. Once Event Hubs has collected the data, you can retrieve, transform, and store it by using any
real-time analytics provider or with batching/storage adapters. If you would like to know more about Azure Event Hubs,
you may wish to review: [What is Event Hubs](https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-about)?

The Azure Event Hubs client library allows for publishing and consuming of Azure Event Hubs events and may be used to:

- Emit telemetry about your application for business intelligence and diagnostic purposes.
- Publish facts about the state of your application which interested parties may observe and use as a trigger for taking
  action.
- Observe interesting operations and interactions happening within your business or other ecosystem, allowing loosely
  coupled systems to interact without the need to bind them together.
- Receive events from one or more publishers, transform them to better meet the needs of your ecosystem, then publish
  the transformed events to a new stream for consumers to observe.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product documentation][event_hubs_product_docs]

## Getting started

### Prerequisites

- Java Development Kit (JDK) with version 8 or above
- [Maven][maven]
- Microsoft Azure subscription
    - You can create a free account at: https://azure.microsoft.com
- Azure Event Hubs instance
    - Step-by-step guide for [creating an Event Hub using the Azure Portal][event_hubs_create]

### Adding the package to your product

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventhubs</artifactId>
    <version>5.0.0-preview.1</version>
</dependency>
```

### Obtain a connection string

For the Event Hubs client library to interact with an Event Hub, it will need to understand how to connect and authorize
with it. The easiest means for doing so is to use a connection string, which is created automatically when creating an
Event Hubs namespace. If you aren't familiar with shared access policies in Azure, you may wish to follow the
step-by-step guide to [get an Event Hubs connection string][event_hubs_connection_string].

### Create an Event Hub client

Once the Event Hub and connection string are available, they can be used to create a client for interacting with Azure
Event Hubs. Create an `EventHubClient` using the `EventHubClientBuilder`:

```java
String connectionString = "<< CONNECTION STRING FOR THE EVENT HUBS NAMESPACE >>";
String eventHubName = "<< NAME OF THE EVENT HUB >>";
EventHubClient client = new EventHubClientBuilder()
    .connectionString(connectionString, eventHubName)
    .build();
```

## Key concepts

- An **Event Hub client** is the primary interface for developers interacting with the Event Hubs client library, 
  allowing for inspection of Event Hub metadata and providing a guided experience towards specific Event Hub operations 
  such as the creation of producers and consumers.

- An **Event Hub producer** is a source of telemetry data, diagnostics information, usage logs, or other log data, as 
  part of an embedded device solution, a mobile device application, a game title running on a console or other device, 
  some client or server based business solution, or a web site.  

- An **Event Hub consumer** picks up such information from the Event Hub and processes it. Processing may involve 
  aggregation, complex computation, and filtering. Processing may also involve distribution or storage of the information
  in a raw or transformed fashion. Event Hub consumers are often robust and high-scale platform infrastructure parts 
  with built-in analytics capabilities, like Azure Stream Analytics, Apache Spark, or Apache Storm.

- A **partition** is an ordered sequence of events that is held in an Event Hub. Azure Event Hubs provides message streaming
  through a partitioned consumer pattern in which each consumer only reads a specific subset, or partition, of the 
  message stream. As newer events arrive, they are added to the end of this sequence. The number of partitions is
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

- [Inspect Event Hub and partition properties][sample_get_event_hubs_metadata]
- [Publish an event to an Event Hub][sample_send_event]
- [Consume events from an Event Hub partition][sample_receive_event]

### Publish events to an Event Hub

In order to publish events, you'll need to create an EventHubProducer. Producers may be dedicated to a specific partition,
or allow the Event Hubs service to decide which partition events should be published to. It is recommended to use 
automatic routing when the publishing of events needs to be highly available or when event data should be distributed 
evenly among the partitions. In the our example, we will take advantage of automatic routing.

you can also use the send method to send multiple events using a single call.

#### Producer creation

```java
EventHubProducer producer = client.createProducer();

// We will publish three events based on simple sentences.
Flux<EventData> data = Flux.just(
    new EventData("EventData Sample 1".getBytes(UTF_8)),
    new EventData("EventData Sample 2".getBytes(UTF_8)),
    new EventData("EventData Sample 3".getBytes(UTF_8)));

// Send that event. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
// event has been delivered to the Event Hub. It completes with an error if an exception occurred while sending
// the event.
producer.send(data).subscribe(
    (ignored) -> System.out.println("Event sent."),
    error -> {
        System.err.println("There was an error sending the event: " + error.toString());

        if (error instanceof AmqpException) {
            AmqpException amqpException = (AmqpException) error;

            System.err.println(String.format("Is send operation retriable? %s. Error condition: %s",
                amqpException.isTransient(), amqpException.getErrorCondition()));
        }
    }, () -> {
        // Disposing of our producer and client.
        try {
            producer.close();
        } catch (IOException e) {
            System.err.println("Error encountered while closing producer: " + e.toString());
            client.close();
        }
    }
);
```

To send events to a particular partition, set the optional parameter `partitionId` on the `EventHubProducerOptions` 
when creating an event producer. 

#### Partition ID

```java
 EventHubProducerOptions producerOptions = new EventHubProducerOptions().partitionId(partitionId);
 EventHubProducer producer = client.createProducer(producerOptions);
```
Many Event Hub operations take place within the scope of a specific partition. Because partitions are owned by the 
Event Hub, their names are assigned at the time of creation. To understand what partitions are available, You can use 
the `getPartitionIds` function to get the ids of all available partitions in your Event Hub instance.

```java
private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(30);
...

// We take the first partition id.
// .blockFirst() here is used to synchronously block until the first partition id is emitted. The maximum wait
// time is set by passing in the OPERATION_TIMEOUT value. If no item is emitted before the timeout elapses, a
// TimeoutException is thrown.

String firstPartition = client.getPartitionIds().blockFirst(OPERATION_TIMEOUT);
```
#### Partition Key

When an Event Hub producer is not associated with any specific partition, it may be desirable to request that the Event
 Hubs service keep different events or batches of events together on the same partition. This can be accomplished by 
 setting a `partition key` when publishing the events.
 
```java
// The partition key is NOT the identifier of a specific partition. Rather, it is an arbitrary piece of string data
// that Event Hubs uses as the basis to compute a hash value. Event Hubs will associate the hash value with a specific
// partition, ensuring that any events published with the same partition key are rerouted to the same partition.
//
// Note that there is no means of accurately predicting which partition will be associated with a given partition key;
// we can only be assured that it will be a consistent choice of partition. If you have a need to understand which
// exact partition an event is published to, you will need to use an Event Hub producer associated with that partition.

SendOptions sendOptions = new SendOptions().partitionKey("ANY_PARTITION_KEY");
producer.send(dataList, sendOptions).subscribe(
    ...
);
```

### Consume events from an Event Hub

In order to consume events, you'll need to create an EventHubConsumer for a specific partition and consumer group 
combination. When an Event Hub is created, it starts with a default consumer group that can be used to get started. 
A consumer also needs to specify where in the event stream to begin receiving events; in our example, we will focus 
on reading new events as they are published.

#### Consumer creation

```java
EventHubConsumer consumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, partitionID, 
    EventPosition.latest());
```

#### Consume events

```java
private static final int NUMBER_OF_EVENTS = 10;

...

CountDownLatch countDownLatch = new CountDownLatch(NUMBER_OF_EVENTS);

Disposable subscription = consumer.receive().subscribe(event -> {
    String contents = UTF_8.decode(event.body()).toString();
    System.out.println(String.format("[%s] Sequence Number: %s. Contents: %s", countDownLatch.getCount(), 
    event.sequenceNumber(), contents));
    countDownLatch.countDown();
});

// Because the consumer is only listening to new events, we need to send some events to `firstPartition`.
EventHubProducerOptions producerOptions = new EventHubProducerOptions().partitionId(partitionID);
EventHubProducer producer = client.createProducer(producerOptions);

// We create 10 events to send to the service and block until the send has completed.
Flux.range(0, NUMBER_OF_EVENTS).flatMap(number -> {
    String body = String.format("Hello world! Number: %s", number);
    return producer.send(new EventData(body.getBytes(UTF_8)));
}).blockLast(OPERATION_TIMEOUT);

// We wait for all the events to be received before continuing.
countDownLatch.await(OPERATION_TIMEOUT.getSeconds(), TimeUnit.SECONDS);

```

### Close resource

Dispose and close of all the resources we've created.

```java
subscription.dispose();
producer.close();
consumer.close();
client.close();
```
## Troubleshooting

### Enable client logging

You can set the `AZURE_LOG_LEVEL` environment variable to view logging statements made in the client library. For example,
setting `AZURE_LOG_LEVEL=2` would show all informational, warning, and error log messages. The log levels can be found 
here: [log levels][log_levels].

### Enable AMQP transport logging

If enabling client logging is not enough to diagnose your issues. You can enable logging to a file 
in the underlying AMQP library, [Qpid Proton-J][qpid_proton_j_apache]. Qpid Proton-J uses `java.util.logging`. You can 
enable logging by create a configuration file with the contents below. Or set `proton.trace.level=ALL` and whichever 
configuration options you want for the `java.util.logging.Handler` implementation. Implementation classes and their
options can be found in [Java 8 SDK javadoc][java_8_sdk_javadocs].

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

The [ErrorCondition][error_condition] contains error conditions common to the AMQP protocol and used by Azure services. 
When an AMQP exception is thrown, examining the error condition field can inform developers as to why the AMQP exception
occurred and if possible, how to mitigate this exception. A list of all the AMQP exceptions can be found in [OASIS AMQP
Version 1.0 Transport Errors][oasis_amqp_v1_error].
  
The [ErrorContext][error_context] in the [AmqpException][amqp_exception] provides information about the AMQP session, 
link, or connection that the exception occurred in. This is useful to diagnose which level in the transport this 
exception occurred at and whether it was an issue in one of the producers or consumers.

The recommended way to solve the specific exception the AMQP exception represents is to follow the
[Event Hubs Messaging Exceptions][event_hubs_messaging_exceptions] guidance.

#### Operation cancelled exception
It occurs when the underlying AMQP layer encounters an abnormal link abort or the connection is disconnected in an 
unexpected fashion. It is recommended to attempt to verify the current state and retry if necessary.

#### Message size exceeded
Event data, both individual and in batches, have a maximum size allowed. This includes the data of the event, as well as
any associated metadata and system overhead. The best approach for resolving this error is to reduce the number of events
being sent in a batch or the size of data included in the message. Because size limits are subject to change, please 
refer to Azure Event Hubs quotas and limits for specifics.

### Other exceptions
For detailed information about these and other exceptions that may occur, please refer to 
[Event Hubs Messaging Exceptions][event_hubs_messaging_exceptions].

## Next steps
Beyond those discussed, the Azure Event Hubs client library offers support for many additional scenarios to help take 
advantage of the full feature set of the Azure Event Hubs service. In order to help explore some of the these scenarios,
the following set of sample is available:
- [Inspect Event Hub and partition properties][sample_get_event_hubs_metadata]
- [Publish an event to an Event Hub][sample_send_event]
- [Publish events to a specific Event Hub partition with producer option][sample_send_producer_option]
- [Publish events to a specific Event Hub partition with send option][sample_send_send_option]
- [Publish events with custom metadata][sample_send_custom_event_data]
- [Consume events from an Event Hub partition][sample_receive_event]
- [Save the last read event and resume from that point][sample_sequence_number]

## Contributing
If you would like to become an active contributor to this project please refer to our [Contribution Guidelines](./CONTRIBUTING.md) for more information.

<!-- Links -->
[api_documentation]: https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/index.html
[event_hubs_connection_string]: https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-get-connection-string
[event_hubs_create]: https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-create
[event_hubs_product_docs]: https://docs.microsoft.com/en-us/azure/event-hubs/
[event_hubs_features]: https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-features
[maven]: https://maven.apache.org/
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/master/eventhubs/client/
[event_hubs_messaging_exceptions]: https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-messaging-exceptions
[amqp_transport_error]: https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#type-amqp-error
[oasis_amqp_v1]: http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-overview-v1.0-os.html
[sample_receive_event]: https://github.com/Azure/azure-sdk-for-java/blob/master/eventhubs/client/azure-eventhubs/src/samples/java/ReceiveEvent.java
[sample_send_event]:https://github.com/Azure/azure-sdk-for-java/blob/master/eventhubs/client/azure-eventhubs/src/samples/java/SendEvent.java
[sample_get_event_hubs_metadata]: https://github.com/Azure/azure-sdk-for-java/blob/master/eventhubs/client/azure-eventhubs/src/samples/java/GetEventHubMetadata.java
[sample_send_custom_event_data]: https://github.com/Azure/azure-sdk-for-java/blob/master/eventhubs/client/azure-eventhubs/src/samples/java/SendCustomEventDataList.java
[sample_sequence_number]: https://github.com/Azure/azure-sdk-for-java/blob/master/eventhubs/client/azure-eventhubs/src/samples/java/ReceiveEventsFromKnownSequenceNumberPosition.java
[sample_send_producer_option]: https://github.com/Azure/azure-sdk-for-java/blob/master/eventhubs/client/azure-eventhubs/src/samples/java/SendEventsWithProducerOptions.java
[sample_send_send_option]: https://github.com/Azure/azure-sdk-for-java/blob/master/eventhubs/client/azure-eventhubs/src/samples/java/SendEventDataListWIthSendOption.java
[qpid_proton_j_apache]: http://qpid.apache.org/proton/
[java_8_sdk_javadocs]: https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html
[error_condition]: https://github.com/Azure/azure-sdk-for-java/blob/master/core/azure-core-amqp/src/main/java/com/azure/core/amqp/exception/ErrorCondition.java
[error_context]: https://github.com/Azure/azure-sdk-for-java/blob/master/core/azure-core-amqp/src/main/java/com/azure/core/amqp/exception/ErrorContext.java
[amqp_exception]: https://github.com/Azure/azure-sdk-for-java/blob/master/core/azure-core-amqp/src/main/java/com/azure/core/amqp/exception/AmqpException.java
[oasis_amqp_v1_error]: http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#type-error
[log_levels]: https://github.com/Azure/azure-sdk-for-java/blob/master/core/azure-core/src/main/java/com/azure/core/util/logging/ClientLogger.java
