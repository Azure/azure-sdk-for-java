# Azure Core shared library for Java

[![Build Documentation](https://img.shields.io/badge/documentation-published-blue.svg)](https://azure.github.io/azure-sdk-for-java)

Azure Core provides shared primitives, abstractions, and helpers for modern Java Azure SDK client libraries. These libraries follow the [Azure SDK Design Guidelines for Java](https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html) and can be easily identified by package names starting with `com.azure` and module names starting with `azure-`, e.g. `com.azure.storage.blobs` would be found within the `/sdk/storage/azure-storage-blob` directory. A more complete list of client libraries using Azure Core can be found [here](https://github.com/Azure/azure-sdk-for-java).

Azure Core allows client libraries to expose common functionality in a consistent fashion, so that once you learn how to use these APIs in one client library, you will know how to use them in other client libraries.

The main shared concepts of Azure Core (and therefore all Azure client libraries using Azure Core) include:

- Configuring service clients, e.g. configuring retries, logging, etc.
- Accessing HTTP response details (`Response<T>`).
- Calling long running operations (`Poller<T>`).
- Paging and asynchronous streams (`PagedFlux<T>`).
- Exceptions for reporting errors from service requests in a consistent fashion.
- Abstractions for representing Azure SDK credentials.

## Sub-projects

Azure Core is split into a number of sub-components:

* [azure-core](azure-core) is the primary library, used by all client libraries to offer the functionality outlined above.
* [azure-core-amqp](azure-core-amqp) provides functionality related to AMQP (Advanced Message Queuing Protocol).
* [azure-core-management](azure-core-management) provides APIs used by the Azure management libraries, but which are redundant to client libraries.
* [azure-core-test](azure-core-test) provides utilities and API to make writing tests for Azure Core simpler and consistent.

For documentation on using Azure Core, refer to the [azure-core readme](azure-core).

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcore%2FREADME.png)
