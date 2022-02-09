# Guide for migrating to `azure-messaging-eventgrid` from `microsoft-azure-eventgrid`

This guide is intended to assist in the migration to `azure-messaging-eventgrid` from `microsoft-azure-eventgrid`. It will focus on side-by-side comparisons for similar operations between the two packages.

We assume that you are familiar with the old SDK `microsoft-azure-eventgrid`. If not, please refer to the new SDK README for [azure-messaging-eventgrid][README] directly rather than this migration guide.

## Table of contents

- [Guide for migrating to `azure-messaging-eventgrid` from `microsoft-azure-eventgrid`](#guide-for-migrating-to-azure-messaging-eventgrid-from-microsoft-azure-eventgrid)
    - [Table of contents](#table-of-contents)
    - [Migration benefits](#migration-benefits)
        - [Cross Service SDK improvements](#cross-service-sdk-improvements)
    - [Important changes](#important-changes)
        - [Group id, artifact id, and package names](#group-id-artifact-id-and-package-names)
        - [Instantiating clients](#instantiating-clients)
        - [Send events to an Event Grid Topic](#send-events-to-an-event-grid-topic)
        - [Send events to an Event Grid Domain](#send-events-to-an-event-grid-domain)
        - [Deserialize events and data](#deserialize-events-and-data)
        - [Handle System Events](#handle-system-events)
    - [Additional samples](#additional-samples)

## Migration benefits

A natural question to ask when considering whether or not to adopt a new version or library is its benefits. As Azure has matured and been embraced by a more diverse group of developers, we have been focused on learning the patterns and practices to best support developer productivity and to understand the gaps that the Java
client libraries have.

There were several areas of consistent feedback expressed across the Azure client library ecosystem. The most important is that the client libraries for different Azure services have not had a consistent organization, naming, and API structure. Additionally, many developers have felt that the learning curve was difficult, and the APIs did not offer a good, approachable, and consistent onboarding story for those learning Azure or exploring a specific Azure service.

To improve the development experience across Azure services, a set of uniform [design guidelines][Guidelines] was created for all languages to drive a
consistent experience with established API patterns for all services. A set of [Java design guidelines][GuidelinesJava] was introduced to ensure that Java clients have a natural and idiomatic feel with respect to the Java ecosystem. Further details are available in the guidelines
for those interested.

Aside from the benefits of the new design mentioned above, the `azure-messaging-eventgrid` adds new features to send `CloudEvent` and custom events to
an EventGrid Topic or Domain Topic. Refer to the [README][README] for more information about sending `CloudEvent` and custom events.

### Cross Service SDK improvements

The modern Event Grid client library also provides the ability to share in some of the cross-service improvements made to the Azure development experience, such as

- A unified logging and diagnostics pipeline offering a common view of the activities across each of the client libraries.
- A unified asynchronous programming model using [Project Reactor][project-reactor].
- A unified method of creating clients via client builders to interact with Azure services.

## Important changes

### Group id, artifact id, and package names

Group ids, artifact ids, and package names for the modern Azure client libraries for Java have changed. They follow the [Java SDK naming guidelines][GuidelinesJavaDesign]. Each will have the group id `com.azure`, an artifact id following the pattern `azure-[area]-[service]`, and the root package name `com.azure.[area].[Service]`. The legacy clients have a group id `com.microsoft.azure` and their package names followed the pattern `com.microsoft. Azure.[service]`. This provides a quick and accessible means to help understand, at a glance, whether you are using modern or legacy clients.

In EventGrid, the modern client libraries have packages and namespaces that begin with `com.azure.messaging.eventgrid` and were released starting with 4.0.0. The legacy client libraries have package names starting with `com.microsoft.azure.eventgrid` and a version of 1.x.x or below.
The new SDK starts with 4.0.0 to be inline with EventGrid SDKs of other languages.

#### Instantiating clients

In 1.x, the `EventGridClient` was instantiated via the `EventGridClientImpl` constructor. The client contains both sync and async methods.
It doesn't have the `endpoint` while the publish methods like `publishEvents` accepts `topicHostname`.
It can send only `EventGridEvent` to an EventGrid topic or domain.
```java
String endpoint = "<endpont of your event grid topic/domain that accepts EventGridEvent schema>";
TopicCredentials topicCredentials = new TopicCredentials(key);
EventGridClient client = new EventGridClientImpl(topicCredentials);
```

In 4.x, the creation of the client is done through the [EventGridPublisherClientBuilder][EventGridPublisherClientBuilder]. The sync and async operations are separated to [EventGridPublisherClient] and [EventGridPublisherAsyncClient].
It must have a full-url `endpoint` instead of a host name.
```java
EventGridPublisherClient<EventGridEvent> eventGridEventClient = new EventGridPublisherClientBuilder()
    .endpoint("<endpont of your event grid topic/domain that accepts EventGridEvent schema>")
    .credential(new AzureKeyCredential("<key for the endpoint>"))
    .buildEventGridEventPublisherClient();
```
Aside from [EventGridEvent][EventGridEvent], the generic client can be instantiated to also send `CloudEvent`, or custom events to an EventGrid topic or domain.

#### Send events to an Event Grid Topic
In 1.x, the `publishEvent` method has the EventGrid endpoint as a parameter.
```java
List<EventGridEvent> eventsList = new ArrayList<>();
for (int i = 0; i < 5; i++) {
    eventsList.add(new EventGridEvent(
        UUID.randomUUID().toString(),
        String.format("Door%d", i),
        new ContosoItemReceivedEventData("Contoso Item SKU #1"),
        "Contoso.Items.ItemReceived",
        DateTime.now(),
        "2.0"
    ));
}
client.publishEvents(endpoint, getEventsList());
```

In 4.x, `publishEvents` are renamed to `sendEvents` in line with other Azure messaging SDKs such as Event Hubs and Service Bus.
`sendEvents` doesn't need the EventGrid endpoint because the client already has it.
The `EventGridEvent` class set `id` to an UUID and `time` to now by default so you don't have to set it.
```java
List<EventGridEvent> eventsList = new ArrayList<>();
for (int i = 0; i < 5; i++) {
    eventsList.add(new EventGridEvent(
        String.format("Door%d", i),
        "Contoso.Items.ItemReceived",
        BinaryData.fromObject(new ContosoItemReceivedEventData("Contoso Item SKU #1")),
        "2.0"
    ));
}
eventGridEventClient.sendEvents(events);
```

#### Send events to an Event Grid Domain
To send an `EventGridEvent` to an Event Grid Domain Topic, in both versions of SDKs, you need to set the `topic` attribute for the event.

In 1.x, you set the topic of an `EventGridEvent` and then send it to the topic of the Event Grid Domain.
```java
// Create an event 
List<EventGridEvent> eventsList = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
        eventsList.add(new EventGridEvent(
            UUID.randomUUID().toString(),
            String.format("Door%d", i),
            new ContosoItemReceivedEventData("Contoso Item SKU #1"),
            "Contoso.Items.ItemReceived",
            DateTime.now(),
            "2.0"
        ).withTopic("myTopic"));
    }
```

In 4.x, this remains the same except that you use `setTopic` instead of `withTopic`.
```java
List<EventGridEvent> eventsList = new ArrayList<>();
for (int i = 0; i < 5; i++) {
    eventsList.add(new EventGridEvent(
        String.format("Door%d", i),
        "Contoso.Items.ItemReceived",
        BinaryData.fromObject(new ContosoItemReceivedEventData("Contoso Item SKU #1")),
        "2.0"
    ).setTopic("myTopic"));
}
```

#### Deserialize events and data
In 1.x, to consume events, you register the event types in the map and deserialize events.
```java
EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
eventGridSubscriber.putCustomEventMapping("Contoso.Items.ItemReceived", ContosoItemSentEventData.class);
EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(eventGridJsonString);
EventGridEvent event = events[0];
if (event.data() instanceof ContosoItemReceivedEventData){
    ContosoItemReceivedEventData eventData=(ContosoItemReceivedEventData)event.data();
    // do something
}
```

In 4.x, you use `EventGridEvent.fromString` convenience method to deserialize `EventGridEvent` from a Json string. Then you can use the `getData()` method
to get the event's data in a `BinaryData`, from which you can use method `toObject()` to deserialize to the data, or use `toString()` and `toBytes()`.
```java
List<EventGridEvent> eventGridEvents = EventGridEvent.fromString(eventGridJsonString);
EventGridEvent event = events.get(0);
if ("Contoso.Items.ItemReceived".equals(event.getType()) {
    ContosoItemReceivedEventData eventData = event.getData().toObject(ContosoItemReceivedEventData.class);
}
```

#### Handle System Events
Handling System Events is similar to handling other events like above except that there are pre-defined System Event Data classes and event types mapping in both v1.x and v4.x. 
In 1.x,
```java
EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(eventGridJsonString);
EventGridEvent event = events[0];
if (event.data() instanceof StorageBlobDeletedEventData) {
    StorageBlobDeletedEventData eventData=(StorageBlobDeletedEventData)event.data();
}
```

In 4.x, you check event types:
```java
EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(eventGridJsonString);
EventGridEvent event = events.get(0);
if (SystemEventNames.STORAGE_BLOB_DELETED.equals(event.getEventType())) {
    StorageBlobDeletedEventData eventData = event.getData().toObject(StorageBlobDeletedEventData.class);
}
```

## Additional samples

More examples can be found at:

- [Event Grid samples][README-Samples]

<!-- Links -->
[EventGridPublisherClientBuilder]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-messaging-eventgrid/latest/com/azure/messaging/eventgrid/EventGridPublisherClientBuilder.html
[EventGridPublisherClient]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-messaging-eventgrid/latest/com/azure/messaging/eventgrid/EventGridPublisherClient.html
[EventGridPublisherAsyncClient]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-messaging-eventgrid/latest/com/azure/messaging/eventgrid/EventGridPublisherAsyncClient.html
[EventGridEvent]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-messaging-eventgrid/latest/com/azure/messaging/eventgrid/EventGridEvent.html
[Guidelines]: https://azure.github.io/azure-sdk/general_introduction.html
[GuidelinesJava]: https://azure.github.io/azure-sdk/java_introduction.html
[GuidelinesJavaDesign]: https://azure.github.io/azure-sdk/java_introduction.html#namespaces
[project-reactor]: https://projectreactor.io/
[README-Samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventgrid/azure-messaging-eventgrid/src/samples/java/README.md
[README]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventgrid/azure-messaging-eventgrid/README.md

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Feventgrid%2Fazure-messaging-eventgrid%2Fmigration-guide.png)
