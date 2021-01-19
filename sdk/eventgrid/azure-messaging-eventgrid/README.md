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
using the following command in [Azure CLI][cli].

```bash
az eventgrid topic show --name <your-resource-name> --resource-group <your-resource-group-name> --query "key"
```

#### Creating the Client

Once you have your access key and topic endpoint, you can create the publisher client as follows:

<!-- embedme ./src/samples/java/com/azure/messaging/eventgrid/ReadmeSamples.java#L38-L41 -->
```java
EventGridPublisherClient egClient = new EventGridPublisherClientBuilder()
    .endpoint(endpoint)
    .credential(new AzureKeyCredential(key))
    .buildClient();
```
or
<!-- embedme ./src/samples/java/com/azure/messaging/eventgrid/ReadmeSamples.java#L45-L48 -->
```java
EventGridPublisherAsyncClient egAsyncClient = new EventGridPublisherClientBuilder()
    .endpoint(endpoint)
    .credential(new AzureKeyCredential(key))
    .buildAsyncClient();
```

If you have a **Shared Access Signature** that be used to send events to an Event Grid Topic or Domain for
limited time, you can use it to create the publisher client:
<!-- embedme ./src/samples/java/com/azure/messaging/eventgrid/ReadmeSamples.java#L52-L55 -->
```java
EventGridPublisherClient egClient = new EventGridPublisherClientBuilder()
    .endpoint(endpoint)
    .credential(new AzureSasCredential(key))
    .buildClient();
```
or
<!-- embedme ./src/samples/java/com/azure/messaging/eventgrid/ReadmeSamples.java#L59-L62 -->
```java
EventGridPublisherAsyncClient egClient = new EventGridPublisherClientBuilder()
    .endpoint(endpoint)
    .credential(new AzureSasCredential(key))
    .buildAsyncClient();
```

#### Create a SAS key for other people to send events for a limited period of time
If you'd like to allow other people to publish events to your Event Grid Topic or Domain for some time, you can create
a **Shared Access Signature** for them so they can create an `EventGridPublisherClient` like the above to use `AzureSasCredential`
to create the publisher client.

Here is sample code to produce a shared access signature that expires after 20 minutes:
<!-- embedme ./src/samples/java/com/azure/messaging/eventgrid/ReadmeSamples.java#L124-L126 -->
```java
OffsetDateTime expiration = OffsetDateTime.now().plusMinutes(20);
    
String sasToken = EventGridSharedAccessSingatureGenerator
    .generateSharedAccessSignature(endpoint, new AzureKeyCredential(key), expiration);
```

