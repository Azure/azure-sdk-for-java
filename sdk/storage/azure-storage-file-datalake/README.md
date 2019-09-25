# Azure File Data Lake client library for Java

[Source code][source_code] | [API reference documentation][api_documentation] | [Product documentation][storage_docs] |
[Samples][samples]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Create Storage Account][storage_account]

### Adding the package to your product

```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-storage-file-datalake</artifactId>
  <version>12.0.0-preview.4</version>
</dependency>
```

### Default HTTP Client
All client libraries support a pluggable HTTP transport layer. Users can specify an HTTP client specific for their needs by including the following dependency in the Maven pom.xml file:

```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-netty</artifactId>
  <version>1.0.0-preview.4</version>
</dependency>
```

This will automatically configure all client libraries on the same classpath to make use of Netty for the HTTP client. Netty is the recommended HTTP client for most applications. OkHttp is recommended only when the application being built is deployed to Android devices.

If, instead of Netty it is preferable to use OkHTTP, there is a HTTP client available for that too. Simply include the following dependency instead:

```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-okhttp</artifactId>
  <version>1.0.0-preview.4</version>
</dependency>
```

### Configuring HTTP Clients
When an HTTP client is included on the classpath, as shown above, it is not necessary to specify it in the client library [builders](#file-services), unless you want to customize the HTTP client in some fashion. If this is desired, the `httpClient` builder method is often available to achieve just this, by allowing users to provide a custom (or customized) `com.azure.core.http.HttpClient` instances.

For starters, by having the Netty or OkHTTP dependencies on your classpath, as shown above, you can create new instances of these `HttpClient` types using their builder APIs. For example, here is how you would create a Netty HttpClient instance:

```java
HttpClient client = new NettyAsyncHttpClientBuilder()
    .port(8080)
    .wiretap(true)
    .build();
```

### Create a Storage Account
To create a Storage Account you can use the Azure Portal or [Azure CLI][azure_cli].

```powershell
az group create \
    --name storage-resource-group \
    --location westus
```
