# Azure Cloud Native Cloud Event client library for Java

This library can be used to enable publishing the Cloud Native Computing Foundation(CNCF) [CloudEvents][CNCFCloudEvents]
using the Azure Event Grid library. 

## Getting started

### Prerequisites
You should have an EventGrid client before using this bridge library. Follow [Azure EventGrid][eventgridGettingStarted]
steps to create an EventGrid client.

### Include the package

#### Include direct dependency
If you want to take dependency on a particular version of the library, add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-messaging-cloudnative-cloudevents;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventgrid-cloudnative-cloudevents</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts
For information about general Event Grid concepts: [Concepts in Azure Event Grid][eventgridConcept].

For detailed information about the Event Grid client library concepts: [Event Grid Client Library][eventgridClientConcept].

## Examples

### Sending CNCF CloudEvents To Event Grid Topics
```java readme-sample-sendCNCFCloudEvents-topic
// Prepare Event Grid client
EventGridPublisherClient<com.azure.core.models.CloudEvent> egClient =
    new EventGridPublisherClientBuilder()
        .endpoint(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_ENDPOINT"))
        .credential(new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_KEY")))
        .buildCloudEventPublisherClient();

// Prepare a native cloud event input, the cloud event input should be replace with your own.
CloudEvent cloudEvent =
    CloudEventBuilder.v1()
        .withData("{\"name\": \"joe\"}".getBytes(StandardCharsets.UTF_8)) // Replace it
        .withId(UUID.randomUUID().toString()) // Replace it
        .withType("User.Created.Text") // Replace it
        .withSource(URI.create("http://localHost")) // Replace it
        .withDataContentType("application/json") // Replace it
        .build();

// Publishing a single event
EventGridCloudNativeEventPublisher.sendEvent(egClient, cloudEvent);
```

### Sending CNCF CloudEvents To Event Grid Domain
When publishing to an Event Grid domain with cloud events, the cloud event source is used as the domain topic.
The Event Grid service doesn't support using an absolute URI for a domain topic, so you would need to do
something like the following to integrate with the cloud native cloud events:
```java readme-sample-sendCNCFCloudEvents-domain
// Prepare Event Grid client
EventGridPublisherClient<com.azure.core.models.CloudEvent> egClient =
    new EventGridPublisherClientBuilder()
        .endpoint(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_ENDPOINT"))
        .credential(new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_KEY")))
        .buildCloudEventPublisherClient();

// Prepare a native cloud event input, the cloud event input should be replace with your own.
CloudEvent cloudEvent =
    CloudEventBuilder.v1()
        .withData("{\"name\": \"joe\"}".getBytes(StandardCharsets.UTF_8)) // Replace it
        .withId(UUID.randomUUID().toString()) // Replace it
        .withType("User.Created.Text") // Replace it
        // Replace it. Event Grid does not allow absolute URIs as the domain topic.
        // For example, use the Event Grid Domain resource name as the relative path.
        .withSource(URI.create("/relative/path"))
        .withDataContentType("application/json") // Replace it
        .build();

// Publishing a single event
EventGridCloudNativeEventPublisher.sendEvent(egClient, cloudEvent);
```

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

If you would like to become an active contributor to this project please refer to our 
Contribution Guidelines for more information.

<!-- LINKS -->
[eventgridGettingStarted]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventgrid/azure-messaging-eventgrid#getting-started
[eventgridConcept]: https://docs.microsoft.com/azure/event-grid/concepts
[eventgridClientConcept]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventgrid/azure-messaging-eventgrid#key-concepts
[javadocs]: https://azure.github.io/azure-sdk-for-java/eventgrid.html
[CNCFCloudEvents]: https://cloudevents.github.io/sdk-java/
[HttpResponseException]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/exception/HttpResponseException.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Feventgrid%2Fazure-messaging-eventgrid-cloudnative-cloudevents%2FREADME.png)
