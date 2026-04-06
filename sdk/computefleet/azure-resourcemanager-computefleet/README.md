# Azure Resource Manager Compute Fleet client library for Java

Azure Resource Manager Compute Fleet client library for Java.

This package contains Microsoft Azure SDK for Compute Fleet Management SDK.  Package api-version 2025-07-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

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

[//]: # ({x-version-update-start;com.azure.resourcemanager:azure-resourcemanager-computefleet;current})
```xml
<dependency>
    <groupId>com.azure.resourcemanager</groupId>
    <artifactId>azure-resourcemanager-computefleet</artifactId>
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
AzureProfile profile = new AzureProfile(AzureCloud.AZURE_PUBLIC_CLOUD);
TokenCredential credential = new DefaultAzureCredentialBuilder()
    .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
    .build();
ComputeFleetManager manager = ComputeFleetManager
    .authenticate(credential, profile);
```

The sample code assumes global Azure. Please change the `AzureCloud.AZURE_PUBLIC_CLOUD` variable if otherwise.

See [Authentication][authenticate] for more options.

## Key concepts

See [API design][design] for general introduction on design and key concepts on Azure Management Libraries.

## Examples

```java
Network network = networkManager.networks()
    .define(vnetName)
    .withRegion(REGION)
    .withExistingResourceGroup(resourceGroupName)
    .withAddressSpace("172.16.0.0/16")
    .defineSubnet("default")
    .withAddressPrefix("172.16.0.0/24")
    .attach()
    .create();

LoadBalancer loadBalancer = networkManager.loadBalancers()
    .define(loadBalancerName)
    .withRegion(REGION)
    .withExistingResourceGroup(resourceGroupName)
    .defineLoadBalancingRule(loadBalancerName + "-lbrule")
    .withProtocol(TransportProtocol.TCP)
    .fromExistingSubnet(network, "default")
    .fromFrontendPort(80)
    .toBackend(loadBalancerName + "-backend")
    .toBackendPort(80)
    .attach()
    .withSku(LoadBalancerSkuType.STANDARD)
    .create();

fleet = computeFleetManager.fleets()
    .define(fleetName)
    .withRegion(REGION)
    .withExistingResourceGroup(resourceGroupName)
    .withProperties(new FleetProperties()
        .withSpotPriorityProfile(new SpotPriorityProfile().withMaintain(false)
            .withCapacity(1)
            .withEvictionPolicy(EvictionPolicy.DELETE)
            .withAllocationStrategy(SpotAllocationStrategy.LOWEST_PRICE))
        .withVmSizesProfile(Arrays.asList(new VmSizeProfile().withName("Standard_D4s_v3")))
        .withComputeProfile(new ComputeProfile()
            .withBaseVirtualMachineProfile(new BaseVirtualMachineProfile()
                .withStorageProfile(new VirtualMachineScaleSetStorageProfile()
                    .withImageReference(new ImageReference().withPublisher("canonical")
                        .withOffer("ubuntu-24_04-lts")
                        .withSku("server")
                        .withVersion("latest"))
                    .withOsDisk(new VirtualMachineScaleSetOSDisk()
                        .withManagedDisk(new VirtualMachineScaleSetManagedDiskParameters()
                            .withStorageAccountType(StorageAccountTypes.PREMIUM_LRS))
                        .withOsType(OperatingSystemTypes.LINUX)
                        .withDiskSizeGB(30)
                        .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE)
                        .withDeleteOption(DiskDeleteOptionTypes.DELETE)
                        .withCaching(CachingTypes.READ_WRITE))
                    .withDiskControllerType(DiskControllerTypes.SCSI))
                .withOsProfile(new VirtualMachineScaleSetOSProfile().withComputerNamePrefix(randomPadding())
                    .withAdminUsername(adminUser)
                    .withAdminPassword(adminPwd)
                    .withLinuxConfiguration(
                        new LinuxConfiguration().withDisablePasswordAuthentication(false)))
                .withNetworkProfile(
                    new VirtualMachineScaleSetNetworkProfile()
                        .withNetworkInterfaceConfigurations(
                            Arrays.asList(new VirtualMachineScaleSetNetworkConfiguration().withName(vmName)
                                .withProperties(new VirtualMachineScaleSetNetworkConfigurationProperties()
                                    .withPrimary(true)
                                    .withEnableAcceleratedNetworking(false)
                                    .withDeleteOption(DeleteOptions.DELETE)
                                    .withIpConfigurations(Arrays
                                        .asList(new VirtualMachineScaleSetIPConfiguration().withName(vmName)
                                            .withProperties(
                                                new VirtualMachineScaleSetIPConfigurationProperties()
                                                    .withPrimary(true)
                                                    .withSubnet(new ApiEntityReference()
                                                        .withId(network.subnets().get("default").id()))
                                                    .withLoadBalancerBackendAddressPools(
                                                        loadBalancer.loadBalancingRules()
                                                            .get(loadBalancerName + "-lbrule")
                                                            .innerModel()
                                                            .backendAddressPools())))))))
                        .withNetworkApiVersion(NetworkApiVersion.fromString("2024-03-01"))))
            .withComputeApiVersion("2024-03-01")
            .withPlatformFaultDomainCount(1))
        .withRegularPriorityProfile(new RegularPriorityProfile()
            .withAllocationStrategy(RegularPriorityAllocationStrategy.LOWEST_PRICE)
            .withMinCapacity(1)
            .withCapacity(2)))
    .create();
```
[Code snippets and samples](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/computefleet/azure-resourcemanager-computefleet/SAMPLE.md)


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
