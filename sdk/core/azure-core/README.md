# Azure Core client library for Java

[![Build Documentation](https://img.shields.io/badge/documentation-published-blue.svg)](https://azure.github.io/azure-sdk-for-java)

Azure Core provides shared primitives, abstractions, and helpers for modern Java Azure SDK client libraries. These libraries follow the [Azure SDK Design Guidelines for Java](https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html) and can be easily identified by package names starting with `com.azure` and module names starting with `azure-`, e.g. `com.azure.storage.blobs` would be found within the `/sdk/storage/azure-storage-blob` directory. A more complete list of client libraries using Azure Core can be found [here](https://github.com/Azure/azure-sdk-for-java).

Azure Core allows client libraries to expose common functionality in a consistent fashion, so that once you learn how to use these APIs in one client library, you will know how to use them in other client libraries.

## Getting started

Typically, you will not need to install or specifically depend on Azure Core, instead it will be transitively downloaded by your build tool when you depend on of the client libraries using it. In case you want to depend on it explicitly (to implement your own client library, for example), include the following Maven dependency:

[//]: # ({x-version-update-start;com.azure:azure-core;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core</artifactId>
  <version>1.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

For details on including this dependency in other build tools (Gradle, SBT, etc), refer [here](https://search.maven.org/artifact/com.azure/azure-core).

## Key concepts

The key concepts of Azure Core (and therefore all Azure client libraries using Azure Core) include:

- Configuring service clients, e.g. configuring retries, logging, etc.
- Accessing HTTP response details (`Response<T>`).
- Calling long running operations (`Poller<T>`).
- Paging and asynchronous streams (`PagedFlux<T>`).
- Exceptions for reporting errors from service requests in a consistent fashion.
- Abstractions for representing Azure SDK credentials.

These will be introduced by way of the examples presented below.

## Examples

### Accessing HTTP Response Details Using `Response<T>`

_Service clients_ have methods that can be used to call Azure services. We refer to these client methods _service methods_.
_Service methods_ return a shared Azure Core type `Response<T>`. This type provides access to both the deserialized result of the service call, and to the details of the HTTP response returned from the server.

### HTTP pipelines with `HttpPipeline`

Coming soon ...

### Exception Hierarchy with `AzureException`

Coming soon ...

### Pagination with `PagedFlux<T>`

Coming soon ...

### Long Running Operations with `Poller<T>`

Coming soon ...

## Next steps

Get started with some of the Azure libraries that are [built using Azure Core](https://github.com/Azure/azure-sdk-for-java).

## Troubleshooting

If you encounter any bugs, please file issues via [GitHub Issues](https://github.com/Azure/azure-sdk-for-java/issues) or checkout
[StackOverflow for Azure Java SDK](http://stackoverflow.com/questions/tagged/azure-java-sdk).

## Contributing

If you would like to become an active contributor to this project please follow the instructions provided in
[Microsoft Azure Projects Contribution Guidelines](http://azure.github.io/guidelines.html).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcore%2Fazure-core%2FREADME.png)
