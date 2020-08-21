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

## Key Concepts 

Events can be sent or received in either the `CloudEvent` or the `EventGridEvent` 
format, depending on the Event Grid topic.

`EventGridEvent`: See specifications and requirements [here](https://docs.microsoft.com/en-us/azure/event-grid/event-schema).

`CloudEvent`: See the Cloud Event specification [here](https://github.com/cloudevents/spec)
and the Event Grid service summary of Cloud Events [here](https://docs.microsoft.com/en-us/azure/event-grid/cloud-event-schema).

Both classes can be used to consume events from a JSON payload, and can be constructed and sent
for publishing, using a `PublisherClient`



## Examples

Check back to find example code as it is added.

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

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Feventgrid%2Fazure-messaging-eventgrid%2FREADME.png)
