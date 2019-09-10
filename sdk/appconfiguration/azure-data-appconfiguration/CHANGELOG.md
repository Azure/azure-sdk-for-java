# Change Log azure-data-appconfiguration

## Version 1.0.0-preview.3 (2019-09-10)
For details on the Azure SDK for Java (September 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview3-java).

- Removed dependency on Netty.
- Added logging when throwing `RutimeException`s.

This package's 
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-data-appconfiguration_1.0.0-preview.3/sdk/appconfiguration/azure-data-appconfiguration/README.md) 
and 
[samples](https://github.com/Azure/azure-sdk-for-java/tree/azure-data-appconfiguration_1.0.0-preview.3/sdk/appconfiguration/azure-data-appconfiguration/src/samples/java/com/azure/data/appconfiguration) 
demonstrate the new API.

## Version 1.0.0-preview.2 (2019-08-06)
For details on the Azure SDK for Java (August 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview2-java).

- Merged ConfigurationClientBuilder and ConfigurationAsyncClientBuilder into ConfigurationClientBuilder. Method to build each client were added.
- ConfigurationClientBuilder was made instantiable, static builder method removed from ConfigurationClient and ConfigurationAsyncClient.
- Builder method credentials renamed to credential and serviceEndpoint to endpoint.
- Listing operations return PagedFlux and PagedIterable in their respective clients.
- Asynchronous calls check subscriberContext for tracing context.
- Synchronous calls support passing tracing context in maximal overloads.

**Breaking changes: New API Design**
- Simplified API to return model types directly on non-maximal overloads. Maximal overloads return `Response<T>` and suffixed with WithResponse.

This package's 
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-data-appconfiguration_1.0.0-preview.2/sdk/appconfiguration/azure-data-appconfiguration/README.md) 
and 
[samples](https://github.com/Azure/azure-sdk-for-java/tree/azure-data-appconfiguration_1.0.0-preview.2/sdk/appconfiguration/azure-data-appconfiguration/src/samples/java/com/azure/data/appconfiguration) 
demonstrate the new API.

## Version 1.0.0-preview.1 (2019-06-28)
Version 1.0.0-preview.1 is a preview of our efforts in creating a client library that is developer-friendly, idiomatic 
to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide 
our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html).

For details on the Azure SDK for Java (July 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview1-java).

This package's 
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-data-appconfiguration_1.0.0-preview.1/appconfiguration/client/README.md) 
and 
[samples](https://github.com/Azure/azure-sdk-for-java/tree/azure-data-appconfiguration_1.0.0-preview.1/appconfiguration/client/src/samples/java) 
demonstrate the new API.

- Initial release. Please see the README and wiki for information on the new design.
