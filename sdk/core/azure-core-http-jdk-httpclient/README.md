# Azure Core JDK HTTP plugin library for Java

This is an azure-core HTTP client that makes use of the asynchronous HttpClient that was made generally available as 
part of JDK 11. 

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 11 or later.

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-core-http-jdk-httpclient;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core-http-jdk-httpclient</artifactId>
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

Create a HttpClient.

<!-- embedme ./src/samples/java/com/azure/core/http/jdk/httpclient/ReadmeSamples.java#L24-L24 -->
```java
HttpClient client = new JdkAsyncHttpClientBuilder().build();
```

Create a HttpClient using a connection timeout of 60 seconds.

<!-- embedme ./src/samples/java/com/azure/core/http/jdk/httpclient/ReadmeSamples.java#L31-L31 -->
```java
HttpClient client = new JdkAsyncHttpClientBuilder().connectionTimeout(Duration.ofSeconds(60)).build();
```

### Create a Client with Proxy

Create a HttpClient that is using a proxy.

<!-- embedme ./src/samples/java/com/azure/core/http/jdk/httpclient/ReadmeSamples.java#L38-L40 -->
```java
HttpClient client = new JdkAsyncHttpClientBuilder()
    .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888)))
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

<!-- links -->
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcore%2Fazure-core-http-jdk-httpclient%2FREADME.png)
