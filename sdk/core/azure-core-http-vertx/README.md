# Azure Core Vert.x HTTP plugin library for Java

Azure Core Vert.x HTTP client is a plugin for the `azure-core` HTTP client API.

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
    <artifactId>azure-core-http-vertx</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-core-http-vertx;current})
```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core-http-vertx</artifactId>
    <version>1.2.0</version>
  </dependency>
</dependencies>
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

Create a Vert.x HttpClient.

```java readme-sample-createBasicClient
HttpClient client = new VertxHttpClientBuilder().build();
```

### Configure Timeouts

Configure a 60-second connection timeout, 120-second idle write/read timeouts, and a 60-second response timeout.
These settings apply to different stages of a request; they are not a single deadline for the entire operation.

```java readme-sample-configureTimeouts
HttpClient client = new VertxHttpClientBuilder()
    .connectTimeout(Duration.ofSeconds(60))
    .writeTimeout(Duration.ofSeconds(120))
    .responseTimeout(Duration.ofSeconds(60))
    .readTimeout(Duration.ofSeconds(120))
    .build();
```

### Create a Client with Proxy

Create a Vert.x client that is using a proxy.

```java readme-sample-createProxyClient
HttpClient client = new VertxHttpClientBuilder()
    .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888)))
    .build();
```

### Create a Client with Authenticated Proxy

Supply the credentials required by the HTTP proxy.

```java readme-sample-createAuthenticatedProxyClient
HttpClient client = new VertxHttpClientBuilder()
    .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888))
        .setCredentials("<username>", "<password>"))
    .build();
```

### Configure Proxy Bypass

Configure hosts that should be contacted directly instead of through the proxy. Replace the placeholder with the
non-proxy host pattern expected by `ProxyOptions.setNonProxyHosts`.

```java readme-sample-createProxyWithNonProxyHostsClient
HttpClient client = new VertxHttpClientBuilder()
    .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888))
        .setNonProxyHosts("<nonProxyHostRegex>"))
    .build();
```

### Restrict the Client to HTTP/1.1

Use `com.azure.core.http.HttpProtocolVersion` to restrict the client to HTTP/1.1.

```java readme-sample-useHttp1
HttpClient client = new VertxHttpClientBuilder()
    .maximumHttpVersion(HttpProtocolVersion.HTTP_1_1)
    .build();
```

### Create a Client with HTTP/2 Support

Enable HTTP/2 with HTTP/1.1 fallback using `com.azure.core.http.HttpProtocolVersion`.

```java readme-sample-configureHttpVersion
HttpClient client = new VertxHttpClientBuilder()
    .maximumHttpVersion(HttpProtocolVersion.HTTP_2)
    .build();
```

The client uses ALPN to negotiate HTTP/2 over TLS. For plain HTTP requests, Vert.x's cleartext upgrade configuration
applies. Use `HTTP_1_1` explicitly to limit the client to HTTP/1.1. Passing `null` clears the maximum, preserving the
existing default or the protocols in supplied internal Vert.x instances. An explicit maximum overrides protocol and ALPN
settings in a copy of those options without mutating the originals; unrelated settings are retained.

### Customize the Underlying Instance

Supply an application-configured internal Vert.x instance before supplying it to `VertxHttpClientBuilder.vertx`. This
example creates the instance with four event-loop threads.

```java readme-sample-createClientWithVertxInstance
VertxOptions vertxOptions = new VertxOptions()
    .setEventLoopPoolSize(4);
Vertx vertx = Vertx.vertx(vertxOptions);
HttpClient client = new VertxHttpClientBuilder()
    .vertx(vertx)
    .build();
```

The application is responsible for calling `vertx.close()` after all clients using that instance have finished.

### Advanced Configuration

Client options provide connection, read and write timeouts and proxy settings when supplied to the builder. The
builder's response timeout still applies, and an explicit `maximumHttpVersion` overrides protocol/ALPN settings in a
copy of the supplied options. HTTP/2 cleartext behavior can be configured through the supplied Vert.x
`HttpClientOptions`. There is no portable HTTP/2-only equivalent shared by all four transports.

#### Customize the Maximum Header Size

Create a Vert.x HttpClient that uses a custom maxHeaderSize. Use this sample if you're seeing an error such as

```
io.netty.handler.codec.http.TooLongHttpHeaderException: HTTP header is larger than 8192 bytes.
```

(This is a Netty exception as maxHeaderSize is flowed through to Netty.)

```java readme-sample-customMaxHeaderSize
// Constructs an HttpClient with a modified max header size.
// This creates a Vert.x HttpClient with a max headers size of 256 KB.
// NOTE: Internal options provide connection, read and write timeouts and proxy settings.
HttpClient httpClient = new VertxHttpClientBuilder()
    .httpClientOptions(new HttpClientOptions().setMaxHeaderSize(256 * 1024))
    .build();
```

#### Customize HTTP/2 cleartext behavior

`HttpClientOptions.setHttp2ClearTextUpgrade` controls how Vert.x connects to cleartext HTTP/2 (`h2c`) servers.
With `true` (the default), it starts with HTTP/1.1 and attempts an upgrade. If the server doesn't support the upgrade,
it continues using HTTP/1.1.

Set it to `false` to use prior knowledge and start directly with HTTP/2, as in the following example. Use this only
when the server is known to support cleartext HTTP/2, because there is no HTTP/1.1 fallback in this mode.

```java readme-sample-configureHttp2PriorKnowledge
HttpClientOptions clientOptions = new HttpClientOptions()
    .setHttp2ClearTextUpgrade(false);
HttpClient client = new VertxHttpClientBuilder()
    .httpClientOptions(clientOptions)
    .maximumHttpVersion(HttpProtocolVersion.HTTP_2)
    .build();
```

This cleartext setting applies only to `http://` requests. It doesn't change HTTPS protocol negotiation, which uses
ALPN when HTTP/2 is enabled.

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
