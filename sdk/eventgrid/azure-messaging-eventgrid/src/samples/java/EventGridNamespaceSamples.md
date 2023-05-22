---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-event-grid
urlFragment: eventgrid-namespace-samples
---

# Event Grid Namespace Samples client library for Java

Event Grid Namespace topics are a new feature in public preview. This feature allows for receiving events directly
from Azure Event Grid.

A full sample is available [here][https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventgrid/azure-messaging-eventgrid/src/samples/java/com/azure/messaging/eventgrid/samples/EventGRidNamespaceSample].

## Key concepts

Events are published using the CloudEvent schema.

The `EventGridClient` (and `EventGridAsyncClient`) have five operations:
1) Publish
2) Receive
3) Acknowledge
4) Release
5) Reject

### Publish
Publishing an event simply places the event into Event Grid.

### Receive
Receiving events retrieves a copy of the event, as well as a lock token. The lock token is used to communicate what was done with the event.

### Acknowledge
Acknowledging an event removes the event from Event Grid. This is used to indicate that the event was successfully processed.

### Release
Releasing an event makes the event available to be received again. This is used to indicate that the event was not successfully processed, but should be retried.

### Reject
Rejecting an event removes the event from Event Grid and places it in a dead letter queue, if configured.
