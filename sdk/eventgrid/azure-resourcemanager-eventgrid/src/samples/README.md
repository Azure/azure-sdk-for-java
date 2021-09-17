---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-event-grid
urlFragment: eventgrid-resourcemanager-samples
---

# Azure Event Grid Publish/Consume Samples for Java

This contains Java samples for publishing events to Azure Event Grid and consuming events from Azure Event Grid. 
It also contains a set of management samples that demonstrates how to manage topics and event subscriptions using Java code.

## Features

These samples demonstrate the following features:

- How to create an event grid topic using Java.
- How to create an event subscription to a topic using Java.
- How to create an event hub using Java.
- How to publish events to Azure Event Grid using Java.
- How to consume events delivered by Azure Event Grid through an Azure Event Hub consumer using Java.

## Getting started

Getting started explained in detail [here][EVENTGRID_README_GETTING_STARTED]. 
Please refer it to add dependency and configure authentication environment variables.

For details on including this dependency in other build tools (Gradle, SBT, etc), refer [here](https://search.maven.org/artifact/com.azure/azure-core).

## Sample details

Azure event grid publish and consume sample code is [here][EVENTGRID_SAMPLE_CODE]. It has the `main()` function and can be run directly.

The sample demonstrates following operations:

1. Create a resource group.
2. Create an event hub.
3. Create an event grid topic.
4. Create an event grid subscription.
5. Retrieve the event grid client connection key.
6. Create an event grid publisher client: `EventGridPublisherClient`.
7. Create an event hub consumer client: `EventHubConsumerAsyncClient`. 
8. Subscribe to coming events from event grid using `EventHubConsumerAsyncClient`.
9. Publish custom events to the event grid using `EventGridPublisherClient`.
10. Clean up the resources created above.

## Key concepts

Key concepts are explained in detail [here][EVENTGRID_README_KEY_CONCEPTS].

## Next steps

Start using Event Grid Java SDK in your solutions. Our SDK details could be found at [SDK README][EVENTGRID_SDK_README].

## Contributing

This project welcomes contributions and suggestions. Find [more contributing][EVENTGRID_README_CONTRIBUTING] details here.

## More information ##

[https://azure.com/java](https://azure.com/java)

If you don't have a Microsoft Azure subscription you can get a FREE trial account [here](https://go.microsoft.com/fwlink/?LinkId=330212)

<!-- LINKS -->
[EVENTGRID_SDK_README]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventgrid/azure-resourcemanager-eventgrid
[EVENTGRID_README_CONTRIBUTING]:https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventgrid/azure-resourcemanager-eventgrid#contributing
[EVENTGRID_README_GETTING_STARTED]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventgrid/azure-resourcemanager-eventgrid#getting-started
[EVENTGRID_README_KEY_CONCEPTS]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventgrid/azure-resourcemanager-eventgrid#key-concepts
[EVENTGRID_SAMPLE_CODE]:https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventgrid/azure-resourcemanager-eventgrid/src/samples/java/com/azure/resourcemanager/eventgrid/EventGridPublishAndConsumeExample.java

