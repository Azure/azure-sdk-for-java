# Azure management client library for Java

The Azure Management Libraries for Java is a higher-level, object-oriented API for *managing* Azure resources,
that is optimized for ease of use, succinctness and consistency.

## Documentation

Various documentation is available to help you get started

- **[API reference documentation][docs]**
- **[Code snippets and samples][sample]**

## Migration from older version of Azure management library 

If you are an existing user of the older version of Azure management library for Java (the namespace of old packages contains ``com.microsoft.azure.management.**``) and you are looking for a migration guide to the new version of the SDK, please refer to [this migration guide here](docs/MIGRATION_GUIDE.md)

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-resourcemanager;current})
```xml
<dependency>
  <groupId>com.azure.resourcemanager</groupId>
  <artifactId>azure-resourcemanager</artifactId>
  <version>2.0.0-beta.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Include the recommended packages

Azure Management Libraries require a `TokenCredential` implementation for authentication and an `HttpClient` implementation for HTTP client.

`azure-identity` package and `azure-core-http-netty` package provide the default implementation.

[Azure Identity][azure_identity] provides Azure Active Directory token authentication support across the Azure SDK.

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-identity</artifactId>
  <version>1.1.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

[Azure Core Netty HTTP client][azure_core_http_netty] is a plugin for [Azure Core][azure_core] HTTP client API.

[//]: # ({x-version-update-start;com.azure:azure-core-http-netty;dependency})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-netty</artifactId>
  <version>1.5.4</version>
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
AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
TokenCredential credential = new DefaultAzureCredentialBuilder()
    .authorityHost(profile.environment().getActiveDirectoryEndpoint())
    .build();
Azure azure = Azure
    .authenticate(credential, profile)
    .withDefaultSubscription();
```

The sample code assumes global Azure. Please change `AzureEnvironment.AZURE` variable if otherwise.

See [Authentication][authenticate] for more options.

### Code snippets and samples

See [Samples][sample] for code snippets and samples.

## Key concepts

The key concepts of Azure Management Libraries includes:

- Fluent interface to manage Azure resources.
- Dependency across Azure resources.
- Batch Azure resource provisioning.
- Integration with Azure role-based access control.
- Asynchronous operations with [Reactor][reactor]. (Preview)
- Configurable client, e.g. configuring HTTP client, retries, logging, etc.
- [API design][design]
- [API design (preview)][design_preview]

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

You can create a virtual machine instance, together with required virtual network and ip address created automatically.

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

### Dependency across Azure resources

You can create a function app, together with required storage account and app service plan created on specification.

```java
Creatable<StorageAccount> creatableStorageAccount = azure.storageAccounts()
    .define(storageAccountName)
    .withRegion(Region.US_EAST)
    .withExistingResourceGroup(rgName)
    .withGeneralPurposeAccountKindV2()
    .withSku(StorageAccountSkuType.STANDARD_LRS);

Creatable<AppServicePlan> creatableAppServicePlan = azure.appServicePlans()
    .define(appServicePlanName)
    .withRegion(Region.US_EAST)
    .withExistingResourceGroup(rgName)
    .withPricingTier(PricingTier.STANDARD_S1)
    .withOperatingSystem(OperatingSystem.LINUX);

FunctionApp linuxFunctionApp = azure.functionApps().define(functionAppName)
    .withRegion(Region.US_EAST)
    .withExistingResourceGroup(rgName)
    .withNewLinuxAppServicePlan(creatableAppServicePlan)
    .withBuiltInImage(FunctionRuntimeStack.JAVA_8)
    .withNewStorageAccount(creatableStorageAccount)
    .withHttpsOnly(true)
    .withAppSetting("WEBSITE_RUN_FROM_PACKAGE", functionAppPackageUrl)
    .create();
```

### Batch Azure resource provisioning

You can batch create and delete managed disk instances.

```java
List<String> diskNames = Arrays.asList("datadisk1", "datadisk2");

List<Creatable<Disk>> creatableDisks = diskNames.stream()
    .map(diskName -> azure.disks()
        .define(diskName)
        .withRegion(Region.US_EAST)
        .withExistingResourceGroup(rgName)
        .withData()
        .withSizeInGB(1)
        .withSku(DiskSkuTypes.STANDARD_LRS))
    .collect(Collectors.toList());

Collection<Disk> disks = azure.disks().create(creatableDisks).values();

azure.disks().deleteByIds(disks.stream().map(Disk::id).collect(Collectors.toList()));
```

### Integration with Azure role-based access control

You can assign Contributor for an Azure resource to a service principal.

```java
String raName = UUID.randomUUID().toString();
RoleAssignment roleAssignment = azure.accessManagement().roleAssignments()
    .define(raName)
    .forServicePrincipal(servicePrincipal)
    .withBuiltInRole(BuiltInRole.CONTRIBUTOR)
    .withScope(resource.id())
    .create();
```

### Asynchronous operations (Preview)

You can create storage account, then blob container, in reactive programming.

```java
azure.storageAccounts().define(storageAccountName)
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
    )
    ...
```

You can operate on virtual machines in parallel.

```java
azure.virtualMachines().listByResourceGroupAsync(rgName)
    .flatMap(VirtualMachine::restartAsync)
    ...
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

[//]: # ({x-version-update-start;com.azure:azure-resourcemanager-compute;current})
```xml
<dependency>
  <groupId>com.azure.resourcemanager</groupId>
  <artifactId>azure-resourcemanager-compute</artifactId>
  <version>2.0.0-beta.3</version>
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
[design]: docs/DESIGN.md
[design_preview]: docs/DESIGN_PREVIEW.md
[reactor]: https://projectreactor.io/
