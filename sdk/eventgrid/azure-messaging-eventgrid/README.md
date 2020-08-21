# Azure Event Grid

This project provides client tools or utilities in Java that make it easy to interact with [Azure Event Grid](https://azure.com/eventgrid). For documentation please see the Microsoft Azure [Java Developer Center](https://azure.microsoft.com/develop/java/) or the [JavaDocs](https://azure.github.io/azure-sdk-for-java/).

Azure Event Grid is a fully-managed intelligent event routing service that allows for uniform event consumption using a publish-subscribe model.

## Getting Started

### Latest stable release

To get the binaries of the official Microsoft Azure Event Grid Java SDK as distributed by Microsoft, ready for use within your project, you can use Maven.

[//]: # ({x-version-update-start;com.azure:azure-messaging-eventgrid;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventgrid</artifactId>
    <version>2.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites

- [Java Development Kit (JDK) with version 8 or above][jdk]
- [Azure subscription][azure_subscription]
- [Maven][maven]

### Creating a topic

Check out this [quickstart][custom_topic_portal_qs] to create a custom event topic using azure portal,
or [this one][custom_topic_cli_qs] to use Azure CLI.

### Creating the Client

In order to start sending events, we need an `EventGridPublisherClient`. Here is code to 
create the synchronous and the asynchronous versions. You can obtain the key and endpoint 
from the portal or Azure CLI from the quick starts above.

<!-- embedme ./src/samples/java/com/azure/messaging/eventgrid/ReadmeSamples.java#L19-L22 -->
```java
EventGridPublisherClient egClient = new EventGridPublisherClientBuilder()
    .endpoint(endpoint)
    .keyCredential(new AzureKeyCredential(key))
    .buildClient();
```

or

<!-- embedme ./src/samples/java/com/azure/messaging/eventgrid/ReadmeSamples.java#L31-L34 -->
```java
EventGridPublisherAsyncClient egAsyncClient = new EventGridPublisherClientBuilder()
    .endpoint(endpoint)
    .keyCredential(new AzureKeyCredential(key))
    .buildAsyncClient();
```

### Sending Events

Events can be sent in the `EventGridEvent` or `CloudEvent` schema, as detailed in the Key Concepts below.
For now, we can send the events to the topic using whatever format the topic was set to.

#### `EventGridEvent`
<!-- embedme ./src/samples/java/com/azure/messaging/eventgrid/ReadmeSamples.java#L38-L41 -->
```java
List<EventGridEvent> events = new ArrayList<>();
events.add(
    new EventGridEvent("exampleSubject", "Com.Example.ExampleEventType", "1")
        .setData("Example Data")
);

egClient.sendEvents(events);
```

#### `CloudEvent`
<!-- embedme ./src/samples/java/com/azure/messaging/eventgrid/ReadmeSamples.java#L48-L54 -->
```java
List<CloudEvent> events = new ArrayList<>();
events.add(
    new CloudEvent("com/example/source", "Com.Example.ExampleEventType")
        .setData("Example Data")
);

egClient.sendCloudEvents(events);
```

## Key Concepts 

Events can be sent or received in either the `CloudEvent` or the `EventGridEvent` 
format, depending on the Event Grid topic.

`EventGridEvent`: See specifications and requirements [here](https://docs.microsoft.com/en-us/azure/event-grid/event-schema).

`CloudEvent`: See the Cloud Event specification [here](https://github.com/cloudevents/spec)
and the Event Grid service summary of Cloud Events [here](https://docs.microsoft.com/en-us/azure/event-grid/cloud-event-schema).

Both classes can be used to consume events from a JSON payload, and can be constructed and sent
for publishing, using a `PublisherClient`

## Examples

### Recieving and Consuming Events

Events can be sent to a variety of locations, including Azure services such as ServiceBus
or external endpoints such as a WebHook endpoint. However, currently all events will be 
sent as encoded JSON data. Here is some basic code that details the handling 
of an event. Again, the handling is different based on the event schema being recieved
from the topic/subscription.

#### `EventGridEvent`
<!-- embedme ./src/samples/java/com/azure/messaging/eventgrid/ReadmeSamples.java#L58-L75 -->
```java
List<EventGridEvent> events = EventGridEvent.parse(jsonData);

for (EventGridEvent event : events) {
    // system event data will be turned into it's rich object,
    // while custom event data will be turned into a byte[].
    Object data = event.getData();

    // this event type goes to any non-azure endpoint (such as a WebHook) when the subscription is created.
    if (data instanceof SubscriptionValidationEventData) {
        SubscriptionValidationEventData validationData = (SubscriptionValidationEventData) data;
        System.out.println(validationData.getValidationCode());
    } else if (data instanceof byte[]) {
        // we can turn the data into the correct type by calling this method.
        // since we set the data as a string when sending, we pass the String class in to get it back.
        String stringData = event.getData(String.class);
        System.out.println(stringData); // "Example Data"
    }
}
```

#### `CloudEvent`
<!-- embedme ./src/samples/java/com/azure/messaging/eventgrid/ReadmeSamples.java#L79-L96 -->
```java
List<CloudEvent> events = CloudEvent.parse(jsonData);

for (CloudEvent event : events) {
    // system event data will be turned into it's rich object,
    // while custom event data will be turned into a byte[].
    Object data = event.getData();

    // this event type goes to any non-azure endpoint (such as a WebHook) when the subscription is created.
    if (data instanceof SubscriptionValidationEventData) {
        SubscriptionValidationEventData validationData = (SubscriptionValidationEventData) data;
        System.out.println(validationData.getValidationCode());
    } else if (data instanceof byte[]) {
        // we can turn the data into the correct type by calling this method.
        // since we set the data as a string when sending, we pass the String class in to get it back.
        String stringData = event.getData(String.class);
        System.out.println(stringData); // "Example Data"
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

### Help and Issues

If you encounter any bugs with these SDKs, please file issues via [Issues](https://github.com/Azure/azure-sdk-for-java/issues) or checkout [StackOverflow for Azure Java SDK](http://stackoverflow.com/questions/tagged/azure-java-sdk).

## Next Steps

- [Javadoc](https://azure.github.io/azure-sdk-for-java/)
- [Azure Java SDKs](https://docs.microsoft.com/java/azure/)
- If you don't have a Microsoft Azure subscription you can get a FREE trial account [here](http://go.microsoft.com/fwlink/?LinkId=330212)


## Contributing

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](http://azure.github.io/guidelines.html).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

---

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

<!-- LINKS -->
[jdk]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[api_documentation]: https://aka.ms/java-docs
[azure_subscription]: https://azure.microsoft.com/free
[maven]: https://maven.apache.org/
[custom_topic_portal_qs]: https://docs.microsoft.com/en-us/azure/event-grid/custom-event-quickstart-portal
[custom_topic_cli_qs]: https://docs.microsoft.com/en-us/azure/event-grid/custom-event-quickstart
[HttpResponseException]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/exception/HttpResponseException.java
[samples]: ./src/samples/java/com/azure/messaging/eventgrid

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Feventgrid%2Fazure-messaging-eventgrid%2FREADME.png)
