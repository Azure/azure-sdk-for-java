# Azure Cloud Native Cloud Event support for Event Grid

This library can be used to enable publishing the Cloud Native Computing Foundation(CNCF) [CloudEvents][CNCFCloudEvents]
using the Azure Event Grid library. 

## Getting started

### Prerequisites
You should have an EventGrid client before using this bridge library. Follow [Azure EventGrid][eventgridGettingStarted]
steps to create an EventGrid client.

### Include the package
#### Include the BOM file

Please include the azure-sdk-bom to your project to take dependency on GA version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
To learn more about the BOM, see the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-sdk-bom</artifactId>
            <version>{bom_version_to_target}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
and then include the direct dependency in the dependencies section without the version tag.

```xml
<dependencies>
  <dependency>
      <groupId>com.azure</groupId>
      <artifactId>azure-messaging-eventgrid-cloudnative-cloudevents</artifactId>
      <version>1.0.0-beta.1</version> <!-- {x-version-update;com.azure:azure-messaging-eventgrid-cloudnative-cloudevents;current} -->
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-messaging-cloudnative-cloudevents;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventgrid-cloudnative-cloudevents</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Examples

### Sending CNCF CloudEvents To Event Grid Topics
```java readme-sample-sendCNCFCloudEvents
// Prepare Event Grid client
EventGridPublisherClient<CloudEvent> egClient =
    new EventGridPublisherClientBuilder()
        .endpoint(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_ENDPOINT"))
        .credential(new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_KEY")))
        .buildCloudEventPublisherClient();

// Prepare a native cloud event input, the cloud event input should be replace with your own.
io.cloudevents.CloudEvent cloudEvent =
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
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

---

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

<!-- LINKS -->
[javadocs]: https://azure.github.io/azure-sdk-for-java/eventgrid.html
[eventgridGettingStarted]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventgrid/azure-messaging-eventgrid#getting-started 
[CNCFCloudEvents]: https://cloudevents.github.io/sdk-java/
[HttpResponseException]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/exception/HttpResponseException.java

