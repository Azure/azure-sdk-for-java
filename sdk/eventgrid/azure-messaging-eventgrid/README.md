# Azure Event Grid client library for Java

Azure Event Grid allows you to easily build applications with event-based architectures. The Event Grid service fully 
manages all routing of events from any source, to any destination, for any application. 
Azure service events and custom events can be published directly to the service, where the events can then be filtered 
and sent to various recipients, such as built-in handlers or custom webhooks. 
To learn more about Azure Event Grid: [What is Event Grid?](https://docs.microsoft.com/azure/event-grid/overview)

Use the client library for Azure Event Grid to:
- Publish events to the Event Grid service using the Event Grid Event, Cloud Event 1.0, or custom schemas
- Consume events that have been delivered to event handlers
- Generate SAS tokens to authenticate the client publishing events to Azure Event Grid topics
[Sources][sources] |
[API Reference Documentation][javadocs] |
[Product Documentation][service_docs] | 
[Samples][samples]


## Getting started

### Prerequisites

- [Java Development Kit (JDK) with version 8 or above][jdk]
- An [Azure subscription][azure_subscription]
- An [Event Grid][eventgrid] topic or domain. To create the resource, you can use [Azure portal][portal] or
  [Azure CLI][cli]

If you use the Azure CLI, replace `<your-resource-group-name>` and `<your-resource-name>` with your own unique names
and `<location>` with a valid Azure service location.

#### Creating a topic ([Azure CLI][cli])

```bash
az eventgrid topic create --location <location> --resource-group <your-resource-group-name> --name <your-resource-name>
```


#### Creating a domain ([Azure CLI][cli])

```bash
az eventgrid domain create --location <location> --resource-group <your-resource-group-name> --name <your-resource-name>
```

### Include the package
[//]: # ({x-version-update-start;com.azure:azure-messaging-eventgrid;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventgrid</artifactId>
    <version>2.0.0-beta.4</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authenticate the Client

In order to send events, we need an endpoint to send to and some authentication for the endpoint, either as a 
key credential or a shared access signature.
The endpoint and key can both be obtained through [Azure Portal][portal] or [Azure CLI][cli].

#### Endpoint

The endpoint is listed on the dashboard of the topic or domain in the [Azure Portal][portal],
or can be obtained using the following command in [Azure CLI][cli].
```bash
az eventgrid topic show --name <your-resource-name> --resource-group <your-resource-group-name> --query "endpoint"
```

#### Access Key

The keys are listed in the "Access Keys" tab of the [Azure Portal][portal], or can be obtained
using the following command in [Azure CLI][cli]. Anyone of the keys listed will work.

```bash
az eventgrid topic key list --name <your-resource-name> --resource-group <your-resource-group-name>
```

#### Creating the Client

##### Using endpoint and access key to create the client
Once you have your access key and topic endpoint, you can create the publisher client as follows:

Sync client that works for every Java developer:
```java
EventGridPublisherClient egClient = new EventGridPublisherClientBuilder()
    .endpoint("<your event grid endpoint>")
    .credential(new AzureKeyCredential("<your event grid access key>"))
    .buildClient();
```
or async client if your technology stack has reactive programming such as project reactor:
```java
EventGridPublisherAsyncClient egAsyncClient = new EventGridPublisherClientBuilder()
    .endpoint("<your event grid endpoint>")
    .credential(new AzureKeyCredential("<your event grid access key>"))
    .buildAsyncClient();
```

##### Using endpoint and SAS token to create the client
If you have a SAS (**Shared Access Signature**) that can be used to send events to an Event Grid Topic or Domain for
limited time, you can use it to create the publisher client:

Sync client:
```java
EventGridPublisherClient egClient = new EventGridPublisherClientBuilder()
    .endpoint("<your event grid endpoint>")
    .credential(new AzureSasCredential("<your sas token for this event grid point>"))
    .buildClient();
```
Async client:
```java
EventGridPublisherAsyncClient egClient = new EventGridPublisherClientBuilder()
    .endpoint("<your event grid endpoint>")
    .credential(new AzureSasCredential("<your sas token for this event grid point>"))
    .buildAsyncClient();
```

#### Create a SAS token for other people to send events for a limited period of time
If you'd like to give permission to other people to publish events to your Event Grid Topic or Domain for some time, you can create
a SAS (**Shared Access Signature**) for them so they can create an `EventGridPublisherClient` like the above to use `AzureSasCredential`
to create the publisher client.

Here is sample code to create a shared access signature that expires after 20 minutes:
```java
OffsetDateTime expiration = OffsetDateTime.now().plusMinutes(20);
    
String sasToken = EventGridSasGenerator
    .generateSas("<your event grid endpoint>", new AzureKeyCredential("<your event grid access key>"), expiration);
```

## Key concepts
For information about general Event Grid concepts: [Concepts in Azure Event Grid](https://docs.microsoft.com/azure/event-grid/concepts).

### EventGridPublisherClient
    
`EventGridPublisherClient` is used sending events to an Event Grid Topic or an Event Grid Domain.
`EventGridPublisherAsyncClient` is the async version of `EventGridPublisherClient`.

### Event Schemas

Event Grid supports multiple schemas for encoding events. When an Event Grid Topic or Domain is created, you specify the
schema that will be used when publishing events. While you may configure your topic to use a _custom schema_ it is
more common to use the already defined [EventGridEvent schema](https://docs.microsoft.com/azure/event-grid/event-schema) or [CloudEvent schema](https://docs.microsoft.com/azure/event-grid/cloud-event-schema). 
CloudEvent is a Cloud Native Computing Foundation project which produces a specification for describing event data in a common way.
Event Grid service is compatible with the [CloudEvent specification](https://cloudevents.io/)
Regardless of what schema your topic or domain is configured to use, 
`EventGridPublisherClient` will be used to publish events to it. However, you must use the correct method for 
publishing:

| Event Schema       | Publishing Method     |
| ------------ | --------------------- |
| Event Grid Events  | `sendEventGridEvents`       |
| Cloud Events | `sendCloudEvents`  |
| Custom Events       | `sendCustomEvents` |

Using the wrong method will result in a BadRequest error from the service and your events will not be published.
Use this Azure CLI command to query which schema an Event Grid Topic or Domain accepts:
```bash
az eventgrid topic show --name <your-resource-name> --resource-group <your-resource-group-name> --query inputSchema
```

### Event Handlers and event deserialization.

EventGrid doesn't store any events in the Event Grid Topic or Domain itself. You need to [create subscriptions to the
EventGrid Topic or Domain](https://docs.microsoft.com/azure/event-grid/subscribe-through-portal). 
The events sent to the topic or domain will be stored into the subscription's endpoint, also known as 
["Event Handler"](https://docs.microsoft.com/azure/event-grid/event-handlers).

You may use the event handler's SDK to receive the events in Json String and then use the `EventGridEvent.fromString()` or `CloudEvent.fromString()`
deserialize the events. The data part of the events can be in binary, String, or JSON data. 

## Examples

### Sending Events To Event Grid Topics

Events can be sent in the `EventGridEvent`, `CloudEvent`, or a custom schema, as detailed in [Event Schemas](#event-schemas).
The topic or domain must be configured to accept the schema being sent. For simplicity,
the synchronous client is used for samples, however the asynchronous client has the same method names.

Note: figure out what schema (cloud event, event grid event, or custom event) the event grid topic accepts before you start sending.
#### Sending `EventGridEvent` to a topic that accepts EventGridEvent schema
```java
// Make sure that the event grid topic or domain you're sending to accepts EventGridEvent schema.
List<EventGridEvent> events = new ArrayList<>();
User user = new User("John", "James");
events.add(new EventGridEvent("exampleSubject", "Com.Example.ExampleEventType", user, "v1"));
egClient.sendEventGridEvents(events);
```

#### Sending `CloudEvent` to a topic that accepts CloudEvent schema
```java
// Make sure that the event grid topic or domain you're sending to accepts CloudEvent schema.
List<CloudEvent> events = new ArrayList<>();
User user = new User("John", "James");
events.add(new CloudEvent("https://source.example.com", "Com.Example.ExampleEventType", user));
egClient.sendCloudEvents(events);
```

#### Sending Custom Events to a topic that accepts custom event schema

To send custom events in any defined schema, use the `sendCustomEvents` method
on the `PublisherClient`.

### Sending Events To Event Grid Domain

An [Event Grid Domain](https://docs.microsoft.com/azure/event-grid/event-domains) can have thousands of topics
but has a single endpoint. You can use a domain to manage a set of related topics. Sending events to the topics of
an Event Grid Domain is the same as sending events to a regular Event Grid Topic except that you need to 
specify the `topic` of an `EventGridEvent` if the domain accepts `EventGridEvent` schema.
```java
List<EventGridEvent> events = new ArrayList<>();
User user = new User("John", "James");
events.add(new EventGridEvent("com/example", "Com.Example.ExampleEventType", user, "1")
    .setTopic("yourtopic"));  // Set it only when you send to an event grid domain.
egClient.sendEventGridEvents(events);
```

If the domain accepts `CloudEvent` schema, the CloudEvent's attribute that is configured to map the `topic` when the 
domain is created must be set. The default mapping attribute is `source`.

### Recieving and Consuming Events
The Event Grid service doesn't store events. So this Event Grid SDK doesn't have an event receiver.
Instead, events are stored in the [Event Handlers](#event-handlers-and-event-deserialization), including ServiceBus, EventHubs, Storage Queue, WebHook endpoint, or many other supported Azure Services.
However, currently all events will be sent and stored as encoded JSON data. Here is some basic code that details the deserialization 
of events after they're received by the event handlers. Again, the handling is different based on the event schema being received
from the topic/subscription.

#### Deserialize `EventGridEvent` or `CloudEvent` from a Json String
The Json String can have a single event or an array of events. The returned result is a list of events.
```java
// Deserialize an EventGridEvent
String eventGridEventJsonData = "your EventGridEvent json String";
List<EventGridEvent> events = EventGridEvent.fromString(eventGridEventJsonData);

// Deserialize a CloudEvent
String cloudEventJsonData = "your CloudEvent json String";
List<CloudEvent> events = CloudEvent.fromString(cloudEventJsonData);
```

#### Deserialize data from a `CloudEvent` or `EventGridEvent`
Once you deserialize the `EventGridEvent` or `CloudEvent` from a Json String, you can use `getData()` of 
`CloudEvent` or `EventGridEvent` to get the payload of the event. It returns a `BinaryData`
object, which has methods to further deserialize the data into usable types:
- `BinaryData.toBytes()` gets the data as a byte[]
- `BinaryData.toString()` gets the data as a String
- `BinaryData.toObject()` gets the data as an object of a specific type. It uses Json deserializer by default. It has
  an overload to accept your deserializer if you want to use your own.

```java
BinaryData eventData = eventGridEvent.getData();

//Deserialize data to a model class
User dataInModelClass = eventData.toObject(TypeReference.createInstance(User.class));

//Deserialize data to a Map
Map<?, ?> dataMap = eventData.toObject(TypeReference.createInstance(Map.class));

//Deserialize data to a String
String dataString = eventData.toString();

//Deserialize data to byte array (byte[])
byte[] dataInBytes = eventData.toBytes();
```  

#### Deserialize system event data from `CloudEvent` or `EventGridEvent`
An event that is sent to a [System Topic](https://docs.microsoft.com/azure/event-grid/system-topics) is called a
System Topic Event, or System Event. 
A system topic in Event Grid represents events published by an [Event Source](https://docs.microsoft.com/azure/event-grid/overview#event-sources) like Azure Storage, Azure Event Hubs, App Configuration and so on.
An example is when a blob is created, a system event with event type "Microsoft.Storage.BlobCreated" is sent to the configured System Topic. 
The system event class for this event type is `StorageBlobCreatedEventData` defined in package `com.azure.messaging.eventgrid.systemevents`.
EventGrid has system events for:
- [Azure App Configuration](https://docs.microsoft.com/azure/event-grid/event-schema-app-configuration)
- [Azure App Service](https://docs.microsoft.com/azure/event-grid/event-schema-app-service)
- [Azure Blob Storage](https://docs.microsoft.com/azure/event-grid/event-schema-blob-storage)
- ...
- Refer to [Azure services that support system topics](https://docs.microsoft.com/azure/event-grid/system-topics#azure-services-that-support-system-topics) for many other services.
- Refer to package `com.azure.messaging.eventgrid.systemevents` for the related system event classes

You can't send a System Event to a System Topic by using this SDK.

Receiving and consuming system events is the same as other events. Additionally, a set of model classes
for the various system event data are defined in package `com.azure.messaging.eventgrid.systemevents`. You can do the 
following after you deserialize an event by using `EventGridEvent.fromString()` or `CloudEvent.fromString()`:
- look up the system event data model class that the System Event data can be deserialized to;
```java
    Class<?> eventDataClazz = SystemEventNames.getSystemEventMappings().get(event.getEventType());
```
- deserialize a system event's data to a model class instance like deserializing any other event data;
```java
    // Deserialize the event data to an instance of a specific System Event data class type
    BinaryData data = event.getData();
    if (data != null) {
        StorageBlobCreatedEventData blobCreatedData = data.toObject(TypeReference.createInstance(StorageBlobCreatedEventData.class));
        System.out.println(blobCreatedData.getUrl());
    }
```
- deal with multiple event types.
```java
    List<EventGridEvent> eventGridEvents = EventGridEvent.fromString(eventGridJsonString);
    for (EventGridEvent eventGridEvent : eventGridEvents) {
        BinaryData data = eventGridEvent.getData();
        switch (eventGridEvent.getEventType()) {
            case SystemEventNames.APP_CONFIGURATION_KEY_VALUE_DELETED:
                AppConfigurationKeyValueDeletedEventData keyValueDeletedEventData =
                    data.toObject(TypeReference.createInstance(AppConfigurationKeyValueDeletedEventData.class));
                System.out.println("Processing the AppConfigurationKeyValueDeletedEventData...");
                System.out.printf("The key is: %s%n", keyValueDeletedEventData.getKey());
                break;
            case SystemEventNames.APP_CONFIGURATION_KEY_VALUE_MODIFIED:
                AppConfigurationKeyValueModifiedEventData keyValueModifiedEventData =
                    data.toObject(TypeReference.createInstance(AppConfigurationKeyValueModifiedEventData.class));
                System.out.println("Processing the AppConfigurationKeyValueModifiedEventData...");
                System.out.printf("The key is: %s%n", keyValueModifiedEventData.getKey());
                break;
            default:
                System.out.printf("%s isn't an AppConfiguration event data%n", eventGridEvent.getEventType());
                break;
        }
    }
```

### More samples
Some additional sample code can be found [here][samples].

## Troubleshooting

### Responses and error codes

Service responses are returned in the form of Http status codes, including a number
of error codes. These codes can optionally be returned by the `PublisherClient`.
Unexpected status codes are thrown as [`HttpResponseException`][HttpResponseException] 
which wraps the error code.

Reference documentation for the event grid service can be found [here][service_docs]. This is a
good place to start for problems involving configuration of topics/endpoints, as well as for
problems involving error codes from the service.

### Help and Issues

Reference documentation for the SDK can be found [here][javadocs]. This is a good first step
to understanding the purpose of each method called, as well as possible reasons for errors
or unexpected behavior.

If you encounter any bugs with these SDKs, please file issues via [Issues](https://github.com/Azure/azure-sdk-for-java/issues) or checkout [StackOverflow for Azure Java SDK](https://stackoverflow.com/questions/tagged/azure-java-sdk).

## Next steps

- [Azure Java SDKs](https://docs.microsoft.com/java/azure/)
- If you don't have a Microsoft Azure subscription you can get a FREE trial account [here](https://go.microsoft.com/fwlink/?LinkId=330212)
- Some additional sample code can be found [here][samples]
- Additional Event Grid tutorials can be found [here][service_docs]

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/master/CONTRIBUTING.md).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

---

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

<!-- LINKS -->
[jdk]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[javadocs]: https://azure.github.io/azure-sdk-for-java/eventgrid.html
[azure_subscription]: https://azure.microsoft.com/free
[maven]: https://maven.apache.org/
[HttpResponseException]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/exception/HttpResponseException.java
[samples]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/eventgrid/azure-messaging-eventgrid/src/samples/java/com/azure/messaging/eventgrid
[eventgrid]: https://azure.com/eventgrid
[portal]: https://ms.portal.azure.com/
[cli]: https://docs.microsoft.com/cli/azure
[service_docs]: https://docs.microsoft.com/azure/event-grid/
[sources]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/eventgrid/azure-messaging-eventgrid/src
[EventGridEvent]: https://docs.microsoft.com/azure/event-grid/event-schema
[CloudEvent]: https://github.com/cloudevents/spec/blob/master/spec.md


![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Feventgrid%2Fazure-messaging-eventgrid%2FREADME.png)
