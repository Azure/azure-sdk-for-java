# Azure Core OkHttp HTTP plugin library for Java

Azure Core OkHttp HTTP client is a plugin for the `azure-core` HTTP client API.

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
  - Here are details about [Java 8 client compatibility with Azure Certificate Authority][java8_client_compatibility].

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
and then include the direct dependency in the dependencies section without the version tag.

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core-http-okhttp</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-core-http-okhttp;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core-http-okhttp</artifactId>
    <version>1.13.8</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

## Examples

The following sections provide several code snippets covering some of the most common client configuration scenarios.

- [Create a Simple Client](#create-a-simple-client)
- [Configure Timeouts](#configure-timeouts)
- [Create a Client with Proxy](#create-a-client-with-proxy)
- [Create a Client with Authenticated Proxy](#create-a-client-with-authenticated-proxy)
- [Configure Proxy Bypass](#configure-proxy-bypass)
- [Restrict the Client to HTTP/1.1](#restrict-the-client-to-http11)
- [Create a Client with HTTP/2 Support](#create-a-client-with-http2-support)
- [Customize the Underlying Client](#customize-the-underlying-client)
- [Advanced Configuration](#advanced-configuration)

### Create a Simple Client

Create an OkHttp client with the default configuration.

```java readme-sample-createBasicClient
HttpClient client = new OkHttpAsyncHttpClientBuilder().build();
```

### Configure Timeouts

Configure a 60-second connection timeout, 120-second idle write/read timeouts, and a 60-second response timeout.
These settings apply to different stages of a request; they are not a single deadline for the entire operation.

```java readme-sample-configureTimeouts
HttpClient client = new OkHttpAsyncHttpClientBuilder()
    .connectionTimeout(Duration.ofSeconds(60))
    .writeTimeout(Duration.ofSeconds(120))
    .responseTimeout(Duration.ofSeconds(60))
    .readTimeout(Duration.ofSeconds(120))
    .build();
```

### Create a Client with Proxy

Create an OkHttp client that is using a proxy.

```java readme-sample-createProxyClient
HttpClient client = new OkHttpAsyncHttpClientBuilder()
    .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888)))
    .build();
```

### Create a Client with Authenticated Proxy

Supply the credentials required by the HTTP proxy.

```java readme-sample-createAuthenticatedProxyClient
HttpClient client = new OkHttpAsyncHttpClientBuilder()
    .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888))
        .setCredentials("<username>", "<password>"))
    .build();
```

### Configure Proxy Bypass

Configure hosts that should be contacted directly instead of through the proxy. Replace the placeholder with the
non-proxy host pattern expected by `ProxyOptions.setNonProxyHosts`.

```java readme-sample-createProxyWithNonProxyHostsClient
HttpClient client = new OkHttpAsyncHttpClientBuilder()
    .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888))
        .setNonProxyHosts("<nonProxyHostRegex>"))
    .build();
```

### Restrict the Client to HTTP/1.1

Use `com.azure.core.http.HttpProtocolVersion` to restrict the client to HTTP/1.1.

```java readme-sample-useHttp1
HttpClient client = new OkHttpAsyncHttpClientBuilder()
    .maximumHttpVersion(HttpProtocolVersion.HTTP_1_1)
    .build();
```

### Create a Client with HTTP/2 Support

Create an OkHttp client that supports both the HTTP/1.1 and HTTP/2 protocols, with HTTP/2 being the preferred protocol.

```java readme-sample-configureHttpVersion
HttpClient client = new OkHttpAsyncHttpClientBuilder()
    .maximumHttpVersion(HttpProtocolVersion.HTTP_2)
    .build();
```

HTTP/2 is negotiated over TLS, with HTTP/1.1 fallback; plain HTTP requests use HTTP/1.1. Use `HTTP_1_1` to limit the
client to HTTP/1.1. Passing `null` clears the maximum, preserving OkHttp's default protocols or those of a supplied
internal OkHttp client.

### Customize the Underlying Client

Pass an application-configured internal OkHttp client to the Azure builder. This example disables OkHttp's own
connection retries; retry policies in an Azure HTTP pipeline remain independently configurable. The configurable internal
settings can differ from those exposed in `com.azure.core.util.HttpClientOptions`.

```java readme-sample-customizeInternalClient
OkHttpClient internalClient = new OkHttpClient.Builder()
    .retryOnConnectionFailure(false)
    .build();
HttpClient client = new OkHttpAsyncHttpClientBuilder(internalClient).build();
```

### Advanced Configuration

The following examples use OkHttp-specific protocol settings and are not portable to every HTTP transport.

#### Configure HTTP/2 on an Internal Client

You can also configure the internal client's protocol list directly instead of using `maximumHttpVersion`.

```java readme-sample-useHttp2WithConfiguredOkHttpClient
// Constructs an HttpClient that supports both HTTP/1.1 and HTTP/2 with HTTP/2 being the preferred protocol.
// This is the default handling for OkHttp.
HttpClient client = new OkHttpAsyncHttpClientBuilder(new OkHttpClient.Builder()
    .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
    .build())
    .build();
```

#### Create a Cleartext HTTP/2-Only Client

`H2_PRIOR_KNOWLEDGE` sends cleartext HTTP/2 directly to a server already known to support that mode. It does not
perform an HTTP/1.1 upgrade, does not fall back to HTTP/1.1, and cannot be used with HTTPS. OkHttp 4.12 requires
`HTTP_1_1` in a negotiated protocol list, so replacing this value with a singleton `HTTP_2` list is not supported.
For normal HTTPS traffic, use `maximumHttpVersion(HTTP_2)` or `[HTTP_2, HTTP_1_1]`.

```java readme-sample-useHttp2OnlyWithConfiguredOkHttpClient
// Constructs a cleartext HTTP/2-only client. HTTPS and HTTP/1.1 fallback are not supported.
HttpClient client = new OkHttpAsyncHttpClientBuilder(new OkHttpClient.Builder()
    .protocols(Collections.singletonList(Protocol.H2_PRIOR_KNOWLEDGE))
    .build())
    .build();
```

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
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- Links -->
[logging]: https://learn.microsoft.com/azure/developer/java/sdk/logging-overview
[jdk_link]: https://learn.microsoft.com/java/azure/jdk/?view=azure-java-stable
[java8_client_compatibility]: https://learn.microsoft.com/azure/security/fundamentals/azure-ca-details?tabs=root-and-subordinate-cas-list#client-compatibility-for-public-pkis
