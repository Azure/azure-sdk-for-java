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
    <version>1.12.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

## Examples

The following sections provide several code snippets covering some of the most common client configuration scenarios.

- [Create a Simple Client](#create-a-simple-client)
- [Create a Client with Proxy](#create-a-client-with-proxy)

### Create a Simple Client

Create an OkHttp client using a connection timeout of 60 seconds and a read timeout of 120 seconds.

```java readme-sample-createBasicClient
HttpClient client = new OkHttpAsyncHttpClientBuilder().build();
```

### Create a Client with Proxy

Create an OkHttp client that is using a proxy.

```java readme-sample-createProxyClient
HttpClient client = new OkHttpAsyncHttpClientBuilder()
    .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888)))
    .build();
```

### Create a Client with HTTP/2 Support

Create an OkHttp client that supports both the HTTP/1.1 and HTTP/2 protocols, with HTTP/2 being the preferred protocol.

```java readme-sample-useHttp2WithConfiguredOkHttpClient 
// Constructs an HttpClient that supports both HTTP/1.1 and HTTP/2 with HTTP/2 being the preferred protocol.
// This is the default handling for OkHttp.
HttpClient client = new OkHttpAsyncHttpClientBuilder(new OkHttpClient.Builder()
    .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
    .build())
    .build();
```

It is also possible to create an OkHttp client that only supports HTTP/2.

```java readme-sample-useHttp2OnlyWithConfiguredOkHttpClient
// Constructs an HttpClient that only supports HTTP/2.
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
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-in-Azure-SDK
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[java8_client_compatibility]: https://learn.microsoft.com/azure/security/fundamentals/azure-ca-details?tabs=root-and-subordinate-cas-list#client-compatibility-for-public-pkis

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcore%2Fazure-core-http-okhttp%2FREADME.png)
