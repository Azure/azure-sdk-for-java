# Azure Core JDK HTTP plugin library for Java

This is an azure-core HTTP client that makes use of the asynchronous HttpClient that was made generally available as 
part of JDK 11. 

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 12 or later.

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
    <artifactId>azure-core-http-jdk-httpclient</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-core-http-jdk-httpclient;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core-http-jdk-httpclient</artifactId>
    <version>1.2.0</version>
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

Create a HttpClient.

```java readme-sample-createBasicClient
HttpClient client = new JdkHttpClientBuilder().build();
```

### Configure Timeouts

Configure a 60-second connection timeout, 120-second idle write/read timeouts, and a 60-second response timeout.
These settings apply to different stages of a request; they are not a single deadline for the entire operation.

```java readme-sample-configureTimeouts
HttpClient client = new JdkHttpClientBuilder()
    .connectionTimeout(Duration.ofSeconds(60))
    .writeTimeout(Duration.ofSeconds(120))
    .responseTimeout(Duration.ofSeconds(60))
    .readTimeout(Duration.ofSeconds(120))
    .build();
```

To change only the connection timeout:

```java readme-sample-createClientWithConnectionTimeout
HttpClient client = new JdkHttpClientBuilder().connectionTimeout(Duration.ofSeconds(60)).build();
```

### Create a Client with Proxy

Create a HttpClient that is using a proxy.

```java readme-sample-createProxyClient
HttpClient client = new JdkHttpClientBuilder()
    .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888)))
    .build();
```

### Create a Client with Authenticated Proxy

Supply the credentials required by the HTTP proxy. Authentication support also depends on the JDK and proxy
configuration; the Azure builder does not change the JVM's disabled authentication-scheme settings.

```java readme-sample-createAuthenticatedProxyClient
HttpClient client = new JdkHttpClientBuilder()
    .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888))
        .setCredentials("<username>", "<password>"))
    .build();
```

### Configure Proxy Bypass

Configure hosts that should be contacted directly instead of through the proxy. Replace the placeholder with the
non-proxy host pattern expected by `ProxyOptions.setNonProxyHosts`.

```java readme-sample-createProxyWithNonProxyHostsClient
HttpClient client = new JdkHttpClientBuilder()
    .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888))
        .setNonProxyHosts("<nonProxyHostRegex>"))
    .build();
```

### Restrict the Client to HTTP/1.1

Use `com.azure.core.http.HttpProtocolVersion` to restrict the client to HTTP/1.1.

```java readme-sample-useHttp1
HttpClient client = new JdkHttpClientBuilder()
    .maximumHttpVersion(HttpProtocolVersion.HTTP_1_1)
    .build();
```

### Create a Client with HTTP/2 Support

Enable HTTP/2 with HTTP/1.1 fallback using `com.azure.core.http.HttpProtocolVersion`.

```java readme-sample-configureHttpVersion
HttpClient client = new JdkHttpClientBuilder()
    .maximumHttpVersion(HttpProtocolVersion.HTTP_2)
    .build();
```

The JDK client negotiates the protocol with the server. Use `HTTP_1_1` to limit the client to HTTP/1.1. The default
remains HTTP/1.1, including when a native JDK builder is supplied. Passing `null` clears the maximum and restores
that default.

### Customize the Underlying Client

Pass an application-configured native JDK builder to the Azure builder. This example selects the shared common
pool for native asynchronous work rather than creating an executor that needs a separate application lifecycle.
Calling `JdkHttpClientBuilder.executor` overrides an executor supplied through the native builder.

```java readme-sample-customizeUnderlyingClient
java.net.http.HttpClient.Builder nativeBuilder = java.net.http.HttpClient.newBuilder()
    .executor(ForkJoinPool.commonPool());
HttpClient client = new JdkHttpClientBuilder(nativeBuilder).build();
```

### Advanced Configuration

The native JDK client treats HTTP/2 as a preferred version with HTTP/1.1 fallback; its version setting is not an
HTTP/2-only mode. Azure's `maximumHttpVersion` controls the resulting client version even when a native builder is
supplied. Reactor Netty's HTTP/2-only and OkHttp's cleartext prior-knowledge examples should not be copied to this client.
The Azure JDK builder also does not expose equivalent per-client header-size, chunk-size, or DNS-resolver controls.

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

<!-- links -->
[logging]: https://learn.microsoft.com/azure/developer/java/sdk/logging-overview
[jdk_link]: https://learn.microsoft.com/java/azure/jdk/?view=azure-java-stable
