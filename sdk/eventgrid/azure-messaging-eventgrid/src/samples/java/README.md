---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-event-grid
urlFragment: eventgrid-samples
---

# Azure EventGrid Samples client library for Java
Azure EventGrid samples are a set of self-contained Java programs that demonstrate interacting with Azure EventGrid
using the client library. Each sample focuses on a specific scenario and can be executed independently.

## Key concepts
Key concepts are explained in detail [here][sdk_readme_key_concepts].

## Getting started
Please refer to the [Getting Started][sdk_readme_getting_started] section.

### Obtaining an EventGrid endpoint and key
Please refer to the [Endpoint][sdk_readme_endpoint] and [Access Key][sdk_readme_access_key] section.

## Examples

### Asynchronously sending and receiving
- [Send CloudEvent to Event Grid Topic asynchronously][SendCloudEventAsync]
- [Send EventGridEvent to Event Grid Topic asynchronously][SendEventGridEventAsync]
- [Send EventGridEvent to Event Grid Domain asynchronously][SendEventGridEventToDomainAsync]


### Synchronous sending
- [Send CloudEvent to Event Grid Topic synchronously][SendCloudEventSync]
- [Send EventGridEvent to Event Grid Topic synchronously][SendEventGridEventSync]
- [Send EventGridEvent to Event Grid Domain synchronously][SendEventGridEventToDomainSync]

### Deserialize events
- [Deserialize Events from Json String][DeserializeEvents]
- [Deserialize Events with System Event Data][DeserializeSystemEvent]

### Generate Sas Tokken
- [Generate Sas Token][GenerateSasToken]

## Troubleshooting
See [Troubleshooting][sdk_readme_troubleshooting].

## Next steps
See [Next steps][sdk_readme_next_steps].

## Contributing

If you would like to become an active contributor to this project please refer to our [Contribution
Guidelines](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md) for more information.

<!-- LINKS -->
[sdk_readme_key_concepts]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventgrid/azure-messaging-eventgrid#key-concepts
[sdk_readme_getting_started]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventgrid/azure-messaging-eventgrid#getting-started
[sdk_readme_endpoint]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventgrid/azure-messaging-eventgrid#endpoint
[sdk_readme_access_key]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventgrid/azure-messaging-eventgrid#access-key
[sdk_readme_troubleshooting]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventgrid/azure-messaging-eventgrid#troubleshooting
[sdk_readme_next_steps]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventgrid/azure-messaging-eventgrid#next-steps
[SendCloudEventAsync]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventgrid/azure-messaging-eventgrid/src/samples/java/com/azure/messaging/eventgrid/samples/PublishCloudEventsToTopicAsynchronously.java
[SendEventGridEventAsync]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventgrid/azure-messaging-eventgrid/src/samples/java/com/azure/messaging/eventgrid/samples/PublishEventGridEventsToTopicAsynchronously.java
[SendEventGridEventToDomainAsync]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventgrid/azure-messaging-eventgrid/src/samples/java/com/azure/messaging/eventgrid/samples/PublishEventsToDomainAsynchronously.java
[SendCloudEventSync]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventgrid/azure-messaging-eventgrid/src/samples/java/com/azure/messaging/eventgrid/samples/PublishCloudEventsToTopic.java
[SendEventGridEventSync]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventgrid/azure-messaging-eventgrid/src/samples/java/com/azure/messaging/eventgrid/samples/PublishEventGridEventsToTopic.java
[SendEventGridEventToDomainSync]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventgrid/azure-messaging-eventgrid/src/samples/java/com/azure/messaging/eventgrid/samples/PublishEventsToDomain.java
[DeserializeEvents]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventgrid/azure-messaging-eventgrid/src/samples/java/com/azure/messaging/eventgrid/samples/DeserializeEventsFromString.java
[DeserializeSystemEvent]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventgrid/azure-messaging-eventgrid/src/samples/java/com/azure/messaging/eventgrid/samples/ProcessSystemEvents.java
[GenerateSasToken]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventgrid/azure-messaging-eventgrid/src/samples/java/com/azure/messaging/eventgrid/samples/GenerateSasToken.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Feventgrid%2Fazure-messaging-eventgrid%2Fsrc%2Fsamples%2FREADME.png)
