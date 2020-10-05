# Azure Event Grid client library for Java

This project provides client tools or utilities in Java that make it easy to interact with [Azure Event Grid][eventgrid].

Azure Event Grid is a fully-managed intelligent event routing service that provides reliable and scalable event delivery.

The client library can be used to:

- Publish events in the Event Grid, Cloud Event (1.0), or custom schema
- Decode and process events and event data at the event destination
- Generate shared access signatures that connect to an event topic

[Sources][sources] |
[API Reference Documentation][javadocs] |
[Product Documentation][service_docs] | 
[Samples][samples]


## Getting started

### Install the Package

To get the binaries of the official Microsoft Azure Event Grid Java SDK as distributed by Microsoft, ready for use 
within your project, you can use [Maven][maven].

[//]: # ({x-version-update-start;com.azure:azure-messaging-eventgrid;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventgrid</artifactId>
    <version>2.0.0-beta.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

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

### Authenticate the Client

In order to send events, we need an endpoint to send to and some authentication for the endpoint, either as a 
key credential or a shared access signature (which will in turn need an endpoint and key).
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

#### Creating a shared access signature

A shared access signature is an alternative way to authenticate requests to an [Event Grid][eventgrid]
topic or domain. They behave similarly to keys, and require a key to produce, but can be configured
with an expiration time, so they can be used to restrict access to a topic or domain.
Here is sample code to produce a shared access signature that expires after 20 minutes:

<!-- embedme ./src/samples/java/com/azure/messaging/eventgrid/ReadmeSamples.java#L104-L107 -->
```java
OffsetDateTime expiration = OffsetDateTime.now().plusMinutes(20);
String credentialString = EventGridSasCredential
    .createSas(endpoint, expiration, new AzureKeyCredential(key));
EventGridSasCredential signature = new EventGridSasCredential(credentialString);
```

#### Creating the Client

In order to start sending events, we need an `EventGridPublisherClient`. Here is code to 
create the synchronous and the asynchronous versions. Note that a shared access signature can
be used instead of a key in any of these samples by calling the `sharedAccessSignatureCredential` 
method instead of `keyCredential`. 


<!-- embedme ./src/samples/java/com/azure/messaging/eventgrid/ReadmeSamples.java#L28-L31 -->
```java
EventGridPublisherClient egClient = new EventGridPublisherClientBuilder()
    .endpoint(endpoint)
    .credential(new AzureKeyCredential(key))
    .buildClient();
```

or

<!-- embedme ./src/samples/java/com/azure/messaging/eventgrid/ReadmeSamples.java#L35-L38 -->
```java
EventGridPublisherAsyncClient egAsyncClient = new EventGridPublisherClientBuilder()
    .endpoint(endpoint)
    .credential(new AzureKeyCredential(key))
    .buildAsyncClient();
```

## Key concepts 

### EventGridPublisherClient
    
`EventGridPublisherClient` is used sending events to an Event Grid Topic or an Event Grid Domain.

### Event Schemas

Event Grid supports multiple schemas for encoding events. When a Custom Topic or Domain is created, you specify the
schema that will be used when publishing events. While you may configure your topic to use a _custom schema_ it is
more common to use the already defined _Event Grid schema_ or _CloudEvents 1.0 schema_. 
[CloudEvents](https://cloudevents.io/) is a Cloud Native Computing Foundation project which produces a specification 
for describing event data in a common way. Regardless of what schema your topic or domain is configured to use, 
`EventGridPublisherClient` will be used to publish events to it. However, you must use the correct method for 
publishing:

| Schema       | Publishing Method     |
| ------------ | --------------------- |
| Event Grid   | `publishEvents`       |
| Cloud Events | `publishCloudEvents`  |
| Custom       | `publishCustomEvents` |

Using the wrong method will result in an error from the service and your events will not be published.

### Consumption

Events can be consumed from the event destination using respective schema class' `parse` method, passing the JSON string 
received from the destination.

## Examples

### Sending Events

Events can be sent in the `EventGridEvent`, `CloudEvent`, or a custom schema, as detailed in the Key Concepts above.
The topic or domain must be configured to accept the schema being sent. For simplicity,
the synchronous client is used for samples, however the asynchronous client has the same method names.

#### `EventGridEvent`
The `EventGridEvent` model has 3 required properties to set:
- the subject, similar to a title or subject line of an email
- the event type, defining what kind of event happened
- the data version, identifying the current version of the data being sent

These are set in the constructor, and a variety of other properties can be optionally set or overridden.
Learn more [here][EventGridEvent].
<!-- embedme ./src/samples/java/com/azure/messaging/eventgrid/ReadmeSamples.java#L42-L48 -->
```java
List<EventGridEvent> events = new ArrayList<>();
events.add(
    new EventGridEvent("exampleSubject", "Com.Example.ExampleEventType", "Example Data",
        "1")
);

egClient.sendEvents(events);
```

#### `CloudEvent`
The `CloudEvent` model has 2 required properties to set:
- the source, a URI identifying the source of the event
- the type, defining what kind of event happened. Note that this aligns with the "event type" of `EventGridEvent`

These are set in the constructor, and a variety of other properties can be optionally set or overridden.
Learn more [here][CloudEvent].
<!-- embedme ./src/samples/java/com/azure/messaging/eventgrid/ReadmeSamples.java#L52-L58 -->
```java
List<CloudEvent> events = new ArrayList<>();
events.add(
    new CloudEvent("com/example/source", "Com.Example.ExampleEventType")
        .setData("Example Data")
);

egClient.sendCloudEvents(events);
```

#### Custom Events

To send custom events in any defined schema, use the `sendCustomEvents` method
on the `PublisherClient`.

### Recieving and Consuming Events

Events can be sent to a variety of locations, including Azure services such as ServiceBus
or external endpoints such as a WebHook endpoint. However, currently all events will be 
sent as encoded JSON data. Here is some basic code that details the handling 
of an event. Again, the handling is different based on the event schema being received
from the topic/subscription.

#### `EventGridEvent`
<!-- embedme ./src/samples/java/com/azure/messaging/eventgrid/ReadmeSamples.java#L62-L79 -->
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
<!-- embedme ./src/samples/java/com/azure/messaging/eventgrid/ReadmeSamples.java#L83-L100 -->
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

Reference documentation for the event grid service can be found [here][service_docs]. This is a
good place to start for problems involving configuration of topics/endpoints, as well as for
problems involving error codes from the service.

### Help and Issues

Reference documentation for the SDK can be found [here][javadocs]. This is a good first step
to understanding the purpose of each method called, as well as possible reasons for errors
or unexpected behavior.

If you encounter any bugs with these SDKs, please file issues via [Issues](https://github.com/Azure/azure-sdk-for-java/issues) or checkout [StackOverflow for Azure Java SDK](http://stackoverflow.com/questions/tagged/azure-java-sdk).

## Next steps

- [Azure Java SDKs](https://docs.microsoft.com/java/azure/)
- If you don't have a Microsoft Azure subscription you can get a FREE trial account [here](http://go.microsoft.com/fwlink/?LinkId=330212)
- Some additional sample code can be found [here][samples]
- Additional Event Grid tutorials can be found [here][service_docs]

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
