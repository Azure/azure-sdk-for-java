# Testing the Playwright management SDK

The Java SDK validation scenario is a JUnit test in
`src/test/java/com/azure/resourcemanager/playwright/PlaywrightWorkspaceTests.java`.
It runs against the SDK source in this checkout, not a separately downloaded
Playwright SDK package. It uses the module's existing Azure Identity, Azure Core
Test, and JUnit dependencies. No separate Maven project or `exec:java` command is
required.

## Run offline tests

Install a JDK and Maven as described in the repository's
[contributing guide](../../../CONTRIBUTING.md). JDK 21 is recommended for local
development.

If you use unpacked JDK and Maven distributions, first configure the current
PowerShell session:

```powershell
$env:JAVA_HOME = "<path-to-jdk>"
$env:PATH = "$env:JAVA_HOME\bin;<path-to-maven>\bin;$env:PATH"
```

Run the tests from the repository root:

```powershell
$env:AZURE_TEST_MODE = "PLAYBACK"
mvn -f sdk\playwright\azure-resourcemanager-playwright\pom.xml --batch-mode --no-transfer-progress test
```

This runs the generated model and HTTP mock tests. The live workspace test is
marked `@LiveOnly` and is skipped without Azure authentication or resource changes.
It has no recordings and does not run in `RECORD` mode.

## Run the live workspace test

The test creates a uniquely named workspace in an **existing test resource group**.
It enables reporting with an **existing storage account**, creates the workspace
with local authentication disabled, enables local authentication in an update,
and retrieves the workspace. Assertions verify the provisioning state, tags,
reporting, storage URI, local authentication, and dataplane URI.

The test deletes only its generated workspace after execution, including when an
assertion fails, and verifies that a subsequent get returns HTTP 404. It does not
delete the resource group or storage account and does not run browser tests or
upload reports. Azure resources can incur charges. Use nonproduction resources.
If cleanup fails, the Maven run fails and logs the workspace name for manual
cleanup.

### Prerequisites

- Azure CLI installed and authenticated with `az login`, or another credential
  supported by `DefaultAzureCredential`.
- Permission to create, update, read, and delete Playwright workspaces in the
  test resource group (for example, Contributor at that scope).
- The `Microsoft.LoadTestService` resource provider registered in the subscription.
- A region that supports the Playwright service and reporting configuration.
- An existing storage account usable by the Playwright reporting feature. This
  control-plane test does not provision storage, grant data-plane roles, or
  validate report uploads.

### Configuration and command

From the repository root, set the following variables in PowerShell. Replace all
placeholders with your test resource values. No source-code edits are needed.

```powershell
$env:AZURE_TEST_MODE = "LIVE"
$env:AZURE_SUBSCRIPTION_ID = "<subscription-id>"
$env:AZURE_TENANT_ID = "<tenant-id>"
$env:AZURE_RESOURCE_GROUP_NAME = "<existing-test-resource-group>"
$env:AZURE_PLAYWRIGHT_LOCATION = "<supported-region>"
$env:AZURE_PLAYWRIGHT_STORAGE_URI = "https://<storage-account>.blob.core.windows.net"
$env:AZURE_LOG_LEVEL = "info"

mvn -f sdk\playwright\azure-resourcemanager-playwright\pom.xml --batch-mode --no-transfer-progress test "-Dtest=PlaywrightWorkspaceTests"
```

The test uses the Azure public cloud. Missing required configuration fails the
live test rather than silently skipping it. `DefaultAzureCredential` can use
environment credentials before your Azure CLI session; ensure those credentials
target the intended tenant and subscription. Do not put credentials in the
repository or share logs containing secrets.

### Verify the result

A successful live run reports:

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The SDK test log at
`sdk\playwright\azure-resourcemanager-playwright\target\azure-resourcemanager-playwright-test.log`
includes create, update, get, and delete verification messages. Maven also writes
the JUnit XML and text results to
`sdk\playwright\azure-resourcemanager-playwright\target\surefire-reports`.
A result with `Skipped: 1` is **not** proof of a live run.

Afterward, restore offline mode in your shell:

```powershell
$env:AZURE_TEST_MODE = "PLAYBACK"
```

For Maven feed authentication and dependency resolution errors, see the
[contributing guide](../../../CONTRIBUTING.md#azure-artifacts-feed-setup).
For general test conventions, see the
[live testing guide](../../../docs/contributor/live-testing.md).
