# Azure management client library for Java

The Azure Management Libraries for Java is a higher-level, object-oriented API for *managing* Azure resources,
that is optimized for ease of use, succinctness and consistency.

- [API reference documentation][docs]
- [Code snippets and samples][sample]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-management;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-management</artifactId>
  <version>2.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Include the recommended packages

Azure Management Libraries require a `TokenCredential` implementation for authentication and an `HttpClient` implementation for HTTP client.

`azure-identity` package and `azure-core-http-netty` package provide the default implementation.

[Azure Identity][azure_identity] provides Azure Active Directory token authentication support across the Azure SDK.

[//]: # ({x-version-update-start;com.azure:azure-identity;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-identity</artifactId>
  <version>1.1.0-beta.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

[Azure Core Netty HTTP client][azure_core_http_netty] is a plugin for [Azure Core][azure_core] HTTP client API.

[//]: # ({x-version-update-start;com.azure:azure-core-http-netty;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-netty</artifactId>
  <version>1.5.0</version>
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

With above configuration, `azure` client can be authenticated by following code:

```java
AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE, true);
TokenCredential credential = new DefaultAzureCredentialBuilder()
    .authorityHost(profile.environment().getActiveDirectoryEndpoint())
    .build();
Azure azure = Azure
    .authenticate(credential, profile)
    .withDefaultSubscription();
```

See [Authentication][authenticate] for more options.

### Code snippets and samples

See [Samples][sample] for code snippets and samples.

## Key concepts

The key concepts of Azure Management Libraries includes:

- Fluent interface to manage Azure resources.
- Asynchronous operations with [Reactor][reactor].
- Configurable client, e.g. configuring HTTP client, retries, logging, etc.

### Service features

- Compute
- Storage
- Networking
- SQL Database
- Container and Kubernetes (AKS)
- Web app and Function app
- Key Vault
- Cosmos

## Examples

### Fluent interface

You can create a virtual machine instance, together with required virtual network and ip address.

```java
VirtualMachine linuxVM = azure.virtualMachines().define("myLinuxVM")
    .withRegion(Region.US_EAST)
    .withNewResourceGroup(rgName)
    .withNewPrimaryNetwork("10.0.0.0/28")
    .withPrimaryPrivateIPAddressDynamic()
    .withNewPrimaryPublicIPAddress("mylinuxvm")
    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
    .withRootUsername("tirekicker")
    .withSsh(sshKey)
    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
    .create();
```

Update.

```java
linuxVM.update()
    .withNewDataDisk(20, lun, CachingTypes.READ_WRITE)
    .apply();
```

### Asynchronous operations

You can create storage account, then blob container, in reactive programming. 

```java
azure.storageAccounts().define("mystorageaccount")
    .withRegion(Region.US_EAST)
    .withNewResourceGroup(rgName)
    .withSku(StorageAccountSkuType.STANDARD_LRS)
    .withGeneralPurposeAccountKindV2()
    .withOnlyHttpsTraffic()
    .createAsync()
    .filter(indexable -> indexable instanceof StorageAccount)
    .last()
    .flatMapMany(indexable -> azure.storageBlobContainers()
        .defineContainer("container")
        .withExistingBlobService(rgName, ((StorageAccount) indexable).name())
        .withPublicAccess(PublicAccess.BLOB)
        .createAsync()
    ).blockLast();
```

### Configurable client

You can customize various aspects of the client.

```java
Azure azure = Azure
    .configure()
    .withHttpClient(customizedHttpClient)
    .withPolicy(additionalPolicy)
    .withConfiguration(customizedConfiguration)
    ...
```

### Include single package

Instead of include the complete Azure Management Libraries, you can choose to include a single service package.

For example, here is sample maven dependency for Compute package.

[//]: # ({x-version-update-start;com.azure:azure-mgmt-compute;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-mgmt-compute</artifactId>
  <version>2.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

Sample code to create the authenticated client.
```java
ComputeManager client = ComputeManager.authenticate(credential, profile);
client.virtualMachines().listByResourceGroup(rgName);
```

## Troubleshooting

If you encounter any bugs, please file issues via [GitHub Issues](https://github.com/Azure/azure-sdk-for-java/issues/new/choose) 
or checkout [StackOverflow for Azure Java SDK](http://stackoverflow.com/questions/tagged/azure-java-sdk).

### HTTP client

An `HttpClient` implementation must exist on the classpath.
See [Include optional packages](#include-optional-packages).

### Enabling logging

Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

Sample code to enable logging in Azure Management Libraries.
```java
Azure azure = Azure
    .configure()
    .withLogLevel(HttpLogDetailLevel.BASIC)
    .authenticate(credential, profile)
    .withDefaultSubscription();
```

## Next steps

## Contributing

If you would like to become an active contributor to this project please follow the instructions provided in
[Microsoft Azure Projects Contribution Guidelines](http://azure.github.io/guidelines.html).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

<!-- LINKS -->
[docs]: http://azure.github.io/azure-sdk-for-java/
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: ../identity/azure-identity
[azure_core_http_netty]: ../core/azure-core-http-netty
[azure_core_http_okhttp]: ../core/azure-core-http-okhttp
[azure_core]: ../core/azure-core
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[authenticate]: docs/AUTH.md
[sample]: docs/SAMPLE.md
[reactor]: https://projectreactor.io/
