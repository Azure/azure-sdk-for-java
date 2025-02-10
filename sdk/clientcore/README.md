# Client Core shared library for Java

Client Core provides shared primitives, abstractions, and helpers for modern SDK client libraries. These libraries
can be easily identified by package names starting with `io.clientcore`, e.g. `io.clientcore.core` would be found within
the `/sdk/clientcore/core` directory.

Client Core allows client libraries to expose common functionality in a consistent fashion, so that once you learn how
to use these APIs in one client library, you will know how to use them in other client libraries.

The main shared concepts of Client Core include:

- Configuring service clients, e.g. configuring retries, logging, etc.
- Accessing HTTP response details (`Response<T>`).
- Exceptions for reporting errors from service requests in a consistent fashion.
- Abstractions for representing credentials.

## Sub-projects

Client Core is split into a number of sub-components:

- [https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/clientcore/core](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/clientcore/core) is the primary library, used by all client libraries to offer the functionality outlined above.
- [https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/clientcore/http-okhttp3](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/clientcore/http-okhttp3) provides an OkHttp derived HTTP client.

For documentation on using Client Core, refer to the [https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/clientcore/core readme](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/clientcore/core).
