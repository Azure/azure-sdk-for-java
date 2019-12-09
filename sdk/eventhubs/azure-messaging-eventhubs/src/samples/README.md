# Azure Event Hubs client library for Java - Samples

Azure Event Hubs samples are a set of self-contained Java programs that demonstrate interacting with Azure Event Hubs
using the client library. Each sample focuses on a specific scenario and can be executed independently. 

## Key concepts
Key concepts are explained in detail [here][sdk_readme_key_concepts].

## Getting started
Please refer to the [Getting Started][sdk_readme_getting_started] section.

## Examples

- [Inspect Event Hub and partition properties][sample_get_event_hubs_metadata]
- [Publish events using Microsoft identity platform][sample_publish_identity]
- [Publish events to a specific Event Hub partition with partition identifier][sample_publish_partitionId]
- [Publish events to a specific Event Hub partition with partition key][sample_publish_partitionKey]
- [Publish events to an Event Hub with a size-limited batch][sample_publish_size_limited]
- [Publish events with custom metadata][sample_publish_custom_metadata]
- [Consume events from an Event Hub partition][sample_consume_event]
- [Consume events starting from an event sequence number][sample_consume_sequence_number]
- [Consume events from all partitions using EventProcessorClient][sample_event_processor]

## Troubleshooting
See [Troubleshooting][sdk_readme_troubleshooting].

## Next steps
See [Next steps][sdk_readme_next_steps].

## Contributing

If you would like to become an active contributor to this project please refer to our [Contribution
Guidelines](../../CONTRIBUTING.md) for more information.

<!-- Links -->
[sdk_readme_key_concepts]: ../../README.md#key-concepts
[sdk_readme_getting_started]: ../../README.md#getting-started
[sdk_readme_troubleshooting]: ../../README.md#troubleshooting
[sdk_readme_next_steps]: ../../README.md#next-steps
[sample_consume_event]: ./java/com/azure/messaging/eventhubs/ConsumeEvents.java
[sample_consume_sequence_number]: ./java/com/azure/messaging/eventhubs/ConsumeEventsFromKnownSequenceNumberPosition.java
[sample_event_processor]: ./java/com/azure/messaging/eventhubs/EventProcessorSample.java
[sample_get_event_hubs_metadata]: ./java/com/azure/messaging/eventhubs/GetEventHubMetadata.java
[sample_publish_custom_metadata]: ./java/com/azure/messaging/eventhubs/PublishEventsWithCustomMetadata.java
[sample_publish_identity]: ./java/com/azure/messaging/eventhubs/PublishEventsWithAzureIdentity.java
[sample_publish_partitionId]: ./java/com/azure/messaging/eventhubs/PublishEventsToSpecificPartition.java
[sample_publish_partitionKey]: ./java/com/azure/messaging/eventhubs/PublishEventsWithPartitionKey.java
[sample_publish_size_limited]: ./java/com/azure/messaging/eventhubs/PublishEventsWithSizeLimitedBatches.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Feventhubs%2Fazure-messaging-eventhubs%2Fsrc%2Fsamples%2README.png)
