# Azure Core OkHttp HTTP client library for Java

Azure Core OkHttp HTTP client is a plugin for the azure-core HTTP client API. 

## Getting started

### Prerequisites

- Java Development Kit (JDK) with version 8 or above

### Adding the package to your product

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core-http-okhttp</artifactId>
    <version>1.0.0-preview.6</version>
</dependency>
```

## Key concepts

## Examples

The following sections provide several code snippets covering some of the most common client configuration scenarios.

- [Create a Simple Client](#create-a-simple-client)
- [Create a Client with Proxy](#create-a-client-with-proxy)

### Create a Simple Client

Create an OkHttp client using a connection timeout of 60 seconds and a read timeout of 120 seconds.

```java
HttpClient client = new OkHttpAsyncHttpClientBuilder().build();
```

### Create a Client with Proxy

Create an OkHttp client that is using a proxy.

```java
HttpClient client = new OkHttpAsyncHttpClientBuilder()
    .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888)))
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

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/core/azure-core-http-okhttp/README.png)
