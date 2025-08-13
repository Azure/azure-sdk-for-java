# Azure management client library for Java

The Azure Management Libraries for Java is a higher-level, object-oriented API for *managing* Azure resources,
that is optimized for ease of use, succinctness and consistency.

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
#### Include the BOM file
From 1.2.36 and above, azure-sdk-bom includes `azure-resourcemanager`.

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
    <groupId>com.azure.resourcemanager</groupId>
    <artifactId>azure-resourcemanager</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
For your convenience, we have provided a multi-service package that includes some of the most highly used Azure services. We recommend using this package when you are dealing with multiple services.

[//]: # ({x-version-update-start;com.azure.resourcemanager:azure-resourcemanager;current})
```xml
<dependency>
  <groupId>com.azure.resourcemanager</groupId>
  <artifactId>azure-resourcemanager</artifactId>
  <version>2.53.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

The services available via `azure-resourcemanager` are listed as below:

<details>
<summary> List of services </summary>

- App Services
- Authorization
- CDN
- Compute
- Container Instance
- Container Registry
- Container Services (AKS)
- Cosmos DB
- DNS
- Event Hubs
- Insight (Monitor)
- Key Vault
- Managed Identity
- Network
- Private DNS
- Redis
- Resources
- Search
- Service Bus
- Spring Cloud
- SQL
- Storage
- Traffic Manager
</details>

In the case where you are interested in certain service above or the service not included in the multi-service package, you can choose to use the single-service package for each service. Those packages follow the same naming patterns and design principals. For example, the package for Media Services has the following artifact information.

[//]: # ({x-version-update-start;com.azure.resourcemanager:azure-resourcemanager-resourcegraph;dependency})
```xml
<dependency>
  <groupId>com.azure.resourcemanager</groupId>
  <artifactId>azure-resourcemanager-resourcegraph</artifactId>
  <version>1.1.0</version> <!-- {x-version-update;com.azure.resourcemanager:azure-resourcemanager-resourcegraph;dependency} -->
</dependency>
```
[//]: # ({x-version-update-end})

See [Single-Service Packages][single_service_packages] for a complete list of single-services packages with the API versions they are consuming.

### Include the recommended packages

Azure Management Libraries require a `TokenCredential` implementation for authentication and an `HttpClient` implementation for HTTP client.

`azure-identity` package and `azure-core-http-netty` package provide the default implementation.

[Azure Identity][azure_identity] provides Microsoft Entra ID token authentication support across the Azure SDK.

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-identity</artifactId>
  <version>1.16.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

[Azure Core Netty HTTP client][azure_core_http_netty] is a plugin for [Azure Core][azure_core] HTTP client API.

[//]: # ({x-version-update-start;com.azure:azure-core-http-netty;dependency})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-netty</artifactId>
  <version>1.15.13</version>
</dependency>
```
[//]: # ({x-version-update-end})

Alternatively, [Azure Core OkHttp HTTP client][azure_core_http_okhttp] is another plugin for HTTP client API.

### Authentication

Microsoft Entra ID token authentication relies on the [credential class][azure_identity_credentials] from [Azure Identity][azure_identity] package.

Azure subscription ID can be configured via `AZURE_SUBSCRIPTION_ID` environment variable.
Azure tenant ID can be configured via `AZURE_TENANT_ID` environment variable.

Assuming the use of the `DefaultAzureCredential` credential class, the client can be authenticated using the following code:

```java readme-sample-authenticate
AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
TokenCredential credential = new DefaultAzureCredentialBuilder()
    .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
    .build();
AzureResourceManager azure = AzureResourceManager
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
- Integration with [Azure role-based access control][rbac].
- Asynchronous operations with [Reactor][reactor]. (Preview)
- Configurable client, e.g. configuring HTTP client, retries, logging, etc.
- [API design][design]
- [API design (Preview)][design_preview]

## Examples

### Fluent interface

You can create a virtual machine instance, together with required virtual network and ip address created automatically.

```java readme-sample-createVirtualMachine
VirtualMachine linuxVM = azure.virtualMachines().define("myLinuxVM")
    .withRegion(Region.US_EAST)
    .withNewResourceGroup(rgName)
    .withNewPrimaryNetwork("10.0.0.0/28")
    .withPrimaryPrivateIPAddressDynamic()
    .withoutPrimaryPublicIPAddress()
    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
    .withRootUsername("<username>")
    .withSsh("<ssh-key>")
    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
    .create();
```

Update.

```java readme-sample-updateVirtualMachine
linuxVM.update()
    .withNewDataDisk(10, 0, CachingTypes.READ_WRITE)
    .apply();
```

### Dependency across Azure resources

You can create a function app, together with required storage account and app service plan created on specification.

```java readme-sample-createFunctionApp
Creatable<StorageAccount> creatableStorageAccount = azure.storageAccounts()
    .define("<storage-account-name>")
    .withRegion(Region.US_EAST)
    .withExistingResourceGroup(rgName)
    .withGeneralPurposeAccountKindV2()
    .withSku(StorageAccountSkuType.STANDARD_LRS);
Creatable<AppServicePlan> creatableAppServicePlan = azure.appServicePlans()
    .define("<app-service-plan-name>")
    .withRegion(Region.US_EAST)
    .withExistingResourceGroup(rgName)
    .withPricingTier(PricingTier.STANDARD_S1)
    .withOperatingSystem(OperatingSystem.LINUX);
FunctionApp linuxFunctionApp = azure.functionApps().define("<function-app-name>")
    .withRegion(Region.US_EAST)
    .withExistingResourceGroup(rgName)
    .withNewLinuxAppServicePlan(creatableAppServicePlan)
    .withBuiltInImage(FunctionRuntimeStack.JAVA_8)
    .withNewStorageAccount(creatableStorageAccount)
    .withHttpsOnly(true)
    .withAppSetting("WEBSITE_RUN_FROM_PACKAGE", "<function-app-package-url>")
    .create();
```

### Batch Azure resource provisioning

You can batch create and delete managed disk instances.

```java readme-sample-batchCreateAndDeleteDisk
List<String> diskNames = Arrays.asList("datadisk1", "datadisk2");
List<Creatable<Disk>> creatableDisks = diskNames.stream()
    .map(diskName -> azure.disks()
        .define(diskName)
        .withRegion(Region.US_EAST)
        .withExistingResourceGroup(rgName)
        .withData()
        .withSizeInGB(10)
        .withSku(DiskSkuTypes.STANDARD_LRS))
    .collect(Collectors.toList());
Collection<Disk> disks = azure.disks().create(creatableDisks).values();
azure.disks().deleteByIds(disks.stream().map(Disk::id).collect(Collectors.toList()));
```

### Integration with Azure role-based access control

You can assign Contributor for an Azure resource to a service principal.

```java readme-sample-createRoleAssignment
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

```java readme-sample-createStorageAccountAndBlobContainerAsync
azure.storageAccounts().define("<storage-account-name>")
    .withRegion(Region.US_EAST)
    .withNewResourceGroup(rgName)
    .withSku(StorageAccountSkuType.STANDARD_LRS)
    .withGeneralPurposeAccountKindV2()
    .withOnlyHttpsTraffic()
    .createAsync()
    .flatMap(storageAccount -> azure.storageBlobContainers()
        .defineContainer("container")
        .withExistingStorageAccount(storageAccount)
        .withPublicAccess(PublicAccess.NONE)
        .createAsync()
    )
    //...
```

You can operate on virtual machines in parallel.

```java readme-sample-restartVirtualMachineAsync
azure.virtualMachines().listByResourceGroupAsync(rgName)
    .flatMap(VirtualMachine::restartAsync)
    //...
```

### Configurable client

You can customize various aspects of the client and pipeline.

```java readme-sample-configureClientAndPipeline
AzureResourceManager azure = AzureResourceManager
    .configure()
    .withHttpClient(customizedHttpClient)
    .withPolicy(additionalPolicy)
    //...
```

### Include single package

Instead of include the complete Azure Management Libraries, you can choose to include a single service package.

For example, here is sample maven dependency for Compute package.

[//]: # ({x-version-update-start;com.azure.resourcemanager:azure-resourcemanager-compute;current})
```xml
<dependency>
  <groupId>com.azure.resourcemanager</groupId>
  <artifactId>azure-resourcemanager-compute</artifactId>
  <version>2.53.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

Or if you are using BOM:
```xml
<dependency>
  <groupId>com.azure.resourcemanager</groupId>
  <artifactId>azure-resourcemanager-compute</artifactId>
</dependency>
```

Sample code to create the authenticated client.

```java readme-sample-authenticateComputeManager
ComputeManager manager = ComputeManager.authenticate(credential, profile);
manager.virtualMachines().list();
```

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
locate the root issue. View the [configure logging][logging] for guidance on package required and its configuration.

Sample code to enable logging in Azure Management Libraries for Java. If you want to troubleshoot HTTP request and response details, you can use `.withLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)`.

```java readme-sample-configureWithLogging
AzureResourceManager azure = AzureResourceManager
    .configure()
    .withLogLevel(HttpLogDetailLevel.BASIC)
    .authenticate(credential, profile)
    .withDefaultSubscription();
```

### Mocking SDK

Please refer to [Unit Testing][unit_test] for guidance on mocking the SDK clients.
In particular, since many classes in clients are `final`, "Mocking Final classes" section would be helpful on mocking `final` classes in Mockito.

As Azure Management Libraries for Java uses Fluent interface extensively, it takes some extra effects to mock the Fluent interface.
Samples can be found [here](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/azure-resourcemanager/src/samples/java/com/azure/resourcemanager/MockSdkSamples.java).

### Dependency management

We now recommend using azure-sdk-bom for easier dependency management. See [Include the BOM file](#include-the-bom-file).

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
[INFO] +- com.azure.resourcemanager:azure-resourcemanager:jar:2.2.0:compile
[INFO] |  +- com.azure.resourcemanager:azure-resourcemanager-resources:jar:2.2.0:compile
[INFO] |  |  +- (com.azure:azure-core:jar:1.13.0:compile - omitted for conflict with 1.12.0)
[INFO] |  |  \- com.azure:azure-core-management:jar:1.1.1:compile
[INFO] |  |     \- (com.azure:azure-core:jar:1.13.0:compile - omitted for conflict with 1.12.0)
[INFO] |  \- com.azure.resourcemanager:azure-resourcemanager-keyvault:jar:2.2.0:compile
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
For example, `azure-resourcemanager` 2.6.0 would require `azure-core-management` 1.3.0 or above, relying on `ArmChallengeAuthenticationPolicy` class for continuous access evaluation support.

### ARM throttling

Azure Resource Manager applies throttling on the number of requests sent from client within certain span of time.
For details, please refer to [Guidance on ARM throttling][throttling].

### HTTP header is larger than 8192 bytes

If you are seeing this error message, please upgrade `azure-core-http-netty` to 1.15.12 or above. If you are using BOM, take 1.2.36 or above.

[reactor-netty-http](https://projectreactor.io/docs/netty/release/reference/http-client.html) has a default max response header limit of 8192 bytes, configured by `io.netty.handler.codec.http.HttpResponseDecoder`.

Azure Resource Manager will now append cryptographic token to the LRO (long-running-operation) URL returned in response header.
This will make [LRO URLs](https://learn.microsoft.com/azure/azure-resource-manager/management/async-operations#url-to-monitor-status) significantly larger, which sometimes contributes to an HTTP response header over 8192 bytes size in total.

In this case, Netty's HttpClient will throw:
```
io.netty.handler.codec.http.TooLongHttpHeaderException: HTTP header is larger than 8192 bytes.
```

From `azure-core-http-netty` 1.15.12 and above, we increased the limit to 256 KB to allow for these large LRO headers.
[Increase HTTP header size limit in Netty-based HttpClients](https://github.com/Azure/azure-sdk-for-java/pull/45291)

### Duplicate request sent out seconds apart

It is likely caused by [HTTP response header larger than 8192 bytes](#http-header-is-larger-than-8192-bytes).
Our `RetryPolicy` would retry and send out new request upon above failure. One way to diagnose this would be to remove the `RetryPolicy`.

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
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[azure_identity_credentials]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity#credentials
[azure_core_http_netty]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-http-netty
[azure_core_http_okhttp]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-http-okhttp
[azure_core]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core
[logging]: https://learn.microsoft.com/azure/developer/java/sdk/logging-overview
[single_service_packages]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/SINGLE_SERVICE_PACKAGES.md
[authenticate]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/AUTH.md
[sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/SAMPLE.md
[design]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/DESIGN.md
[design_preview]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/DESIGN_PREVIEW.md
[throttling]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/THROTTLING.md
[reactor]: https://projectreactor.io/
[rbac]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/RBAC.md
[unit_test]: https://github.com/Azure/azure-sdk-for-java/wiki/Unit-Testing
