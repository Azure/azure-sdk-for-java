# Client Core OkHttp HTTP plugin library for Java

Client Core OkHttp HTTP client is a plugin for the `io.clientcore.core` HTTP client API.

## Getting started

### Prerequisites

- A Java Development Kit (JDK), version 17 or later.

### Include the package

[//]: # ({x-version-update-start;io.clientcore:http-okhttp3;current})
```xml
<dependency>
    <groupId>io.clientcore</groupId>
    <artifactId>http-okhttp3</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

## Examples

The following sections provide several code snippets covering some of the most common client configuration scenarios.

- [Create a Simple Client](#create-a-simple-client)
- [Create a Client with Proxy](#create-a-client-with-proxy)

### Create a Simple Client

Create an OkHttp client.

```java readme-sample-createBasicClient
HttpClient client = new OkHttpHttpClientBuilder().build();
```

### Create a Client with Proxy

Create an OkHttp client that is using a proxy.

```java readme-sample-createProxyClient
HttpClient client = new OkHttpHttpClientBuilder()
    .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888)))
    .build();
```

### Create a Client with HTTP/2 Support

Create an OkHttp client that supports both the HTTP/1.1 and HTTP/2 protocols, with HTTP/2 being the preferred protocol.

```java readme-sample-useHttp2WithConfiguredOkHttpClient 
// Constructs an HttpClient that supports both HTTP/1.1 and HTTP/2 with HTTP/2 being the preferred protocol.
// This is the default handling for OkHttp.
HttpClient client = new OkHttpHttpClientBuilder(
    new OkHttpClient.Builder().protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1)).build())
    .build();
```

It is also possible to create an OkHttp client that only supports HTTP/2.

```java readme-sample-useHttp2OnlyWithConfiguredOkHttpClient
// Constructs an HttpClient that only supports HTTP/2.
HttpClient client = new OkHttpHttpClientBuilder(
    new OkHttpClient.Builder().protocols(Collections.singletonList(Protocol.H2_PRIOR_KNOWLEDGE)).build())
    .build();
```

## Troubleshooting

If you encounter any bugs, please file issues via [GitHub Issues](https://github.com/Azure/azure-sdk-for-java/issues/new/choose)
or checkout [StackOverflow for Azure Java SDK](https://stackoverflow.com/questions/tagged/azure-java-sdk).

### Enabling Logging

Client Core libraries for Java provide a consistent logging story to help aid in troubleshooting application errors and
expedite their resolution. The logs produced will capture the flow of an application before reaching the terminal state
to help locate the root issue.

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request
