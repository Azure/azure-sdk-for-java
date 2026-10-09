# Azure Resource Manager Platform Validation client library for Java

Azure Resource Manager Platform Validation client library for Java.

This package contains Microsoft Azure SDK for Platform Validation Management SDK. Microsoft.PlatformValidation Resource Provider management API. Package api-version 2026-08-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

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

[//]: # ({x-version-update-start;com.azure.resourcemanager:azure-resourcemanager-platformvalidation;current})
```xml
<dependency>
    <groupId>com.azure.resourcemanager</groupId>
    <artifactId>azure-resourcemanager-platformvalidation</artifactId>
    <version>1.0.0-beta.1</version>
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
PlatformValidationManager manager = PlatformValidationManager
    .authenticate(credential, profile);
```

The sample code assumes global Azure. Please change the `AzureCloud.AZURE_PUBLIC_CLOUD` variable if otherwise.

See [Authentication][authenticate] for more options.

## Key concepts

See [API design][design] for general introduction on design and key concepts on Azure Management Libraries.

## Examples

[Code snippets and samples](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/platformvalidation/azure-resourcemanager-platformvalidation/SAMPLE.md)


## Troubleshooting

## Next steps

## Contributing

For details on contributing to this repository, see the [contributing guide][cg].

### Live tests

The handwritten `PlatformValidationLiveTests` use the repository's `TestProxyTestBase` and
`@LiveOnly` convention. Normal Maven tests run the generated model/mock tests and skip live
scenarios; no live recordings are produced. See the
[management-plane live-test guide](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/HOW_TO_ADD_LIVE_TESTS.md).

Authenticate with Azure CLI or the supported pipeline credential and set `AZURE_TENANT_ID`,
`AZURE_SUBSCRIPTION_ID`, and `AZURE_RESOURCE_GROUP_NAME` to a dedicated test resource group.
The live-test pipeline sets `PLATFORMVALIDATION_LOCATION=eastus2euap` (Canary).
When this setting is absent, the tests default to `southcentralusstg` (South Central US Stage).
This is a configuration default, not automatic failover after a service or test failure.
The provider must already be registered in that subscription.

```powershell
$env:AZURE_TEST_MODE = "LIVE"
mvn -f sdk\platformvalidation\azure-resourcemanager-platformvalidation\pom.xml -Dtest=PlatformValidationLiveTests test
```

CloudValidation tests cover create, GET, both LIST scopes, PATCH, PUT update, and DELETE.
The operation-status client is checked against the completed PATCH operation.
Catalog checks assert real discovery and GET results; service errors fail rather than skip.
Plan tests require `PLATFORMVALIDATION_SOURCE_VHD_URI`, an approved Linux Gen1 X64 VHD
HTTPS URL, supplied as a secret environment variable. They check plan create, GET, LIST,
metadata PATCH, and DELETE. Boot execution additionally requires
`PLATFORMVALIDATION_RUN_EXECUTION=true` and checks execution results, VTR GET/LIST, and
child absence after execution deletion. Missing optional fixtures are reported as skipped,
not successful execution coverage.
Both plan creation and boot execution require the referenced boot test to be available in
the target environment's TestStore. Wait for its deployment and publishing before running
these scenarios; disabling boot execution alone does not bypass plan-time TestStore resolution.
If the image URL has a SAS expiry, the tests require more than 25 hours of remaining
validity before creating resources (the service requires at least one day).

Each scenario creates unique Java-owned names; no existing CV or plan is accepted as input.
Failures retain resources and log their IDs for investigation. Parent deletion stops if a
child remains. The supplied parent resource group is never deleted by the tests.
For an explicitly approved environment without automatic managed-group provisioning,
`PLATFORMVALIDATION_CREATE_MANAGED_GROUP=true` creates a new, tagged `<CV>-mrg` and
deletes it only when empty after successful child cleanup. It never adopts existing groups
or grants roles; arrange service permissions separately. Do not enable this setting in
environments that automatically provision managed groups.

`sdk/platformvalidation/tests.mgmt.yml` and `test-resources.bicep` provide the standard
live-test pipeline setup. The YAML enables manual managed-group provisioning and keeps
boot execution disabled until the target environment's TestStore is ready.
Before queuing a run, configure the intended subscription and identity through the shared
pipeline's service connection and subscription configuration. Overriding only
`AZURE_SUBSCRIPTION_ID` does not move pipeline-provisioned resources to that subscription.
The identity needs the separately approved permissions to create sibling managed groups;
the Bicep template grants Contributor only on the supplied parent resource group.
Supply the approved image through a secret environment variable, never through checked-in YAML.
After opening the test PR, `/azp run prepare-pipelines` requests
pipeline creation and `/azp run java - platformvalidation - mgmt - tests` requests a run.
Adding these files alone does not confirm pipeline registration, a schedule, or boot-fixture
configuration. Verify those with the SDK team before claiming scheduled execution coverage.

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
