# Edge Actions management tests

The handwritten integration tests exercise **API version `2026-10-01` only**, as generated in
[PR #50750](https://github.com/Azure/azure-sdk-for-java/pull/50750). They do not test earlier API versions.

| Test class | Coverage |
| --- | --- |
| `EdgeActionsIntegrationTests` | Create, get, list, tag replacement/preservation/clearing, rejection of SKU changes through PATCH, and delete |
| `EdgeActionVersionsIntegrationTests` | File and ZIP deployment, downloaded source verification, automatic first default, version updates, default switching, listing, and deletion |
| `EdgeActionExecutionFiltersIntegrationTests` | Filter creation, retrieval, listing, version/header updates, omitted-field preservation, deletion, and a separate undeployed-version filter probe |
| `EdgeActionsTestCodeTests` | Offline fixture encoding, deterministic ZIP creation, and rejection of missing downloaded code |
| `EdgeActionsSanitizerTests` | Offline sanitizer rules: polling query boundaries, embedded JSON URLs, resource identity, token fields, and trace/cookie headers |
| `EdgeActionsSanitizerProxyTests` | Local-only proxy record/replay against a synthetic HTTP server; verifies saved redaction and matching changed or already-sanitized polling values |

The integration tests use the Java management test-proxy infrastructure. **PLAYBACK is the default and missing
recordings fail the tests.** There are no live-only annotations, skip conditions, or fallback calls to Azure.
Initial recordings are deliberately not included: run RECORD against your authorized test environment first.
Switching to PLAYBACK afterwards does not require a code change.

The seven service test methods include `createsExecutionFilterWithoutCodeDeployment`, which creates an
Edge Action and a FILE version without deploying or uploading code, then creates an execution filter referencing
that version. It checks the returned and retrieved version linkage and synthetic header values, deletes the filter,
and verifies it is absent. Shared cleanup removes the test-owned version and parent. This is an unverified
service probe: it does not assert successful code validation, mock service acceptance, or bypass the existing
deployed-version test. A service rejection remains a test failure.

After configuring the intended test mode and its prerequisites below, run only this probe from the module directory:

```powershell
mvn test '-Dtest=EdgeActionExecutionFiltersIntegrationTests#createsExecutionFilterWithoutCodeDeployment'
```

Without a recording, default PLAYBACK fails rather than calling Azure. Local compilation and offline tests do
not establish whether the service accepts an execution filter referencing a version with no code.

## Prerequisites

- Install the JDK and Maven required by the repository's
  [build guide](https://github.com/Azure/azure-sdk-for-java/tree/main/docs/contributor/building.md).
  Set `JAVA_HOME` and put `mvn` on `PATH`.
- For RECORD or LIVE, install Azure CLI and sign in to the intended Azure public-cloud tenant/subscription.
  Use a dedicated existing resource group and an identity allowed to manage `Microsoft.Cdn/edgeActions` and their
  child resources. Ensure the provider is registered and the subscription is authorized for the service.
- The Java test framework downloads and starts its local test proxy automatically. Initial downloads require
  network access. Do not set `CI` or `TF_BUILD` for these local commands; CI uses externally managed proxy startup.
- Reserve localhost port 5000 for the proxy. If another application owns it, do not stop that application blindly.
  On a host where only IPv4 port 5000 is occupied and IPv6 loopback is free, the following process-local settings
  allow the proxy and Java to use IPv6 instead:

  ```powershell
  $env:ASPNETCORE_URLS = "http://[::1]:5000"
  $env:JAVA_TOOL_OPTIONS = "-Djava.net.preferIPv6Addresses=true"
  ```

  Preserve any existing `JAVA_TOOL_OPTIONS` your environment requires. Otherwise use an environment with port
  5000 available. Proxy startup errors are infrastructure failures, not missing-recording failures.
- Publishing/restoring external recording assets uses the
  [test-proxy CLI](https://github.com/Azure/azure-sdk-tools/tree/main/tools/test-proxy/Azure.Sdk.Tools.TestProxy#installation).
  Initial publishing also requires PowerShell 7, configured Git identity, and write permission to
  `Azure/azure-sdk-assets`.

Run the following commands from this repository's root in PowerShell. If the test-only
`azure-resourcemanager-test` dependency is not available from your Maven feed, build it locally first:

```powershell
mvn --% -B -f sdk\resourcemanager\azure-resourcemanager-test\pom.xml install -DskipTests -Dgpg.skip=true -Drevapi.skip=true
```

This builds the shared test helper, not Edge Actions recordings. Resolve feed authentication through the
repository's [contribution instructions](https://github.com/Azure/azure-sdk-for-java/tree/main/CONTRIBUTING.md)
if another dependency is inaccessible.

## Record against Azure

RECORD creates real resources and can incur charges. The tests require an **existing** resource group:
they never create or delete the group itself. Each test generates a unique Edge Action name, checks that
it is absent before creation, and cleans up its filters, versions (current default last), and parent.
Do not use a resource group in which other processes modify these test-owned resources.
The commands below use `javasdk-edgeaction-tests-rg`; ensure that group exists in the selected subscription.

```powershell
az login --tenant "<tenant-id>"
az account set --subscription "<subscription-id>"

$env:AZURE_TENANT_ID = "<tenant-id>"
$env:AZURE_SUBSCRIPTION_ID = "<subscription-id>"
$env:AZURE_RESOURCE_GROUP_NAME = "javasdk-edgeaction-tests-rg"
$env:AZURE_TEST_MODE = "RECORD"
$env:AZURE_TEST_LOG_LEVEL = "BASIC"

mvn --% -B -f sdk\edgeactions\azure-resourcemanager-edgeactions\pom.xml -Dtest=EdgeActionsIntegrationTests,EdgeActionVersionsIntegrationTests,EdgeActionExecutionFiltersIntegrationTests test
```

The shared management test helper selects the test credential; local Azure CLI sign-in is supported.
Avoid ambiguous credentials from an unrelated tenant in the shell.

Before `assets.json` exists, the proxy writes local recordings under
`sdk\edgeactions\azure-resourcemanager-edgeactions\src\test\resources\session-records`.
The JSON files are git-ignored until you review and publish them. Once assets are configured, the proxy stores
recordings in the external assets checkout.

On an interrupted or failed run, inspect the exact generated Edge Action names in the test output/recordings and
the supplied resource group. Cleanup is best effort when Azure itself fails or the process is killed; failures
are surfaced rather than silently ignored. Manually remove only confirmed test-owned resources, not the shared group.

## Replay locally

After a successful recording, run the same tests in PLAYBACK:

```powershell
$env:AZURE_TEST_MODE = "PLAYBACK"
$env:AZURE_TEST_LOG_LEVEL = "BASIC"

mvn --% -B -f sdk\edgeactions\azure-resourcemanager-edgeactions\pom.xml -Dtest=EdgeActionsIntegrationTests,EdgeActionVersionsIntegrationTests,EdgeActionExecutionFiltersIntegrationTests test
```

Playback uses recorded resource names/configuration, a dummy credential, and the local proxy; Azure CLI login,
Azure resource configuration, and Azure connectivity are not required. Verify this in a separate shell without
Azure credentials. A first-time Maven/proxy download or assets restore can still require network access.

Check `target\surefire-reports` under the package: all seven integration test methods must execute with zero failures,
errors, or skips once recordings are present. Before the initial capture, missing-recording errors are expected;
they must not be treated as successful validation. Configuration/authentication errors are not the expected playback failure.

Run all package tests, including generated mock tests and the offline fixture tests, with:

```powershell
mvn --% -B -f sdk\edgeactions\azure-resourcemanager-edgeactions\pom.xml test
```

To run only mock/fixture and local proxy sanitizer tests while preparing the first Azure recordings:

```powershell
mvn --% -B -f sdk\edgeactions\azure-resourcemanager-edgeactions\pom.xml -Dtest=*Tests,!*IntegrationTests test
```

The sanitizer proxy test uses the same local proxy prerequisites, requires no Azure configuration, and writes only
temporary synthetic recordings. It does not create or replace the seven service integration recordings.

## Review and publish the first recordings

Review the captured JSON before sharing it. The tests extend `ResourceManagerTestProxyTestBase`, as do CDN and
other Java management tests, and register package-specific `TestProxySanitizer` rules through `addSanitizers`.
The shared management sanitizers remain enabled, except for the shared base's standard exclusions that preserve
resource names and LRO URLs.

The additional rules follow the coverage in
[Azure/azure-sdk-for-python#49467](https://github.com/Azure/azure-sdk-for-python/pull/49467), using Java's
URI, body, and header sanitizers rather than Python's general-regex/environment-string sanitizers:

| Surface | Sanitization |
| --- | --- |
| Resource IDs in bodies, including version IDs and embedded URLs | Replace subscription IDs without erasing resource paths |
| Polling request URLs, embedded body URLs, and `Location`/`Azure-AsyncOperation`/`Operation-Location` headers | Redact opaque `t`, `c`, `s`, and `h` values; preserve paths, API versions, and other query parameters |
| `systemData.createdBy` and `systemData.lastModifiedBy` | Redact identity metadata |
| `tenantId`, `clientId`, and `principalId` body fields | Replace with a zero UUID |
| `access_token`, `refresh_token`, `id_token`, and `client_secret` body fields | Redact token/secret values |
| Correlation/routing/operation/original-request IDs, `X-MSEdge-Ref`, and cookie headers | Redact header values |

Unlike the Python configuration, these rules do not globally replace every occurrence of configured environment
values in arbitrary fields. Do not assume that automatic sanitizers cover every
future service response. Check authorization headers, tokens, subscription/tenant identifiers, identity metadata, and
polling URLs. Use neutral resource-group names. Test JavaScript, synthetic filter headers, and tags intentionally remain
visible so playback can validate requests and responses.

Do not edit generated clients or overwrite recordings to hide a product/test failure. Diagnose the failure first.
Never fabricate service responses.

After sanitizer inspection and successful local playback, onboard these local recordings:

```powershell
Push-Location sdk\edgeactions\azure-resourcemanager-edgeactions
try {
    & ..\..\..\eng\common\testproxy\onboarding\generate-assets-json.ps1 -InitialPush
} finally {
    Pop-Location
}
```

The script creates `assets.json`, migrates local recordings into `Azure/azure-sdk-assets`, pushes the initial tag, and
updates the manifest. Commit **`assets.json`**, not the raw local recordings, credentials, or `.assets` checkout.
Do not run initial onboarding again after the manifest exists.

On a clean checkout, restore the published recording set and verify playback:

```powershell
test-proxy restore -a sdk\edgeactions\azure-resourcemanager-edgeactions\assets.json
$env:AZURE_TEST_MODE = "PLAYBACK"
mvn --% -B -f sdk\edgeactions\azure-resourcemanager-edgeactions\pom.xml test
```

For subsequent intentional test changes, run RECORD, inspect the updated recordings, verify PLAYBACK, and publish:

```powershell
test-proxy push -a sdk\edgeactions\azure-resourcemanager-edgeactions\assets.json
```

Commit the updated `assets.json` tag with the test change. See the
[Java proxy migration guide](https://github.com/Azure/azure-sdk-for-java/tree/main/docs/test-proxy-migration.md)
and [assets onboarding guide](https://github.com/Azure/azure-sdk-for-java/tree/main/eng/common/testproxy/onboarding/README.md)
for the repository-wide process.
