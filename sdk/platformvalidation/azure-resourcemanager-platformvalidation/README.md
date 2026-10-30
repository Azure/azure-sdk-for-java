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

### Recorded scenario tests

The handwritten `PlatformValidationLiveTests` use `ResourceManagerTestProxyTestBase`
for RECORD, PLAYBACK, and LIVE modes, following the
[management-plane test framework guide](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/resourcemanager/azure-resourcemanager-test).
Despite the historical class name, these scenarios are no longer marked `@LiveOnly`.
Normal Maven tests use PLAYBACK and do not require Azure credentials or a live subscription.
Recordings must be captured and published before playback can pass; missing recordings
are failures, not successful coverage.

For RECORD or LIVE, authenticate with Azure CLI and set `AZURE_TENANT_ID`,
`AZURE_SUBSCRIPTION_ID`, and `AZURE_RESOURCE_GROUP_NAME` to a dedicated test resource group.
Set `PLATFORMVALIDATION_LOCATION=eastus2euap` for Canary.
When this setting is absent, tests default to `southcentralusstg` (South Central US Stage).
This is a configuration default, not automatic failover after a service or test failure.
Use a subscription where the provider is registered and the requested region is enabled.
For regional Canary ARM routing, also set
`PLATFORMVALIDATION_ARM_ENDPOINT=https://eastus2euap.management.azure.com`.
This overrides only the resource-manager endpoint, preserving public-cloud authentication.
Leave `PLATFORMVALIDATION_CREATE_MANAGED_GROUP` unset for the current Canary deployment:
the service provisions and deletes the managed group; no manual group or role assignment is needed.
Resource names and non-secret environment settings are recorded through the framework's
resource namer, so playback does not depend on local settings. Both the service client and
the Resource Manager client share the proxy-aware HTTP pipeline.

```powershell
$env:AZURE_TEST_MODE = "RECORD"
$env:AZURE_TEST_LOG_LEVEL = "BASIC"
mvn -f sdk\platformvalidation\azure-resourcemanager-platformvalidation\pom.xml "-Djunit.jupiter.execution.parallel.enabled=false" "-Dtest=PlatformValidationLiveTests" test
```

After recording, clear credentials and service-specific environment settings and verify playback:

```powershell
$env:AZURE_TEST_MODE = "PLAYBACK"
mvn -f sdk\platformvalidation\azure-resourcemanager-platformvalidation\pom.xml "-Djunit.jupiter.execution.parallel.enabled=false" test
```

Inspect recordings for secrets before publishing. Custom body sanitizers redact signed URLs,
including the VHD SAS embedded inside `planConfigurationJson`, and subscription IDs in
service-specific fields and operation headers. Playback uses a non-secret image placeholder,
never a real SAS or its wall-clock expiry.
Publish recordings to `Azure/azure-sdk-assets` and commit the updated `assets.json` pointer,
following [Update test recordings](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/README.md#update-test-recordings).
From the repository root, with the test-proxy CLI installed:

```powershell
test-proxy push -a sdk\platformvalidation\azure-resourcemanager-platformvalidation\assets.json
```

Refresh recordings for each new API-version SDK PR, then verify playback before submitting.
Playback validates the SDK against recorded responses, not the current health of the service.

CloudValidation tests cover create, GET, both LIST scopes, PATCH, PUT update, and DELETE.
The operation-status client is checked against the completed PATCH operation.
Catalog checks assert real discovery and GET results; service errors fail rather than skip.
Plan tests require `PLATFORMVALIDATION_SOURCE_VHD_URI`, an approved Linux Gen1 X64 VHD
HTTPS URL, supplied as a secret environment variable. They check plan create, GET, LIST,
metadata PATCH, and DELETE. Boot execution additionally requires
`PLATFORMVALIDATION_RUN_EXECUTION=true` and checks execution results, VTR GET/LIST, and
child absence after execution deletion. In RECORD/LIVE, plan-only tests without an image fixture are
reported as skipped, not successful execution coverage. Explicitly enabling boot execution
without a valid image fixture fails rather than silently skipping the workload.
In PLAYBACK, both image-dependent scenarios use their recordings without fixture credentials
or boot opt-in. Do not publish an incomplete recording set as full scenario coverage.
Both plan creation and boot execution require the referenced boot test to be available in
the target environment's TestStore. Wait for its deployment and publishing before running
these scenarios; disabling boot execution alone does not bypass plan-time TestStore resolution.
If the image URL has a SAS expiry, the tests require more than 25 hours of remaining
validity before creating resources (the service requires at least one day).

Each scenario creates unique Java-owned names; no existing CV or plan is accepted as input.
Failures retain resources and log their IDs for investigation. Parent deletion stops if a
child remains. The supplied parent resource group is never deleted by the tests.
After child deletion, CloudValidation cleanup retries only HTTP 409
`CannotDeleteResource`, at most 30 attempts ten seconds apart, verifying the plan
list is empty before every attempt. Other failures remain failures.
For an explicitly approved environment without automatic managed-group provisioning,
`PLATFORMVALIDATION_CREATE_MANAGED_GROUP=true` creates a new, tagged `<CV>-mrg` and
deletes it only when empty after successful child cleanup. It never adopts existing groups
or grants roles; arrange service permissions separately. Do not enable this setting in
environments that automatically provision managed groups.

The existing `sdk/platformvalidation/tests.mgmt.yml` and `test-resources.bicep` are an
optional live-test pipeline, not the recording workflow. Standard PR CI runs playback
using published recordings; it does not need the SDK test subscription to host the service.
The optional live pipeline uses regional Canary ARM routing, service-managed resource
groups, and serial test execution. Boot execution remains an explicit opt-in:
set the `RunExecution` pipeline parameter to `true` and configure the secret pipeline
variable `PlatformValidationSourceVhdUri` with the approved image URL.
An unset or expired fixture fails an opted-in run. The default pipeline run exercises
discovery and CloudValidation lifecycle; image-dependent scenarios are skipped.
Before queuing a run, configure the intended subscription and identity through the shared
pipeline's service connection and subscription configuration. Overriding only
`AZURE_SUBSCRIPTION_ID` does not move pipeline-provisioned resources to that subscription.
The Bicep template grants Contributor only on the supplied parent resource group.
The live-test client uses the SDK's `ProviderRegistrationPolicy` to register the resource
provider when ARM returns `MissingSubscriptionRegistration`. This requires the test
identity to have subscription-level provider registration permission; otherwise, arrange
registration separately. The policy does not handle `SubscriptionNotRegistered` or
`LocationNotAvailableForResourceType`; verify registration and regional availability
separately if those errors persist.
The service owns managed-group provisioning; the test identity does not create sibling
managed groups or assign service roles.
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
