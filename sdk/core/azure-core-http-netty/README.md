# Azure Core Netty HTTP plugin library for Java

Azure Core Netty HTTP client is a plugin for the `azure-core` HTTP client API.

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
    <artifactId>azure-core-http-netty</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-core-http-netty;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core-http-netty</artifactId>
    <version>1.16.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

## Examples

The following sections provide several code snippets covering some of the most common client configuration scenarios.

- [Create a Simple Client](#create-a-simple-client)
- [Create a Client with Proxy](#create-a-client-with-proxy)
- [Create a Client with HTTP/2 Support](#create-a-client-with-http2-support)
- [Create a Client with Custom Max Chunk Size](#create-a-client-with-custom-max-chunk-size)

### Create a Simple Client

Create a Netty HttpClient that uses port 80 and has no proxy.

```java readme-sample-createBasicClient
HttpClient client = new NettyAsyncHttpClientBuilder().build();
```

### Create a Client with Proxy

Create a Netty HttpClient that is using a proxy.

```java readme-sample-createProxyClient
HttpClient client = new NettyAsyncHttpClientBuilder()
    .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888)))
    .build();
```

### Create a Client with Authenticated Proxy

```java readme-sample-createAuthenticatedProxyClient
HttpClient client = new NettyAsyncHttpClientBuilder()
    .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888))
        .setCredentials("<username>", "<password>"))
    .build();
```

Authenticated proxies have a few unique behaviors not seen with unauthenticated proxies.

1. Authenticated proxies use a custom Netty `ChannelHandler` to apply `Proxy-Authorization` to the proxy `CONNECT`.
2. Authenticated proxies defer applying `Proxy-Authorization` when `CONNECT` is called, waiting for the proxy to respond
   with `Proxy-Authenticate`. This better supports `Digest` authorization that may require information from the proxy 
   and prevents sending credential information when it isn't needed.
3. Authenticated proxies will use either Netty's `NoopAddressResolverGroup.INSTANCE` or a customer `AddressResolverGroup`
   when there wasn't one configured by a provided Reactor Netty `HttpClient` to `NettyAsyncHttpClientBuilder` and when 
   no Reactor Netty `HttpClient` was provided. See the following sample on non-proxy hosts for more details.

### Create a Client with non-proxy hosts proxy

```java readme-sample-createProxyWithNonProxyHostsClient
HttpClient client = new NettyAsyncHttpClientBuilder()
    .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888))
        .setCredentials("<username>", "<password>")
        .setNonProxyHosts("<nonProxyHostRegex>"))
    .build();
```

A proxy with non-proxy hosts will use a special `AddressResolverGroup` if one isn't configured by a passed Reactor Netty
`HttpClient` or if a Reactor Netty `HttpClient` wasn't passed. This `AddressResolverGroup` will use 
`NoopAddressResolverGroup.INSTANCE` to no-op address resolution when the proxy will be used, deferring address 
resolution to the proxy itself, and will use `DefaultAddressResolverGroup.INSTANCE` to resolve the address when the 
proxy won't be used.

If this handling causes issue, you can pass a Reactor Netty `HttpClient` with an `AddressResolverGroup` configured.
`NettyAsyncHttpClientBuilder` respects the pre-configured `AddressResolverGroup` and won't override it when adding
proxy configurations to the Reactor Netty `HttpClient`.

```java readme-sample-createProxyWithNonProxyHostsClientCustomResolver
// Create a Reactor Netty HttpClient with a configured AddressResolverGroup to override the default behavior
// of NettyAsyncHttpClientBuilder.
//
// Passing DefaultAddressResolverGroup here will prevent issues with NoopAddressResolverGroup where it won't
// resolve the address of a non-proxy host.
//
// This may run into other issues when calling proxied hosts that the client machine cannot resolve.
reactor.netty.http.client.HttpClient reactorNettyHttpClient = reactor.netty.http.client.HttpClient.create()
    .resolver(DefaultAddressResolverGroup.INSTANCE);

HttpClient client = new NettyAsyncHttpClientBuilder(reactorNettyHttpClient)
    .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888))
        .setCredentials("<username>", "<password>")
        .setNonProxyHosts("<nonProxyHostRegex>"))
    .build();
```

### Create a Client with HTTP/2 Support

Create a Netty HttpClient that supports both the HTTP/1.1 and HTTP/2 protocols, with HTTP/2 being the preferred
protocol.

```java readme-sample-useHttp2WithConfiguredNettyClient 
// Constructs an HttpClient that supports both HTTP/1.1 and HTTP/2 with HTTP/2 being the preferred protocol.
HttpClient client = new NettyAsyncHttpClientBuilder(reactor.netty.http.client.HttpClient.create()
    .protocol(HttpProtocol.HTTP11, HttpProtocol.H2))
    .build();
```

It is also possible to create a Netty HttpClient that only supports HTTP/2.

```java readme-sample-useHttp2OnlyWithConfiguredNettyClient
// Constructs an HttpClient that only supports HTTP/2.
HttpClient client = new NettyAsyncHttpClientBuilder(reactor.netty.http.client.HttpClient.create()
    .protocol(HttpProtocol.H2))
    .build();
```

### Create a Client with Custom Max Chunk Size

Create a Netty HttpClient that uses a custom max chunk size.

```java readme-sample-customMaxChunkSize
// Constructs an HttpClient with a modified max chunk size.
// Max chunk size modifies the maximum size of ByteBufs returned by Netty (later converted to ByteBuffer).
// Changing the chunk size can positively impact performance of APIs such as Storage's download to file methods
// provided in azure-storage-blob, azure-storage-file-datalake, and azure-storage-file-shares (32KB - 64KB have
// shown the most consistent improvement).
HttpClient httpClient = new NettyAsyncHttpClientBuilder(reactor.netty.http.client.HttpClient.create()
    .httpResponseDecoder(httpResponseDecoderSpec -> httpResponseDecoderSpec.maxChunkSize(64 * 1024)))
    .build();
```

### Create an HttpClient with custom maxHeaderSize

Create a Netty HttpClient that uses a custom maxHeaderSize. Use this sample if you're seeing an error such as

```
io.netty.handler.codec.http.TooLongHttpHeaderException: HTTP header is larger than 8192 bytes.
```

```java readme-sample-customMaxHeaderSize
// Constructs an HttpClient with a modified max header size.
// This creates a Netty HttpClient with a max headers size of 256 KB.
HttpClient httpClient = new NettyAsyncHttpClientBuilder(reactor.netty.http.client.HttpClient.create()
    .httpResponseDecoder(httpResponseDecoderSpec -> httpResponseDecoderSpec.maxHeaderSize(256 * 1024)))
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
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-in-Azure-SDK
[jdk_link]: https://learn.microsoft.com/java/azure/jdk/?view=azure-java-stable
[java8_client_compatibility]: https://learn.microsoft.com/azure/security/fundamentals/azure-ca-details?tabs=root-and-subordinate-cas-list#client-compatibility-for-public-pkis


