# Azure Checkpoint Store client library for Java using Storage Blobs.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product
documentation][event_hubs_product_docs] | [Samples][sample_examples]

## Getting started

### Prerequisites

- Java Development Kit (JDK) with version 8 or above
- [Maven][maven]
- Microsoft Azure subscription
    - You can create a free account at: https://azure.microsoft.com
- Azure Event Hubs instance
    - Step-by-step guide for [creating an Event Hub using the Azure Portal][event_hubs_create]
- Azure Storage account
    - Step-by-step guide for [creating a Storage account using the Azure Portal][storage_account]
### Adding the package to your product

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventhubs-checkpointstore-blob</artifactId>
    <version>1.0.0-preview.1</version>
</dependency>
```

### Default HTTP Client
By default all client libraries ship without any baked-in HTTP client. Users who want to use a client library are encouraged to opt-in to the relevant HTTP client for their needs. For most situations, this is achieved by including the following dependency in the Maven pom.xml file:

```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-netty</artifactId>
  <version>1.0.0-preview.4</version>
</dependency>
```

This will automatically configure all client libraries on the same classpath to make use of Netty for the HTTP client.

If, instead of Netty it is preferable to use OkHTTP, there is a HTTP client available for that too. Simply include the following dependency instead:

```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-okhttp</artifactId>
  <version>1.0.0-preview.4</version>
</dependency>
```

### Configuring HTTP Clients
When an HTTP client is included on the classpath, as shown above, it is not necessary to specify it in the client library builders, unless you want to customize the HTTP client in some fashion. If this is desired, the `httpClient` builder method is often available to achieve just this, by allowing users to provide a custom (or customized) `com.azure.core.http.HttpClient` instances.

For starters, by having the Netty or OkHTTP dependencies on your classpath, as shown above, you can create new instances of these `HttpClient` types using their builder APIs. For example, here is how you would create a Netty HttpClient instance:

```java
HttpClient client = new NettyAsyncHttpClientBuilder()
    .port(8080)
    .wiretap(true)
    .build();
```

## Key concepts

## Examples
- [Consume events from all Event Hub partitions][sample_event_processor]

### Consume events using an Event Processor

To consume events for all partitions of an Event Hub, you'll create an [`EventProcessor`][source_eventprocessor] for a
specific consumer group. When an Event Hub is created, it provides a default consumer group that can be used to get
started.

The [`EventProcessor`][source_eventprocessor] will delegate processing of events to a
[`PartitionProcessor`][source_partition_processor] implementation that you provide, allowing your application to focus on the
business logic needed to provide value while the processor holds responsibility for managing the underlying consumer operations.

In our example, we will focus on building the [`EventProcessor`][source_eventprocessor], use the 
[`BlobPartitionManager`][source_inmemorypartitionmanager], and a `PartitionProcessor` implementation that logs
events received and errors to console.

```java
class Program {
    public static void main(String[] args) {
        EventProcessor eventProcessor = new EventHubClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE EVENT HUB INSTANCE >>")
            .consumerGroupName("<< CONSUMER GROUP NAME>>")
            .partitionProcessorFactory(SimplePartitionProcessor::new)
            .partitionManager(new BlobPartitionManager(containerAsyncClient))
            .buildEventProcessor();

        // This will start the processor. It will start processing events from all partitions.
        eventProcessor.start();

        // When the user wishes to stop processing events, they can call `stop()`.
        eventProcessor.stop();
    }
}

class SimplePartitionProcessor extends PartitionProcessor {
    @Override
    Mono<Void> processEvent(PartitionContext partitionContext, EventData eventData) {
        System.out.printf("Event received. Sequence number: %s%n.", eventData.sequenceNumber());
        return partitionContext.updateCheckpoint(eventData);
    }
}
```

## Troubleshooting

### Enable client logging

You can set the `AZURE_LOG_LEVEL` environment variable to view logging statements made in the client library. For
example, setting `AZURE_LOG_LEVEL=2` would show all informational, warning, and error log messages. The log levels can
be found here: [log levels][source_loglevels].

## Next steps

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
[qpid_proton_j_apache]: http://qpid.apache.org/proton/
[sample_consume_event]: ./src/samples/java/com/azure/messaging/eventhubs/ConsumeEvent.java
[sample_event_processor]: ./src/samples/java/com/azure/messaging/eventhubs/EventProcessorBlobPartitionManagerSample.java
[sample_examples]: ./src/samples/java/com/azure/messaging/eventhubs/checkpointstore/blob
[sample_get_event_hubs_metadata]: ./src/samples/java/com/azure/messaging/eventhubs/GetEventHubMetadata.java
[sample_publish_custom_metadata]: ./src/samples/java/com/azure/messaging/eventhubs/PublishEventsWithCustomMetadata.java
[sample_publish_event]: ./src/samples/java/com/azure/messaging/eventhubs/PublishEvent.java
[sample_publish_eventdatabatch]: ./src/samples/java/com/azure/messaging/eventhubs/PublishEventDataBatch.java
[sample_publish_partitionId]: ./src/samples/java/com/azure/messaging/eventhubs/PublishEventsToSpecificPartition.java
[sample_publish_partitionKey]: ./src/samples/java/com/azure/messaging/eventhubs/PublishEventsWithPartitionKey.java
[sample_sequence_number]: ./src/samples/java/com/azure/messaging/eventhubs/ConsumeEventsFromKnownSequenceNumberPosition.java
[source_amqpexception]: ../../core/azure-core-amqp/src/main/java/com/azure/core/amqp/exception/AmqpException.java
[source_code]: ./
[source_errorcondition]: ../../core/azure-core-amqp/src/main/java/com/azure/core/amqp/exception/ErrorCondition.java
[source_errorcontext]: ../../core/azure-core-amqp/src/main/java/com/azure/core/amqp/exception/ErrorContext.java
[source_eventhubasyncclient]: ./src/main/java/com/azure/messaging/eventhubs/EventHubAsyncClient.java
[source_eventhubconsumer]: ./src/main/java/com/azure/messaging/eventhubs/EventHubProducer.java
[source_eventhubproduceroptions]: ./src/main/java/com/azure/messaging/eventhubs/models/EventHubProducerOptions.java
[source_eventprocessor]: ./src/main/java/com/azure/messaging/eventhubs/EventProcessor.java
[source_blobpartitionmanager]: ./src/main/java/com/azure/messaging/eventhubs/checkpointstore/blob/BlobPartitionManager.java
[source_loglevels]: ../../core/azure-core/src/main/java/com/azure/core/util/logging/ClientLogger.java
[source_partition_processor]: ./src/main/java/com/azure/messaging/eventhubs/PartitionProcessor.java
[storage_account]: https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-portal

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/eventhubs/azure-messaging-eventhubs/README.png)
