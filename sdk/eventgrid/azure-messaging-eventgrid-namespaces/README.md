# Azure EventGrid Namespaces client library for Java

Azure Event Grid  allows you to easily build applications with event-based architectures. The Event Grid service fully manages all routing of events from any source, to any destination, for any application. Azure service events and custom events can be published directly to the service, where the events can then be filtered and sent to various recipients, such as built-in handlers or custom webhooks. To learn more about Azure Event Grid: [What is Event Grid?](https://docs.microsoft.com/azure/event-grid/overview)

Use the client library for Azure Event Grid to:
- Publish events to Event Grid topics using the Cloud Event schema
- Consume and settle events

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]
- [Product documentation][product_documentation]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
  - Here are details about [Java 8 client compatibility with Azure Certificate Authority](https://learn.microsoft.com/azure/security/fundamentals/azure-ca-details?tabs=root-and-subordinate-cas-list#client-compatibility-for-public-pkis).
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-messaging-eventgrid-namespaces;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventgrid-namespaces</artifactId>
    <version>1.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

In order to send events, we need an endpoint to send to and authentication for the endpoint. The authentication can be
a key credential or an Entra ID credential. The endpoint and key can both be obtained through [Azure Portal][portal] or [Azure CLI][cli].

#### Endpoint

The endpoint is listed on the dashboard of the topic or domain in the [Azure Portal][portal],
or can be obtained using the following command in [Azure CLI][cli].
```bash
az eventgrid topic show --name <your-resource-name> --resource-group <your-resource-group-name> --query "endpoint"
```

#### Entra ID Token authentication
Azure Event Grid provides integration with Entra ID for identity-based authentication of requests.
With Entra ID, you can use role-based access control (RBAC) to grant access to your Azure Event Grid resources to users, groups, or applications.
To send events to a topic or domain with a `TokenCredential`, the authenticated identity should have the "EventGrid Data Sender" role assigned.

This authentication method is preferred.

```java com.azure.messaging.eventgrid.namespaces.TokenCredentialExample
EventGridSenderClient client = new EventGridSenderClientBuilder().endpoint("your endpoint")
    .topicName("your topic")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

#### Access Key

The keys are listed in the "Access Keys" tab of the [Azure Portal][portal], or can be obtained
using the following command in [Azure CLI][cli]. Anyone of the keys listed will work.
```bash
az eventgrid topic key list --name <your-resource-name> --resource-group <your-resource-group-name>
```

```java com.azure.messaging.eventgrid.namespaces.AccessKeyExample
EventGridSenderClient client = new EventGridSenderClientBuilder().endpoint("your endpoint")
    .topicName("your topic")
    .credential(new AzureKeyCredential("your access key"))
    .buildClient();
```



## Key concepts

### Event Grid Namespace

A **[namespace](https://learn.microsoft.com/azure/event-grid/concepts-event-grid-namespaces#namespaces)** is a management container for other resources. It allows for grouping of related resources in order to manage them under one subscription.

#### Namespace Topic

A **[namespace topic](https://learn.microsoft.com/azure/event-grid/concepts-event-grid-namespaces#namespace-topics)** is a topic that is created within an Event Grid namespace. The client publishes events to an HTTP namespace endpoint specifying a namespace topic where published events are logically contained. A namespace topic only supports the CloudEvent v1.0 schema.

#### Event Subscription

An **[event subscription](https://learn.microsoft.com/azure/event-grid/concepts-event-grid-namespaces#event-subscriptions)** is a configuration resource associated with a single topic.

## Examples

The following sections provide several code snippets covering some of the most common Event Grid tasks.

There are four relevant clients (and related builders)
- `EventGridSenderClient` - Used to send events to a topic
- `EventGridReceiverClient` - Used to consume events from a topic
- `EventGridSenderAsyncClient` - Used to send events to a topic asynchronously
- `EventGridReceiverAsyncClient` - Used to consume events from a topic asynchronously

Either the synchronous or asynchronous client can be used to send or receive events.

### Send an event
Sending an event is done by creating a `CloudEvent` and using the client to send it. An overload allows for sending multiple.
#### Synchronously
```java com.azure.messaging.eventgrid.namespaces.SendEventExample
User user = new User("John", "Doe");
CloudEvent cloudEvent
    = new CloudEvent("source", "type", BinaryData.fromObject(user), CloudEventDataFormat.JSON, "application/json");
client.send(cloudEvent);
```

```java com.azure.messaging.eventgrid.namespaces.SendMultipleEventsExample
User john = new User("John", "Doe");
User jane = new User("Jane", "Doe");
CloudEvent johnEvent
    = new CloudEvent("source", "type", BinaryData.fromObject(user), CloudEventDataFormat.JSON, "application/json");
CloudEvent janeEvent
    = new CloudEvent("source", "type", BinaryData.fromObject(user), CloudEventDataFormat.JSON, "application/json");
client.send(Arrays.asList(johnEvent, janeEvent));
```

#### Asynchronously
```java com.azure.messaging.eventgrid.namespaces.SendEventAsyncExample
User user = new User("John", "Doe");
CloudEvent cloudEvent
    = new CloudEvent("source", "type", BinaryData.fromObject(user), CloudEventDataFormat.JSON, "application/json");
client.send(cloudEvent).subscribe();
```

```java com.azure.messaging.eventgrid.namespaces.SendMultipleEventsAsyncExample
User john = new User("John", "Doe");
User jane = new User("Jane", "Doe");
CloudEvent johnEvent
    = new CloudEvent("source", "type", BinaryData.fromObject(user), CloudEventDataFormat.JSON, "application/json");
CloudEvent janeEvent
    = new CloudEvent("source", "type", BinaryData.fromObject(user), CloudEventDataFormat.JSON, "application/json");
client.send(Arrays.asList(johnEvent, janeEvent)).subscribe();
```

### Receive and Process Events
Receiving events requires an [event subscription](https://learn.microsoft.com/azure/event-grid/concepts-event-grid-namespaces#event-subscriptions). Upon receiving an event, there are many operations that can be used to settle the event. They are shown in the end to end example below.

```java com.azure.messaging.eventgrid.namespaces.ReceiveEventExample

EventGridReceiverClient client = new EventGridReceiverClientBuilder().endpoint("your endpoint")
    .topicName("your topic")
    .subscriptionName("your subscription")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();

// Receive optionally takes a maximum number of events and a duration to wait. The defaults are
// 1 event and 60 seconds.
ReceiveResult result = client.receive(2, Duration.ofSeconds(10));

// The result contains the received events and the details of the operation. Use the details to obtain
// lock tokens for settling the event. Lock tokens are opaque strings that are used to acknowledge,
// release, or reject the event.

result.getDetails().forEach(details -> {
    CloudEvent event = details.getEvent();
    // Based on some examination of the event, it might be acknowledged, released, or rejected.
    User user = event.getData().toObject(User.class);
    if (user.getFirstName().equals("John")) {
        // Acknowledge the event.
        client.acknowledge(Arrays.asList(details.getBrokerProperties().getLockToken()));
    } else if (user.getFirstName().equals("Jane")) {
        // Release the event.
        client.release(Arrays.asList(details.getBrokerProperties().getLockToken()));
    } else {
        // Reject the event.
        client.reject(Arrays.asList(details.getBrokerProperties().getLockToken()));
    }
});

```

The same can be accomplished asynchronously using the `Async` client variations.

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

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[product_documentation]: https://azure.microsoft.com/services/
[cli]: https://docs.microsoft.com/cli/azure
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[javadocs]: https://azure.github.io/azure-sdk-for-java/eventgrid.html
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[portal]: https://ms.portal.azure.com/
[service_docs]: https://docs.microsoft.com/azure/event-grid/
[HttpResponseException]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/exception/HttpResponseException.java
![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Feventgrid%2Fazure-messaging-eventgrid-namespaces%2FREADME.png)
