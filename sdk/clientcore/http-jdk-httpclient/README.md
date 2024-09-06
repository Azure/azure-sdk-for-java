# Client Core JDK HTTP plugin library for Java

This is a clientcore HTTP client that makes use of the asynchronous HttpClient that was made generally available as 
part of JDK 12. 

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 12 or later.

### Adding the package to your product

[//]: # ({x-version-update-start;io.clientcore:http-jdk-httpclient;current})
```xml
<dependency>
    <groupId>io.clientcore</groupId>
    <artifactId>http-jdk-httpclient</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Java 12 requirement

While the JDK added `HttpClient` in JDK 11, this library requires JDK 12 or later. This is due to certain headers being
disallowed in JDK 11 which JDK 12 allowed to be overridden using system properties. The headers disallowed are required
for the library to function correctly.

## Key concepts

## Examples

The following sections provide several code snippets covering some of the most common client configuration scenarios.

- [Create a Simple Client](#create-a-simple-client)
- [Create a Client with Proxy](#create-a-client-with-proxy)

### Create a Simple Client

Create a HttpClient.

```java readme-sample-createBasicClient
HttpClient client = new JdkHttpClientBuilder().build();
```

Create a HttpClient using a connection timeout of 60 seconds.

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

## Next steps

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- links -->
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-in-Azure-SDK
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fclientcore%2Fhttp-jdk-httpclient%2FREADME.png)
