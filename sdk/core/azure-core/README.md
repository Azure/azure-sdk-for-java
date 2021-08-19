# Azure Core shared library for Java

[![Build Documentation](https://img.shields.io/badge/documentation-published-blue.svg)](https://azure.github.io/azure-sdk-for-java)

Azure Core provides shared primitives, abstractions, and helpers for modern Java Azure SDK client libraries.
These libraries follow the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html)
and can be easily identified by package names starting with `com.azure` and module names starting with `azure-`,
e.g. `com.azure.storage.blobs` would be found within the `/sdk/storage/azure-storage-blob` directory. A more complete
list of client libraries using Azure Core can be found [here](https://azure.github.io/azure-sdk/releases/latest/#java-packages).

Azure Core allows client libraries to expose common functionality consistently, so that once you learn how to use these
APIs in one client library, you will know how to use them in other client libraries.

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.

### Include the package

#### Include the BOM file

Please include the azure-sdk-bom to your project to take dependency on the General Availability (GA) version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
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
and then include the direct dependency in the dependencies section without the version tag. Typically, you won't need to install or depend on Azure Core, instead it will be transitively downloaded by your build
tool when you depend on client libraries using it.

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-core;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core</artifactId>
  <version>1.19.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

The key concepts of Azure Core (and therefore all Azure client libraries using Azure Core) include:

- Configuring service clients, e.g. configuring retries, logging, etc.
- Accessing HTTP response details (`Response<T>`).
- Calling long-running operations (`PollerFlux<T>`).
- Paging and asynchronous streams (`ContinuablePagedFlux<T>`).
- Exceptions for reporting errors from service requests consistently.
- Abstractions for representing Azure SDK credentials.

These will be introduced by way of the examples presented below.

## Examples

### Accessing HTTP Response Details Using `Response<T>`

_Service clients_ have methods that call Azure services, we refer call these methods _service methods_.

_Service methods_ can return a shared Azure Core type `Response<T>`. This type provides access to both the
deserialized result of the service call and to the details of the HTTP response returned from the server.

### HTTP pipelines with `HttpPipeline`

`HttpPipeline` is a construct that contains a list of `HttpPipelinePolicy` which are applied to a request
sequentially to prepare it being sent by an `HttpClient`.

### Exception Hierarchy with `AzureException`

`AzureException` is the root exception in the hierarchy used in Azure Core. Additional exceptions such as
`HttpRequestException` and `HttpResponseException` are used to reduce the scope of exception reasons.

### Pagination with `ContinuablePagedFlux<T>`

`ContinuablePageFlux` manages sending an initial page request to a service and retrieving additional pages as the
consumer requests more data until the consumer finishes processing or all pages have been consumed.

### Long Running Operations with `PollerFlux<T>`

`PollerFlux` manages sending an initial service request and requesting processing updates on a fix interval until polling is cancelled or reaches a terminal state.

## Next steps

Get started with Azure libraries that are [built using Azure Core](https://azure.github.io/azure-sdk/releases/latest/#java).

## Troubleshooting

If you encounter any bugs, please file issues via [GitHub Issues](https://github.com/Azure/azure-sdk-for-java/issues/new/choose)
or checkout [StackOverflow for Azure Java SDK](https://stackoverflow.com/questions/tagged/azure-java-sdk).

### Enabling Logging

Azure SDKs for Java provide a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

<!-- links -->
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcore%2Fazure-core%2FREADME.png)
