# Azure Resource Manager HDInsight client library for Java

Azure Resource Manager HDInsight client library for Java.

This package contains Microsoft Azure SDK for HDInsight Management SDK. HDInsight Management Client. Package tag package-2024-08-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## We'd love to hear your feedback

We're always working on improving our products and the way we communicate with our users. So we'd love to learn what's working and how we can do better.

If you haven't already, please take a few minutes to [complete this short survey][survey] we have put together.

Thank you in advance for your collaboration. We really appreciate your time!

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure.resourcemanager:azure-resourcemanager-hdinsight;current})
```xml
<dependency>
    <groupId>com.azure.resourcemanager</groupId>
    <artifactId>azure-resourcemanager-hdinsight</artifactId>
    <version>1.1.0-beta.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Include the recommended packages

Azure Management Libraries require a `TokenCredential` implementation for authentication and an `HttpClient` implementation for HTTP client.

[Azure Identity][azure_identity] and [Azure Core Netty HTTP][azure_core_http_netty] packages provide the default implementation.

### Authentication

Microsoft Entra ID token authentication relies on the [credential class][azure_identity_credentials] from [Azure Identity][azure_identity] package.

Azure subscription ID can be configured via `AZURE_SUBSCRIPTION_ID` environment variable.

Assuming the use of the `DefaultAzureCredential` credential class, the client can be authenticated using the following code:

```java
AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
TokenCredential credential = new DefaultAzureCredentialBuilder()
    .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
    .build();
HDInsightManager manager = HDInsightManager
    .authenticate(credential, profile);
```

The sample code assumes global Azure. Please change `AzureEnvironment.AZURE` variable if otherwise.

See [Authentication][authenticate] for more options.

## Key concepts

See [API design][design] for general introduction on design and key concepts on Azure Management Libraries.

## Examples

```java
com.azure.resourcemanager.storage.models.StorageAccount storageAccount =
    storageManager.storageAccounts().define(storageName)
        .withRegion(REGION)
        .withExistingResourceGroup(resourceGroupName)
        .withSku(StorageAccountSkuType.STANDARD_LRS)
        .withMinimumTlsVersion(MinimumTlsVersion.TLS1_0)
        .withAccessFromAzureServices()
        .withAccessFromAllNetworks()
        .create();

BlobContainer blobContainer = storageManager.blobContainers()
    .defineContainer(containerName)
    .withExistingStorageAccount(storageAccount)
    .withPublicAccess(PublicAccess.NONE)
    .create();

cluster = hdInsightManager.clusters()
    .define(clusterName)
    .withExistingResourceGroup(resourceGroupName)
    .withRegion(REGION)
    .withProperties(
        new ClusterCreateProperties()
            .withClusterVersion("4.0.3000.1")
            .withOsType(OSType.LINUX)
            .withClusterDefinition(
                new ClusterDefinition()
                    .withKind("SPARK")
                    .withConfigurations(Collections.unmodifiableMap(clusterDefinition)))
            .withComputeProfile(
                new ComputeProfile()
                    .withRoles(
                        Arrays.asList(
                            new Role().withName("headnode")
                                .withTargetInstanceCount(2)
                                .withHardwareProfile(new HardwareProfile().withVmSize("standard_e8_v3"))
                                .withOsProfile(osProfile)
                                .withEncryptDataDisks(false),
                            new Role().withName("workernode")
                                .withTargetInstanceCount(4)
                                .withHardwareProfile(new HardwareProfile().withVmSize("standard_e8_v3"))
                                .withOsProfile(osProfile)
                                .withEncryptDataDisks(false),
                            new Role().withName("zookeepernode")
                                .withTargetInstanceCount(3)
                                .withHardwareProfile(new HardwareProfile().withVmSize("standard_a2_v2"))
                                .withOsProfile(osProfile)
                                .withEncryptDataDisks(false)
                        )))
            .withTier(Tier.STANDARD)
            .withEncryptionInTransitProperties(
                new EncryptionInTransitProperties()
                    .withIsEncryptionInTransitEnabled(false))
            .withStorageProfile(
                new StorageProfile()
                    .withStorageaccounts(
                        Arrays.asList(
                            new StorageAccount()
                                .withName(storageName + ".blob.core.windows.net")
                                .withResourceId(storageAccount.id())
                                .withContainer(blobContainer.name())
                                .withIsDefault(true)
                                .withKey(storageAccount.getKeys().iterator().next().value()))
                    ))
            .withMinSupportedTlsVersion("1.2")
            .withComputeIsolationProperties(
                new ComputeIsolationProperties()
                    .withEnableComputeIsolation(false))
    )
    .create();
```
[Code snippets and samples](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/hdinsight/azure-resourcemanager-hdinsight/SAMPLE.md)


## Troubleshooting

## Next steps

## Contributing

For details on contributing to this repository, see the [contributing guide][cg].

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit <https://cla.microsoft.com>.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repositories using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact <opencode@microsoft.com> with any additional questions or comments.

<!-- LINKS -->
[survey]: https://microsoft.qualtrics.com/jfe/form/SV_ehN0lIk2FKEBkwd?Q_CHL=DOCS
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[azure_identity_credentials]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity#credentials
[azure_core_http_netty]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-http-netty
[authenticate]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/AUTH.md
[design]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/DESIGN.md
[cg]: https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fhdinsight%2Fazure-resourcemanager-hdinsight%2FREADME.png)
