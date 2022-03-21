# Azure management client library for Java (Hybrid)

The Azure Management Libraries for Java (Hybrid) is a higher-level, object-oriented API for *managing* Azure and Azure Stack Hub resources,
that is optimized for ease of use, succinctness and consistency. It uses [API Profiles][api_profile] to allow building hybrid cloud solutions
that target both Azure and Azure Stack Hub.

## We'd love to hear your feedback

We're always working on improving our products and the way we communicate with our users. So we'd love to learn what's working and how we can do better. 

If you haven't already, please take a few minutes to [complete this short survey](https://microsoft.qualtrics.com/jfe/form/SV_ehN0lIk2FKEBkwd?Q_CHL=DOCS) we have put together.

Thank you in advance for your collaboration. We really appreciate your time!

## Documentation

Various documentation is available to help you get started

- **[API reference documentation][docs]**
- **[Code snippets and samples][sample]**

## Migration from older version of Azure management library 

If you are an existing user of the older version of Azure management library for Java (the namespace of old packages contains ``com.microsoft.azure.management.**``) and you are looking for a migration guide to the new version of the SDK, please refer to [this migration guide here](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/MIGRATION_GUIDE.md)

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Include the package

For you convenience, we have provided a multi-service package that includes services shared between Azure and Azure Stack Hub through a common API Profile. We recommend using this package when you are dealing with multiple services.

```xml
<dependency>
  <groupId>com.azure.resourcemanager</groupId>
  <artifactId>azure-resourcemanager</artifactId>
  <version>1.0.0-hybrid</version>
</dependency>
```

The services available are listed as below:

- App Services
- Authorization
- Compute
- Container Registry
- Container Services (AKS)
- DNS
- Event Hubs
- Insight (Monitor)
- Key Vault
- Network
- Resources
- Storage

If you are only interested in using a subset of the services above, you can choose to use single-package services. These
packages follow the same naming patterns and design principals. For example, the Compute package can be used directly
with the following artifact information:

```xml
<dependency>
  <groupId>com.azure.resourcemanager</groupId>
  <artifactId>azure-resourcemanager-compute</artifactId>
  <version>1.0.0-hybrid</version>
</dependency>
```

Note that some features included in service packages may not be available on Azure Stack Hub. For example, see
[Azure Stack Hub VM Considerations][vm_considerations] for a list of difference between Compute on Azure and Azure Stack Hub.

### Include the recommended packages

Azure Management Libraries require a `TokenCredential` implementation for authentication and an `HttpClient` implementation for HTTP client.

`azure-identity` package and `azure-core-http-netty` package provide the default implementation.

[Azure Identity][azure_identity] provides Azure Active Directory token authentication support across the Azure SDK.

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-identity</artifactId>
  <version>1.3.4</version>
</dependency>
```
[//]: # ({x-version-update-end})

[Azure Core Netty HTTP client][azure_core_http_netty] is a plugin for [Azure Core][azure_core] HTTP client API.

[//]: # ({x-version-update-start;com.azure:azure-core-http-netty;dependency})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-netty</artifactId>
  <version>1.10.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

Alternatively, [Azure Core OkHttp HTTP client][azure_core_http_okhttp] is another plugin for HTTP client API.

### Authentication

By default, Azure Active Directory token authentication depends on correct configure of following environment variables.

- `AZURE_CLIENT_ID` for Azure client ID.
- `AZURE_TENANT_ID` for Azure tenant ID.
- `AZURE_CLIENT_SECRET` or `AZURE_CLIENT_CERTIFICATE_PATH` for client secret or client certificate.

In addition, Azure subscription ID can be configured via environment variable `AZURE_SUBSCRIPTION_ID`.

With above configuration, the manager class can be authenticated by following code:

```java com.azure.resourcemanager.authenticate
String armEndpoint = "https://management.<region>.<your-domain>";
AzureProfile profile = new AzureProfile(getAzureEnvironmentFromArmEndpoint(armEndpoint));
TokenCredential credential = new DefaultAzureCredentialBuilder()
    .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
    .build();
AzureResourceManager azure = AzureResourceManager
    .authenticate(credential, profile)
    .withDefaultSubscription();
