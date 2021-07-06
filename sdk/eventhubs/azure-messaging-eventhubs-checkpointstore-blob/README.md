# Azure Event Hubs Checkpoint Store client library for Java using Storage Blobs

Azure Event Hubs Checkpoint Store can be used for storing checkpoints while processing events from Azure Event Hubs.
This package uses Storage Blobs as a persistent store for maintaining checkpoints and partition ownership information.
The `BlobCheckpointStore` provided in this package can be plugged in to `EventProcessor`.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product
documentation][event_hubs_product_docs] | [Samples][sample_examples]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Maven][maven]
- Microsoft Azure subscription
    - You can create a free account at: [https://azure.microsoft.com](https://azure.microsoft.com)
- Azure Event Hubs instance
    - Step-by-step guide for [creating an Event Hub using the Azure Portal][event_hubs_create]
- Azure Storage account
    - Step-by-step guide for [creating a Storage account using the Azure Portal][storage_account]

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-messaging-eventhubs-checkpointstore-blob;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventhubs-checkpointstore-blob</artifactId>
    <version>1.8.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authenticate the storage container client

In order to create an instance of `BlobCheckpointStore`, a `ContainerAsyncClient` should first be created with
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
sequence number, and the timestamp of when it was enqueued.

## Examples
- [Create an instance of Storage Container client][sample_container_client]
- [Create an instance using Azure Identity][sample_azure_identity]
- [Consume events from all Event Hub partitions][sample_event_processor]
- [Specify storage version to create checkpoint store][sample_checkpointstore_custom_storage_version]

### Create an instance of Storage container with SAS token

<!-- embedme ./src/samples/java/com/azure/messaging/eventhubs/checkpointstore/blob/ReadmeSamples.java#L25-L29 -->
```java
BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
    .connectionString("<STORAGE_ACCOUNT_CONNECTION_STRING>")
    .containerName("<CONTAINER_NAME>")
    .sasToken("<SAS_TOKEN>")
    .buildAsyncClient();
```

### Consume events using an Event Processor Client

To consume events for all partitions of an Event Hub, you'll create an
[`EventProcessorClient`][source_eventprocessorclient] for a specific consumer group. When an Event Hub is created, it
provides a default consumer group that can be used to get started.

The [`EventProcessorClient`][source_eventprocessorclient] will delegate processing of events to a callback function that you
provide, allowing you to focus on the logic needed to provide value while the processor holds responsibility for
managing the underlying consumer operations.

In our example, we will focus on building the [`EventProcessor`][source_eventprocessorclient], use the
[`BlobCheckpointStore`][source_blobcheckpointstore], and a simple callback function to process the events
received from the Event Hubs, writes to console and updates the checkpoint in Blob storage after each event.

<!-- embedme ./src/samples/java/com/azure/messaging/eventhubs/checkpointstore/blob/ReadmeSamples.java#L37-L63 -->
```java
BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
    .connectionString("<STORAGE_ACCOUNT_CONNECTION_STRING>")
    .containerName("<CONTAINER_NAME>")
    .sasToken("<SAS_TOKEN>")
    .buildAsyncClient();

EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
    .consumerGroup("<< CONSUMER GROUP NAME >>")
    .connectionString("<< EVENT HUB CONNECTION STRING >>")
    .checkpointStore(new BlobCheckpointStore(blobContainerAsyncClient))
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
```

## Troubleshooting

### Enable client logging

Azure SDK for Java offers a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Default SSL library

All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL
operations. The Boring SSL library is an uber jar containing native libraries for Linux / macOS / Windows, and provides
better performance compared to the default SSL implementation within the JDK. For more information, including how to
reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps

Get started by exploring the samples [here][samples_readme].

## Contributing

If you would like to become an active contributor to this project please refer to our [Contribution
Guidelines](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/CONTRIBUTING.md) for more information.

<!-- Links -->
[api_documentation]: https://azure.github.io/azure-sdk-for-java
[event_hubs_create]: https://docs.microsoft.com/azure/event-hubs/event-hubs-create
[event_hubs_product_docs]: https://docs.microsoft.com/azure/event-hubs/
[java_8_sdk_javadocs]: https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[maven]: https://maven.apache.org/
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/src/samples/README.md
[sample_azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/src/samples/java/com/azure/messaging/eventhubs/checkpointstore/blob/EventProcessorWithAzureIdentity.java
[sample_container_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/src/samples/java/com/azure/messaging/eventhubs/checkpointstore/blob/BlobCheckpointStoreSample.java
[sample_event_hubs]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/src/samples/java/com/azure/messaging/eventhubs
[sample_event_processor]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/src/samples/java/com/azure/messaging/eventhubs/checkpointstore/blob/EventProcessorBlobCheckpointStoreSample.java
[sample_checkpointstore_custom_storage_version]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/src/samples/java/com/azure/messaging/eventhubs/checkpointstore/blob/EventProcessorWithCustomStorageVersion.java
[sample_examples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/src/samples/java/com/azure/messaging/eventhubs/checkpointstore/blob
[sas_token]: https://docs.microsoft.com/azure/storage/common/storage-dotnet-shared-access-signature-part-1
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/
[source_eventprocessorclient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/main/java/com/azure/messaging/eventhubs/EventProcessorClient.java
[source_blobcheckpointstore]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/src/main/java/com/azure/messaging/eventhubs/checkpointstore/blob/BlobCheckpointStore.java
[source_loglevels]: .https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/.https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/core/azure-core/src/main/java/com/azure/core/util/logging/ClientLogger.java
[storage_account]: https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-portal

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Feventhubs%2Fazure-messaging-eventhubs-checkpointstore-blob%2FREADME.png)