## Key concepts
For information about general Event Grid concepts: [Concepts in Azure Event Grid](https://docs.microsoft.com/azure/event-grid/concepts).

### Event Publishing
    
`EventGridPublisherClient` is used sending events to an Event Grid Topic or an Event Grid Domain.
`EventGridPublisherAsyncClient` is the async version of `EventGridPublisherClient`.

### Event Schemas

Event Grid supports multiple schemas for encoding events. When an Event Grid Topic or Domain is created, you specify the
schema that will be used when publishing events. While you may configure your topic to use a _custom schema_ it is
more common to use the already defined _Event Grid schema_ or _CloudEvents 1.0 schema_. 
[CloudEvents](https://cloudevents.io/) is a Cloud Native Computing Foundation project which produces a specification 
for describing event data in a common way. Regardless of what schema your topic or domain is configured to use, 
`EventGridPublisherClient` will be used to publish events to it. However, you must use the correct method for 
publishing:

| Event Schema       | Publishing Method     |
| ------------ | --------------------- |
| Event Grid Events  | `publishEvents`       |
| Cloud Events | `publishCloudEvents`  |
| Custom Events       | `publishCustomEvents` |

Using the wrong method will result in an error from the service and your events will not be published.

### Event Handlers and event deserialization.

EventGrid doesn't store any events in the Event Grid Topic or Domain itself. You need to [create subscriptions to the
EventGrid Topic or Domain](https://docs.microsoft.com/en-us/azure/event-grid/subscribe-through-portal). 
The events sent to the topic or domain will be stored into the subscription's endpoint, also known as 
["Event Handler"](https://docs.microsoft.com/en-us/azure/event-grid/event-handlers).

You may use the event handler's SDK to receive the events and then use the `EventGridDeserializer.parseCloudEvents()`
or `EventGridDeserializer.parseEventGridEvents()` of this SDK to deserialize the events, which are in JSON format.
The payload of the events can be in binary, String, or JSON data. 

## Examples

### Sending Events

Events can be sent in the `EventGridEvent`, `CloudEvent`, or a custom schema, as detailed in the Key Concepts above.
The topic or domain must be configured to accept the schema being sent. For simplicity,
the synchronous client is used for samples, however the asynchronous client has the same method names.

#### Sending `EventGridEvent`
<!-- embedme ./src/samples/java/com/azure/messaging/eventgrid/ReadmeSamples.java#L66-L70 -->
```java
List<EventGridEvent> events = new ArrayList<>();
events.add(new EventGridEvent("exampleSubject", "Com.Example.ExampleEventType", "Example Data", "1"));
egClient.sendEvents(events);
```

#### Sending `CloudEvent`
<!-- embedme ./src/samples/java/com/azure/messaging/eventgrid/ReadmeSamples.java#L74-L78 -->
```java
List<CloudEvent> events = new ArrayList<>();
events.add(new CloudEvent("com/example/source", "Com.Example.ExampleEventType", "Example Data"));
egClient.sendCloudEvents(events);
```

#### Sending Custom Events

To send custom events in any defined schema, use the `sendCustomEvents` method
on the `PublisherClient`.

### Recieving and Consuming Events

Events can be sent to a variety of event handlers, including ServiceBus
EventHubs, Blob Storage Queue, WebHook endpoint, or many other supported Azure Services.
However, currently all events will be sent as encoded JSON data. Here is some basic code that details the deserialization 
of events after they're received by the event handlers. Again, the handling is different based on the event schema being received
from the topic/subscription.

#### Deserialize `EventGridEvent`
<!-- embedme ./src/samples/java/com/azure/messaging/eventgrid/ReadmeSamples.java#L82-L99 -->
```java
List<EventGridEvent> events = EventGridDeserializer.deserializeEventGridEvents(jsonData);

for (EventGridEvent event : events) {
    if (event.isSystemEvent()) {
        Object systemEventData = event.asSystemEventData();
        if (systemEventData instanceof SubscriptionValidationEventData) {
            SubscriptionValidationEventData validationData = (SubscriptionValidationEventData) systemEventData;
            System.out.println(validationData.getValidationCode());
        }
    }
    else {
        BinaryData binaryData = event.getData();
        // we can turn the data into the correct type by calling BinaryData.toString(), BinaryData.toObject(), 
        // or BinaryData.toBytes(). This sample uses toString.
        if (binaryData != null) {
            System.out.println(binaryData.toString()); // "Example Data"
        }
    }
}

```

#### Deserialize `CloudEvent`
<!-- embedme ./src/samples/java/com/azure/messaging/eventgrid/ReadmeSamples.java#L103-L120 -->
```java
public void deserializeCloudEvent() {
    List<CloudEvent> events = EventGridDeserializer.deserializeCloudEvents(jsonData);
    for (CloudEvent event : events) {
        if (event.isSystemEvent()) {
            Object systemEventData = event.asSystemEventData();
            if (systemEventData instanceof SubscriptionValidationEventData) {
                SubscriptionValidationEventData validationData = (SubscriptionValidationEventData) systemEventData;
                System.out.println(validationData.getValidationCode());
            }
        }
        else {
            // we can turn the data into the correct type by calling BinaryData.toString(), BinaryData.toObject(), 
            // or BinaryData.toBytes(). This sample uses toString.
            BinaryData binaryData = event.getData();
            if (binaryData != null) {
                System.out.println(binaryData.toString()); // "Example Data"
            }
        }
    }
}
```

Some additional sample code can be found [here][samples]. 
Be sure to check back for more samples in the future.

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
[samples]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/eventgrid/azure-messaging-eventgrid/src/samples/java/com/azure/messaging/eventgrid
[eventgrid]: https://azure.com/eventgrid
[portal]: https://ms.portal.azure.com/
[cli]: https://docs.microsoft.com/cli/azure
[service_docs]: https://docs.microsoft.com/azure/event-grid/
[sources]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/eventgrid/azure-messaging-eventgrid/src
[EventGridEvent]: https://docs.microsoft.com/azure/event-grid/event-schema
[CloudEvent]: https://github.com/cloudevents/spec/blob/master/spec.md


![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Feventgrid%2Fazure-messaging-eventgrid%2FREADME.png)
