# Azure Event Hubs Checkpoint Store client library for Java using Storage Blobs

Azure Event Hubs Checkpoint Store can be used for storing checkpoints while processing events from Azure Event Hubs. 
This package uses Storage Blobs as a persistent store for maintaining checkpoints and partition ownership information. 
The `BlobEventProcessorStore` provided in this package can be plugged in to `EventProcessor`.

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

[//]: # ({x-version-update-start;com.azure:azure-messaging-eventhubs-checkpointstore-blob;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventhubs-checkpointstore-blob</artifactId>
    <version>1.0.0-preview.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authenticate the storage container client
In order to create an instance of `BlobEventProcessorStore`, a `ContainerAsyncClient` should first be created with 
appropriate SAS token with write access and connection string. To make this possible you'll need the Account SAS 
(shared access signature) string of Storage account. Learn more at [SAS Token][sas_token].

## Key concepts

### Checkpointing

Checkpointing is a process by which readers mark or commit their position within a partition event sequence. 
Checkpointing is the responsibility of the consumer and occurs on a per-partition basis within a consumer group. 
This responsibility means that for each consumer group, each partition reader must keep track of its current position 
in the event stream, and can inform the service when it considers the data stream complete. If a reader disconnects from
a partition, when it reconnects it begins reading at the checkpoint that was previously submitted by the last reader of
that partition in that consumer group. When the reader connects, it passes the offset to the event hub to specify the 
location at which to start reading. In this way, you can use checkpointing to both mark events as "complete" by 
downstream applications, and to provide resiliency if a failover between readers running on different machines occurs. 
It is possible to return to older data by specifying a lower offset from this checkpointing process. Through this 
mechanism, checkpointing enables both failover resiliency and event stream replay.

### Offsets & sequence numbers
Both offset & sequence number refer to the position of an event within a partition. You can think of them as a 
client-side cursor. The offset is a byte numbering of the event. The offset/sequence number enables an event consumer 
(reader) to specify a point in the event stream from which they want to begin reading events. You can specify the a 
timestamp such that you receive events that were enqueued only after the given timestamp. Consumers are responsible for 
storing their own offset values outside of the Event Hubs service. Within a partition, each event includes an offset, 
sequence number and the timestamp of when it was enqueued.

## Examples
- [Create an instance of Storage Container client][sample_container_client]
- [Consume events from all Event Hub partitions][sample_event_processor]

### Create an instance of Storage container with SAS token

```java
BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
    .connectionString("<STORAGE_ACCOUNT_CONNECTION_STRING>")
    .containerName("<CONTAINER_NAME>")
    .sasToken("<SAS_TOKEN>")
    .buildAsyncClient();
```

### Consume events using an Event Processor

To consume events for all partitions of an Event Hub, you'll create an [`EventProcessor`][source_eventprocessor] for a
specific consumer group. When an Event Hub is created, it provides a default consumer group that can be used to get
started.

The [`EventProcessor`][source_eventprocessor] will delegate processing of events to a callback function that you 
provide, allowing your application to focus on the business logic needed to provide value while the processor 
holds responsibility for managing the underlying consumer operations.

In our example, we will focus on building the [`EventProcessor`][source_eventprocessor], use the 
[`BlobEventProcessorStore`][source_blobeventprocessorstore], and a simple callback function to process the events 
received from the Event Hubs, writes to console and updates the checkpoint in Blob storage after each event.

```java
class Program {
    public static void main(String[] args) {
        Function<PartitionEvent, Mono<Void>> processEvent = partitionEvent -> {
            System.out.printf("Event received. Sequence number: %s%n.", partitionEvent.getEventData().sequenceNumber());
            return partitionEvent.getPartitionContext().updateCheckpoint(eventData);
          };                 

        EventProcessor eventProcessor = new EventProcessorBuilder()
            .connectionString("<< CONNECTION STRING FOR THE EVENT HUB INSTANCE >>")
            .consumerGroupName("<< CONSUMER GROUP NAME >>")
            .processEvent(processEvent)
            .eventProcessorStore(new BlobEventProcessorStore(blobContainerAsyncClient))
            .buildEventProcessor();

        // This will start the processor. It will start processing events from all partitions.
        eventProcessor.start();

        // When the user wishes to stop processing events, they can call `stop()`.
        eventProcessor.stop();
    }
}
```

## Troubleshooting

### Enable client logging

You can set the `AZURE_LOG_LEVEL` environment variable to view logging statements made in the client library. For
example, setting `AZURE_LOG_LEVEL=2` would show all informational, warning, and error log messages. The log levels can
be found here: [log levels][source_loglevels].

## Next steps
Get started by exploring the following samples:

1. [Blob Event Processor Store samples][sample_examples]
1. [Event Hubs and Event Processor samples][sample_event_hubs]

## Contributing

If you would like to become an active contributor to this project please refer to our [Contribution
Guidelines](./CONTRIBUTING.md) for more information.

<!-- Links -->
[api_documentation]: http://azure.github.io/azure-sdk-for-java
[event_hubs_product_docs]: https://docs.microsoft.com/azure/event-hubs/
[java_8_sdk_javadocs]: https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html
[maven]: https://maven.apache.org/
[sample_container_client]: ./src/samples/java/com/azure/messaging/eventhubs/checkpointstore/blob/BlobEventProcessorStoreSample.java
[sample_event_hubs]: ./src/samples/java/com/azure/messaging/eventhubs
[sample_event_processor]: ./src/samples/java/com/azure/messaging/eventhubs/checkpointstore/blob/EventProcessorBlobEventProcessorStoreSample.java
[sample_examples]: ./src/samples/java/com/azure/messaging/eventhubs/checkpointstore/blob
[sas_token]: https://docs.microsoft.com/azure/storage/common/storage-dotnet-shared-access-signature-part-1
[source_code]: ./
[source_eventprocessor]: ./src/main/java/com/azure/messaging/eventhubs/EventProcessor.java
[source_blobeventprocessorstore]: ./src/main/java/com/azure/messaging/eventhubs/checkpointstore/blob/BlobEventProcessorStore.java
[source_loglevels]: ../../core/azure-core/src/main/java/com/azure/core/util/logging/ClientLogger.java
[source_partition_processor]: ./src/main/java/com/azure/messaging/eventhubs/PartitionProcessor.java
[storage_account]: https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-portal

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/eventhubs/azure-messaging-eventhubs/README.png)
