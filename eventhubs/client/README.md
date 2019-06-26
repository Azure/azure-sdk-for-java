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

[Source code][source_code] | [Package (Maven) (coming soon)][package] | [API reference documentation][api_documentation]
| [Product documentation][event_hubs_product_docs]

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
    <version>1.0.0-SNAPSHOT</version>
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
EventHubClientBuilder builder = new EventHubClientBuilder()
    .connectionString(connectionString, eventHubName);
EventHubClient client = builder.build();
```

## Key concepts

- An **Event Hub client** is the primary interface for developers interacting with the Event Hubs client library, 
  allowing for inspection of Event Hub metadata and providing a guided experience towards specific Event Hub operations 
  such as the creation of producers and consumers.

- An **Event Hub producer** is a source of telemetry data, diagnostics information, usage logs, or other log data, as 
  part of an embedded device solution, a mobile device application, a game title running on a console or other device, 
  some client or server based business solution, or a web site.  

- An **Event Hub consumer** picks up such information from the Event Hub and processes it. Processing may involve 
  aggregation, complex computation and filtering. Processing may also involve distribution or storage of the information
  in a raw or transformed fashion. Event Hub consumers are often robust and high-scale platform infrastructure parts 
  with built-in analytics capabilities, like Azure Stream Analytics, Apache Spark, or Apache Storm.

- A **partition** is an ordered sequence of events that is held in an Event Hub. Partitions are a means of data 
  organization associated with the parallelism required by event consumers. Azure Event Hubs provides message streaming
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

## Troubleshooting


### Logging in debug
You can use the ClientLogger class to get the debug logs. The class supports asVerbose(), asInfo(), asWarning(), and asError() as logging levels. An example of logging level, asInfo(), shows below

```java
ClientLogger logger = new ClientLogger(Example.class);
try {
    upload(resource);
} catch (Throwable ex) {
    logger.asError().log("Failed to upload {}", resource.name(), ex);
}
```

### Common exceptions
#### AMQP exception
This is a general exception for AMQP related failures, which includes the AMQP errors as ErrorCondition and the context 
that caused this exception as ErrorContext. 'isTransient' is A boolean indicating if the exception is a transient error
or not. If true, then the request can be retried; otherwise not.

- ErrorCondition: it contains constants common to the AMQP protocol and constants shared by Azure services. More detail
  can be found in the link: [Event Hubs Messaging Exceptions][event_hubs_messaging_exceptions].
- ErrorContext, it provides context that caused the AmqpException. The error occurs could be from AmqpConnection, 
  AmqpSession, or AmqpLink. Such as SessionErrorContext and LinkErrorContext, the context for an error that occurs in an
  AMQP session and AMQP link, respectively.

The recommend way to solve the specific exception the AMQP exception represents, please take the recommended action in [Event Hubs Messaging Exceptions][event_hubs_messaging_exceptions].
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
Beyond those discussed, the Azure Event Hubs client library offers support for 
many additional scenarios to help take advantage of the full feature set of the Azure Event Hubs service. In order to help explore some of the these scenarios, the following set of sample is available:
- [Inspect Event Hub and partition properties][sample_get_event_hubs_metadata]
- [Publish an event to an Event Hub][sample_send_event]
- [Publish events to a specific Event Hub partition with producer option][sample_send_producer_option]
- [Publish events to a specific Event Hub partition with send option][sample_send_send_option]
- [Publish events with custom metadata][sample_send_custom_event_data]
- [Consume events from an Event Hub partition][sample_receive_event]
- [Consume event batch][sample_receive_batch]
- [Save the last read event and resume from that point][sample_sequence_number]

## Contributing

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft
Azure Projects Contribution Guidelines](http://azure.github.io/guidelines.html).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- Links -->
[api_documentation]: https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/index.html
[event_hubs_connection_string]: https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-get-connection-string
[event_hubs_create]: https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-create
[event_hubs_product_docs]: https://docs.microsoft.com/en-us/azure/event-hubs/
[event_hubs_features]: https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-features
[maven]: https://maven.apache.org/
[package]: not-valid-link
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
[sample_receive_batch]: https://github.com/Azure/azure-sdk-for-java/blob/master/eventhubs/client/azure-eventhubs/src/samples/java/ReceiveEventsByBatch.java
