# Azure Core JDK HTTP plugin library for Java

This is an azure-core HTTP client that makes use of the asynchronous HttpClient that was made generally available as 
part of JDK 11. 

## Getting started

### Prerequisites

- Java Development Kit (JDK) with version 11 or above

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

## Troubleshooting

## Next steps

## Contributing

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft
Azure Projects Contribution Guidelines](http://azure.github.io/guidelines.html).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcore%2Fazure-core-http-jdk-httpclient%2FREADME.png)