```

Change `armEndpoint` to point to the Azure Resource Manager endpoint of your Azure Stack Hub. The azure environment's
properties above can be populated with the following example:

```java com.azure.resourcemanager.getazureenvironment
private static AzureEnvironment getAzureEnvironmentFromArmEndpoint(String armEndpoint) {
    // Create HTTP client and request
    HttpClient httpClient = HttpClient.createDefault();

    HttpRequest request = new HttpRequest(HttpMethod.GET,
            String.format("%s/metadata/endpoints?api-version=2019-10-01", armEndpoint))
            .setHeader("accept", "application/json");

    // Execute the request and read the response
    HttpResponse response = httpClient.send(request).block();
    if (response.getStatusCode() != 200) {
        throw new RuntimeException("Failed : HTTP error code : " + response.getStatusCode());
    }
    String body = response.getBodyAsString().block();
    try {
        ArrayNode metadataArray = JacksonAdapter.createDefaultSerializerAdapter()
                .deserialize(body, ArrayNode.class, SerializerEncoding.JSON);

        if (metadataArray == null || metadataArray.isEmpty()) {
            throw new RuntimeException("Failed to find metadata : " + body);
        }

        JsonNode metadata = metadataArray.iterator().next();
        AzureEnvironment azureEnvironment = new AzureEnvironment(new HashMap<String, String>() {
            {
                put("managementEndpointUrl", metadata.at("/authentication/audiences/0").asText());
                put("resourceManagerEndpointUrl", armEndpoint);
                put("galleryEndpointUrl", metadata.at("/gallery").asText());
                put("activeDirectoryEndpointUrl", metadata.at("/authentication/loginEndpoint").asText());
                put("activeDirectoryResourceId", metadata.at("/authentication/audiences/0").asText());
                put("activeDirectoryGraphResourceId", metadata.at("/graph").asText());
                put("storageEndpointSuffix", "." + metadata.at("/suffixes/storage").asText());
                put("keyVaultDnsSuffix", "." + metadata.at("/suffixes/keyVaultDns").asText());
            }
        });
        return azureEnvironment;
    } catch (IOException ioe) {
        ioe.printStackTrace();
        throw new RuntimeException(ioe);
    }
}
```

When targeting a hybrid solution to global Azure instead of your Azure Stack Hub, `AzureEnvironment.AZURE` can be used instead.

See [Authentication][authenticate] for more options.

### Code snippets and samples

See [Samples][sample] for code snippets and samples.

## Key concepts

The key concepts of Azure Management Libraries includes:

- Fluent interface to manage Azure resources.
- Dependency across Azure resources.
- Batch Azure resource provisioning.
- Integration with [Azure role-based access control][rbac].
- Asynchronous operations with [Reactor][reactor]. (Preview)
- Configurable client, e.g. configuring HTTP client, retries, logging, etc.
- [API design][design]
- [API design (Preview)][design_preview]

## Examples

See [Azure management client library for Java (Azure)][resourcemanager_azure] for examples on Azure.

## Troubleshooting

If you encounter any bugs, please file issues via [GitHub Issues](https://github.com/Azure/azure-sdk-for-java/issues/new/choose) 
or checkout [StackOverflow for Azure Java SDK](https://stackoverflow.com/questions/tagged/azure-java-sdk).

### HTTP client

An `HttpClient` implementation must exist on the classpath.
See [Include optional packages](#include-optional-packages).

Latest `azure-identity` package specifies dependency on `azure-core-http-netty` package for convenience.
If you would like to use a different `HttpClient`, please exclude `azure-core-http-netty` from `azure-identity`.

### Enabling logging

Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

Sample code to enable logging in Azure Management Libraries.

```java com.azure.resourcemanager.logging
AzureResourceManager azure = AzureResourceManager
    .configure()
    .withLogLevel(HttpLogDetailLevel.BASIC)
    .authenticate(credential, profile)
    .withDefaultSubscription();
```

### Dependency management

[Azure Core][azure_core] (`azure-core`) is the shared library for  all packages under `com.azure`.
It guarantees backward compatibility.

However, if one accidentally uses an older version of it via transitive dependencies, it might cause problem in runtime.
This case could happen when one module depends on multiple Azure Java SDKs with different versions, which in turn depends on different versions of `azure-core`.

Maven dependency plugin would help to diagnostic this problem.
Here is an artificial example.

```shell
mvn dependency:tree -Dincludes=com.azure:azure-core

