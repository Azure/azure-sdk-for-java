---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-event-hubs
  - azure-storage
urlFragment: eventhubs-checkpoint-store-blob-samples
---

# Azure Event Hubs Checkpoint Store client library samples for Java

Azure Event Hubs Checkpoint Store samples are a set of self-contained Java programs that demonstrate interacting 
with Azure Event Hubs Checkpoint Store using the client library. 

## Key concepts
Key concepts are explained in detail [here][sdk_readme_key_concepts].

## Getting started
Please refer to the [Getting Started][sdk_readme_getting_started] section.

## Examples

- [Create an instance of Storage Container client][sample_container_client]
- [Create an instance using Azure Identity][sample_azure_identity]
- [Consume events from all Event Hub partitions][sample_event_processor]
- [Specify alternate storage version to create checkpoint store][sample_checkpointstore_custom_storage_version]

## Troubleshooting
See [Troubleshooting][sdk_readme_troubleshooting].

## Next steps
See [Next steps][sdk_readme_next_steps].

## Contributing

If you would like to become an active contributor to this project please refer to our [Contribution
Guidelines](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/CONTRIBUTING.md) for more information.

<!-- Links -->
[sample_azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/src/samples/java/com/azure/messaging/eventhubs/checkpointstore/blob/EventProcessorWithAzureIdentity.java
[sample_checkpointstore_custom_storage_version]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/src/samples/java/com/azure/messaging/eventhubs/checkpointstore/blob/EventProcessorWithCustomStorageVersion.java
[sample_container_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/src/samples/java/com/azure/messaging/eventhubs/checkpointstore/blob/BlobCheckpointStoreSample.java
[sample_event_processor]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/src/samples/java/com/azure/messaging/eventhubs/checkpointstore/blob/EventProcessorBlobCheckpointStoreSample.java
[sdk_readme_getting_started]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/README.md#getting-started
[sdk_readme_key_concepts]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/README.md#key-concepts
[sdk_readme_next_steps]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/README.md#next-steps
[sdk_readme_troubleshooting]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/README.md#troubleshooting

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Feventhubs%2Fazure-messaging-eventhubs-checkpointstore-blob%2Fsrc%2Fsamples%2FREADME.png)
