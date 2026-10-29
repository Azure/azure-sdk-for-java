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
    <version>1.3.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

### Request host overrides

The request URL determines the connection destination. By default, the SDK filters out the restricted `Host` header
and logs a warning. To allow an explicit `Host` header, start the application JVM with:

```text
java -Djdk.httpclient.allowRestrictedHeaders=host -jar application.jar
```

If `jdk.httpclient.allowRestrictedHeaders` already lists other headers, add `host` to that comma-separated list.
The JDK caches this setting, so it must be configured before the JDK HTTP client is initialized.

The default SDK configuration reads this system property. If you supply a custom `Configuration` to
`JdkHttpClientBuilder.configuration(...)`, include the same setting there so the SDK's header filter also permits `Host`.
`Configuration.NONE` does not read system properties. Setting the property only in the builder's configuration does not
enable the native JDK override.
Requests without an explicit `Host` header continue to use the URL's authority.

This setting affects all JDK HTTP clients in the JVM. The JDK documents it as a testing-oriented option; consult the
[JDK HTTP client properties](https://docs.oracle.com/en/java/javase/25/docs/api/java.net.http/module-summary.html#jdk.httpclient.allowRestrictedHeaders)
for its restrictions.

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
