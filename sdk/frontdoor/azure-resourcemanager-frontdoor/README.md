# Azure Resource Manager FrontDoor client library for Java

Azure Resource Manager FrontDoor client library for Java.

This package contains Microsoft Azure SDK for FrontDoor Management SDK. FrontDoor Client. Package tag package-2024-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

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

[//]: # ({x-version-update-start;com.azure.resourcemanager:azure-resourcemanager-frontdoor;current})
```xml
<dependency>
    <groupId>com.azure.resourcemanager</groupId>
    <artifactId>azure-resourcemanager-frontdoor</artifactId>
    <version>1.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Include the recommended packages

Azure Management Libraries require a `TokenCredential` implementation for authentication and an `HttpClient` implementation for HTTP client.

[Azure Identity][azure_identity] and [Azure Core Netty HTTP][azure_core_http_netty] packages provide the default implementation.

### Authentication

By default, Microsoft Entra ID token authentication depends on correct configuration of the following environment variables.

- `AZURE_CLIENT_ID` for Azure client ID.
- `AZURE_TENANT_ID` for Azure tenant ID.
- `AZURE_CLIENT_SECRET` or `AZURE_CLIENT_CERTIFICATE_PATH` for client secret or client certificate.

In addition, Azure subscription ID can be configured via `AZURE_SUBSCRIPTION_ID` environment variable.

With above configuration, `azure` client can be authenticated using the following code:

```java
AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
TokenCredential credential = new DefaultAzureCredentialBuilder()
    .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
    .build();
FrontDoorManager manager = FrontDoorManager
    .authenticate(credential, profile);
```

The sample code assumes global Azure. Please change `AzureEnvironment.AZURE` variable if otherwise.

See [Authentication][authenticate] for more options.

## Key concepts

See [API design][design] for general introduction on design and key concepts on Azure Management Libraries.

## Examples

```java
StorageAccount storageAccount = storageManager.storageAccounts()
    .define(saName)
    .withRegion(REGION)
    .withExistingResourceGroup(resourceGroupName)
    .create();

String backendAddress = fdName + ".blob.core.windows.net";
String frontendName = "frontend1";
String loadBalancingName = "loadbalancing1";
String healthProbeName = "healthprobe1";
String routingRuleName = "rule1";
String backendPoolName = "backend1";
subscriptionId = ResourceId.fromString(storageAccount.id()).subscriptionId();
String frontendEndpointsId = getResourceId("frontendEndpoints", frontendName);
String loadBalancingSettingsId = getResourceId("loadBalancingSettings", loadBalancingName);
String healthProbeSettingsId = getResourceId("healthProbeSettings", healthProbeName);
String backendPoolsId = getResourceId("backendPools", backendPoolName);

FrontDoor frontDoor = manager.frontDoors().define(fdName)
    .withRegion("global")
    .withExistingResourceGroup(resourceGroupName)
    .withFrontendEndpoints(Collections.singletonList(
        new FrontendEndpointInner()
            .withName(frontendName)
            .withHostname(fdName + ".azurefd.net")
            .withSessionAffinityEnabledState(SessionAffinityEnabledState.DISABLED)
    ))
    .withBackendPools(Collections.singletonList(
        new BackendPool().withName(backendPoolName).withBackends(Collections.singletonList(
                new Backend()
                    .withAddress(backendAddress)
                    .withEnabledState(BackendEnabledState.ENABLED)
                    .withBackendHostHeader(backendAddress)
                    .withHttpPort(80)
                    .withHttpsPort(443)
                    .withPriority(1)
                    .withWeight(50)
            ))
            .withLoadBalancingSettings(new SubResource().withId(loadBalancingSettingsId))
            .withHealthProbeSettings(new SubResource().withId(healthProbeSettingsId))
    ))
    .withLoadBalancingSettings(Collections.singletonList(
        new LoadBalancingSettingsModel()
            .withName(loadBalancingName)
            .withSampleSize(4)
            .withSuccessfulSamplesRequired(2)
            .withAdditionalLatencyMilliseconds(0)
    ))
    .withHealthProbeSettings(Collections.singletonList(
        new HealthProbeSettingsModel()
            .withName(healthProbeName)
            .withEnabledState(HealthProbeEnabled.ENABLED)
            .withPath("/")
            .withProtocol(FrontDoorProtocol.HTTPS)
            .withHealthProbeMethod(FrontDoorHealthProbeMethod.HEAD)
            .withIntervalInSeconds(30)
    ))
    .withRoutingRules(Collections.singletonList(
        new RoutingRule()
            .withName(routingRuleName)
            .withEnabledState(RoutingRuleEnabledState.ENABLED)
            .withFrontendEndpoints(Collections.singletonList(new SubResource().withId(frontendEndpointsId)))
            .withAcceptedProtocols(Arrays.asList(FrontDoorProtocol.HTTP, FrontDoorProtocol.HTTPS))
            .withPatternsToMatch(Collections.singletonList("/*"))
            .withRouteConfiguration(new ForwardingConfiguration()
                .withForwardingProtocol(FrontDoorForwardingProtocol.HTTPS_ONLY)
                .withBackendPool(new SubResource().withId(backendPoolsId)))
    ))
    .create();
```
[Code snippets and samples](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/frontdoor/azure-resourcemanager-frontdoor/SAMPLE.md)


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
[azure_core_http_netty]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-http-netty
[authenticate]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/AUTH.md
[design]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/DESIGN.md
[cg]: https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Ffrontdoor%2Fazure-resourcemanager-frontdoor%2FREADME.png)
