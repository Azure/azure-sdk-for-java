# Azure Core shared library for Java

[![Build Documentation](https://img.shields.io/badge/documentation-published-blue.svg)](https://azure.github.io/azure-sdk-for-java)

Azure Core provides shared primitives, abstractions, and helpers for modern Java Azure SDK client libraries.
These libraries follow
the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html)
and can be easily identified by package names starting with `com.azure` and module names starting with `azure-`,
e.g. `com.azure.storage.blobs` would be found within the `/sdk/storage/azure-storage-blob` directory. A more complete
list of client libraries using Azure Core can be
found [here](https://azure.github.io/azure-sdk/releases/latest/#java-packages).

Azure Core allows client libraries to expose common functionality consistently, so that once you learn how to use these
APIs in one client library, you will know how to use them in other client libraries.

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.

### Include the package

#### Include the BOM file

Please include the azure-sdk-bom to your project to take dependency on the General Availability (GA) version of the
library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
To learn more about the BOM, see
the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

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

and then include the direct dependency in the dependencies section without the version tag. Typically, you won't need to
install or depend on Azure Core, instead it will be transitively downloaded by your build
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
    <version>1.49.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

The key concepts of Azure Core (and therefore all Azure client libraries using Azure Core) include:

- Configuring service clients, e.g. configuring retries, logging, etc. (`HttpTrait<T>`, `ConfigurationTrait<T>`, etc.)
- Accessing HTTP response details (`Response<T>`).
- Calling long-running operations (`PollerFlux<T>`).
- Paging and asynchronous streams (`ContinuablePagedFlux<T>`).
- Exceptions for reporting errors from service requests consistently.
- Abstractions for representing Azure SDK credentials.
- Operation timeouts

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

`PollerFlux` manages sending an initial service request and requesting processing updates on a fix interval until
polling is cancelled or reaches a terminal state.

### Configuring Builders

Builders are used to create service clients and some `TokenCredential` implementations. They can be configured with a 
variety of options, including `HttpPipeline` and `HttpClient` for HTTP-based clients and more general options such as 
`Configuration` and`endpoint`. To allow for simpler integration into frameworks such as Spring and to allow generic 
methods to be used for all builders `azure-core` provides a set of interfaces that can be implemented to provide
the necessary functionality.

#### HttpTrait<T>

`HttpTrait<T>` contains methods for setting key configurations for HTTP-based clients. This interface will allow you to
configure the `HttpClient`, `HttpPipeline`, `HttpPipelinePolicy`s, `HttpRetryOptions`, `HttpLogOptions`, and `ClientOptions` 
(preferably `HttpClientOptions` as it is more specific for HTTP-based service clients).

For builders that expose `HttpTrait<T>`, if an `HttpPipeline` or `HttpClient` isn't set a default instance will be 
created based on classpath configurations and the `ClientOptions` based to the builder. This can cause confusion if 
you're expecting specific behavior for your client, such as using a proxy that wasn't loaded from the environment. To 
avoid this, it is recommended to always set the `HttpPipeline` or `HttpClient` in all clients if you're building if your 
configurations aren't based on the environment running the application.

#### Credential Traits

Azure Core provides different credentials for authenticating with Azure services. Each credential type has a
corresponding trait that can be implemented to provide the credential to the client builder. The following table
lists the credential traits and the corresponding credential type.

| Credential Trait                  | Credential Type                                           |
|-----------------------------------|-----------------------------------------------------------|
| `AzureKeyCredentialTrait`         | `AzureKeyCredential`                                      |
| `AzureNamedKeyCredentialTrait`    | `AzureNamedKeyCredential`                                 |
| `AzureSasCredentialTrait`         | `AzureSasCredential`                                      |
| `ConnectionStringCredentialTrait` | `String` (there is no formal type for connection strings) |
| `KeyCredentialTrait`              | `KeyCredential`                                           |
| `TokenCredentialTrait`            | `TokenCredential`                                         |

#### ConfigurationTrait<T>

`ConfigurationTrait<T>` allows for setting `Configuration` on service clients. `Configuration` can be used to pass a set
of runtime behaviors to the client builder such as how `ProxyOptions` are loaded from the environment, implicitly
passing credentials to some client builders that support it, and more.

#### EndpointTrait<T>

`EndpointTrait<T>` allows for setting the service endpoint on service clients.

### Operation Timeouts

Azure SDKs provide a few, consistent ways to configure timeouts on API calls. Each timeout effects a different scope
of the Azure SDKs and calling application.

#### HTTP Timeouts

HTTP timeouts are the lowest level of timeout handling the Azure SDKs provide. These timeouts can be configured when
building `HttpClient`s or using `HttpClientOptions` when building service clients without configuring an `HttpClient`
yourself. The following table lists the HTTP timeout, the corresponding `HttpClientOptions` method that can be used to
set it, environment variable to control the default value, the default value if the environment value isn't set, and a
brief description of what the timeout effects.

| HTTP Timeout     | `HttpClientOptions` Method     | Environment Variable           | Default Value | Description                                                                                                       |
|------------------|--------------------------------|--------------------------------|---------------|-------------------------------------------------------------------------------------------------------------------|
| Connect Timeout  | `setConnectTimeout(Duration)`  | AZURE_REQUEST_CONNECT_TIMEOUT  | 10 seconds    | The amount of time for a connection to be established before timing out.                                          |
| Write Timeout    | `setWriteTimeout(Duration)`    | AZURE_REQUEST_WRITE_TIMEOUT    | 60 seconds    | The amount of time between each request data write to the network before timing out.                              |
| Response Timeout | `setResponseTimeout(Duration)` | AZURE_REQUEST_RESPONSE_TIMEOUT | 60 seconds    | The amount of time between finishing sending the request to receiving the first response bytes before timing out. |
| Read Timeout     | `setReadTimeout(Duration)`     | AZURE_REQUEST_READ_TIMEOUT     | 60 seconds    | The amount of time between each response data read from the network before timing out.                            |

Since these timeouts are closest to the network, if they trigger they will be propagated back through the `HttpPipeline`
and generally should be retried by the `RetryPolicy`.

#### HttpPipeline Timeouts

HttpPipeline timeouts are the next level of timeout handling the Azure SDKs provide. These timeouts are configured using
an `HttpPipelinePolicy` and configuring a timeout using either `Mono.timeout` for asynchronous requests or an 
`ExecutorService` with a timed `get(long, TimeUnit)` for synchronous requests.

Depending on the location within the `HttpPipeline`, these timeouts may be captured by the `RetryPolicy` and retried.
If the timeout policy is `PER_RETRY` (`HttpPipelinePolicy.getPipelinePosition()`) the timeout will be captured by the
`RetryPolicy` as it will be positioned after the `RetryPolicy`, therefore in its capture scope, if it is `PER_CALL`
retrying the request will need to be handled by application logic.

#### Service Client Timeouts

Service client timeouts are the highest level of timeout handling the Azure SDKs provide. These timeouts are configured
by passing `Duration timeout` into synchronous service methods that support timeouts or by using `Mono.timeout` or
`Flux.timeout` on asynchronous service methods.

Since these timeouts are on the API call itself they cannot be captured by any retry mechanisms within the Azure SDKs
and must be handled by application logic.

## Next steps

Get started with Azure libraries that
are [built using Azure Core](https://azure.github.io/azure-sdk/releases/latest/#java).

## Troubleshooting

If you encounter any bugs, please file issues
via [GitHub Issues](https://github.com/Azure/azure-sdk-for-java/issues/new/choose)
or checkout [StackOverflow for Azure Java SDK](https://stackoverflow.com/questions/tagged/azure-java-sdk).

### Enabling Logging

Azure SDKs for Java provide a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] documentation for guidance about enabling logging.

#### HTTP Request and Response Logging

HTTP request and response logging can be enabled by setting `HttpLogDetailLevel` in the `HttpLogOptions` used to create
an HTTP-based service client or by setting the environment variable or system property `AZURE_HTTP_LOG_DETAIL_LEVEL`.
The following table displays the valid options for `AZURE_HTTP_LOG_DETAIL_LEVEL` and the `HttpLogDetailLevel` it
correlates to (valid options are case-insensitive):

| `AZURE_HTTP_LOG_DETAIL_LEVEL` value | `HttpLogDetailLevel` enum             |
|-------------------------------------|---------------------------------------|
| `basic`                             | `HttpLogDetailLevel.BASIC`            |
| `headers`                           | `HttpLogDetailLevel.HEADERS`          |
| `body`                              | `HttpLogDetailLevel.BODY`             |
| `body_and_headers`                  | `HttpLogDetailLevel.BODY_AND_HEADERS` |
| `bodyandheaders`                    | `HttpLogDetailLevel.BODY_AND_HEADERS` |

All other values, or unsupported values, result in `HttpLogDetailLevel.NONE`, or disabled HTTP request and response
logging. Logging [must be enabled](#enabling-logging) to log HTTP requests and responses. Logging of HTTP headers
requires `verbose`
logging to be enabled. The following table explains what logging is enabled for each `HttpLogDetailLevel`:

| `HttpLogDetailLevel` value            | Logging enabled                                                                                  |
|---------------------------------------|--------------------------------------------------------------------------------------------------|
| `HttpLogDetailLevel.NONE`             | No HTTP request or response logging                                                              |
| `HttpLogDetailLevel.BASIC`            | HTTP request method, response status code, and request and response URL                          |
| `HttpLogDetailLevel.HEADERS`          | All of `HttpLogDetailLevel.BASIC` and request and response headers if the log level is `verbose` |
| `HttpLogDetailLevel.BODY`             | All of `HttpLogDetailLevel.BASIC` and request and response body if it's under 10KB in size       |
| `HttpLogDetailLevel.BODY_AND_HEADERS` | All of `HttpLogDetailLevel.HEADERS` and `HttpLogDetailLevel.BODY`                                |

## Contributing

For details on contributing to this repository, see
the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

<!-- links -->

[logging]: https://learn.microsoft.com/azure/developer/java/sdk/logging-overview

[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcore%2Fazure-core%2FREADME.png)