[INFO] com.microsoft.azure:azure-sdk-test:jar:1.0-SNAPSHOT
[INFO] \- com.azure:azure-identity:jar:1.2.2:compile
[INFO]    \- com.azure:azure-core:jar:1.12.0:compile
```

We can see the `azure-core` resolved as 1.12.0.

```shell
mvn dependency:tree -Dverbose=true -Dincludes=com.azure:azure-core

[INFO] com.microsoft.azure:azure-sdk-test:jar:1.0-SNAPSHOT
[INFO] +- com.azure:azure-identity:jar:1.2.2:compile
[INFO] |  +- com.azure:azure-core:jar:1.12.0:compile
[INFO] |  \- com.azure:azure-core-http-netty:jar:1.7.1:compile
[INFO] |     \- (com.azure:azure-core:jar:1.12.0:compile - omitted for duplicate)
[INFO] +- com.azure.resourcemanager:azure-resourcemanager:jar:1.0.0:compile
[INFO] |  +- com.azure.resourcemanager:azure-resourcemanager-resources:jar:1.0.0:compile
[INFO] |  |  +- (com.azure:azure-core:jar:1.13.0:compile - omitted for conflict with 1.12.0)
[INFO] |  |  \- com.azure:azure-core-management:jar:1.1.1:compile
[INFO] |  |     \- (com.azure:azure-core:jar:1.13.0:compile - omitted for conflict with 1.12.0)
[INFO] |  \- com.azure.resourcemanager:azure-resourcemanager-keyvault:jar:1.0.0:compile
[INFO] |     +- com.azure:azure-security-keyvault-keys:jar:4.2.5:compile
[INFO] |     |  \- (com.azure:azure-core:jar:1.13.0:compile - omitted for conflict with 1.12.0)
[INFO] |     \- com.azure:azure-security-keyvault-secrets:jar:4.2.5:compile
[INFO] |        \- (com.azure:azure-core:jar:1.13.0:compile - omitted for conflict with 1.12.0)
[INFO] \- com.azure:azure-storage-blob:jar:12.10.2:compile
[INFO]    +- (com.azure:azure-core:jar:1.12.0:compile - omitted for duplicate)
[INFO]    \- com.azure:azure-storage-common:jar:12.10.1:compile
[INFO]       \- (com.azure:azure-core:jar:1.12.0:compile - omitted for duplicate)
```

From the module, we can see there is multiple SDKs depends on different versions of `azure-core`, and the latest would be 1.13.0.

If we run the module, we will encounter this error in runtime.

```
java.lang.NoSuchMethodError: 'com.azure.core.http.HttpHeaders com.azure.core.http.HttpHeaders.set(java.lang.String, java.lang.String)'
```

The cause is that this method was not available in 1.12.0 `azure-core`, and now being used by some SDK that depends on 1.13.0 `azure-core`.

In this example, apparently the problem is that we used an old version of `azure-identity`. After upgrade it to 1.2.3, problem solved.

Better, one can explicitly put `azure-core` as the first dependency, and keep it up-to-date.

Alternatively, maven dependency management will also help to control the version in transitive dependencies.
Here is a sample dependency management section in maven POM.

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.azure</groupId>
      <artifactId>azure-core</artifactId>
      <version>${azure-core.version}</version>
    </dependency>
  </dependencies>
</dependencyManagement>
```

To a lesser extent, similar problem could occur in runtime for `azure-core-management` library, when one module depends on multiple Azure Java management SDKs with different versions.
For example, `azure-resourcemanager` 1.0.0 would require `azure-core-management` 1.3.0 or above, relying on `ArmChallengeAuthenticationPolicy` class for continuous access evaluation support.

### ARM throttling

Azure Resource Manager applies throttling on the number of requests sent from client within certain span of time.
For details, please refer to [Guidance on ARM throttling][throttling].

## Next steps

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

<!-- LINKS -->
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[azure_core_http_netty]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-http-netty
[azure_core_http_okhttp]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-http-okhttp
[azure_core]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[authenticate]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/AUTH.md
[sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/SAMPLE.md
[design]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/DESIGN.md
[design_preview]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/DESIGN_PREVIEW.md
[throttling]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/THROTTLING.md
[reactor]: https://projectreactor.io/
[rbac]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/RBAC.md
[api_profile]: https://docs.microsoft.com/azure-stack/user/azure-stack-version-profiles
[vm_considerations]: https://docs.microsoft.com/azure-stack/user/azure-stack-vm-considerations
[resourcemanager_azure]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/README.md
