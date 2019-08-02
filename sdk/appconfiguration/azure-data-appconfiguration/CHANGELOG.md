# Change Log azure-data-appconfiguration

## Version 1.0.0-preview.2:

For details on the Azure SDK for Java (August 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview2-java).

- Merged ConfigurationClientBuilder and ConfigurationAsyncClientBuilder into ConfigurationClientBuilder. Method to build each client were added.
- ConfigurationClientBuilder was made instantiable, static builder method removed from ConfigurationClient and ConfigurationAsyncClient.
- Builder method credentials renamed to credential and serviceEndpoint to endpoint.
- Listing operations return PagedFlux and PagedIterable in their respective clients.
- Non-maximal overloads return response value instead of Response<T>.
- Asynchronous calls check subscriberContext for tracing context.
- Synchronous calls support passing tracing context in maximal overloads.

## Version 1.0.0-preview.1:

Version 1.0.0-preview.1 is a preview of our efforts in creating a client library that is developer-friendly, idiomatic 
to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide 
our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html).

For details on the Azure SDK for Java (July 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview1-java).

This package's [documentation](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/appconfiguration/azure-data-appconfiguration/README.md) 
and [samples](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/appconfiguration/azure-data-appconfiguration/src/samples/java/com/azure/data/appconfiguration) 
demonstrate the new API.

- Initial release. Please see the README and wiki for information on the new design.
