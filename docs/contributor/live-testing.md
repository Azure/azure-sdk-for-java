# Live Testing

> **Source**: Consolidated from [CONTRIBUTING.md](../../CONTRIBUTING.md) and `eng/common/TestResources/` (last reviewed April 2026).  
> **See also**: [Unit Testing](unit-testing.md) · [Test Resources scripts](../../eng/common/TestResources/README.md)

---

## Overview

Live tests connect to real Azure services and require:
1. A deployed set of Azure test resources (storage accounts, key vaults, etc.)
2. Environment variables that point the tests at those resources

The script `New-TestResources.ps1` automates resource deployment.

---

## Step 1 – Deploy Test Resources

Find the ARM template for the service you want to test:

```
sdk/<service>/test-resources.json
```

For example: `sdk/keyvault/test-resources.json`

Deploy using PowerShell:

```powershell
eng/common/TestResources/New-TestResources.ps1 -ServiceDirectory keyvault
```

> **Full reference**: [`New-TestResources.ps1` documentation](../../eng/common/TestResources/New-TestResources.ps1.md)  
> See **Example 1** in that doc for the recommended invocation that creates a service principal and sets environment variables.

The script prints the environment variable exports to set before running tests.

---

## Step 2 – Set Environment Variables

The output of `New-TestResources.ps1` lists the variables to export.
Set them in your shell before running tests:

```bash
# Example (bash)
export AZURE_KEYVAULT_URL=https://...
export AZURE_CLIENT_ID=...
export AZURE_CLIENT_SECRET=...
export AZURE_TENANT_ID=...
```

---

## Step 3 – Run Live Tests

```bash
mvn -f sdk/<service>/pom.xml \
  -Dmaven.wagon.http.pool=false \
  --batch-mode --fail-at-end \
  --settings eng/settings.xml \
  test
```

Example for Key Vault:

```bash
mvn -f sdk/keyvault/pom.xml \
  -Dmaven.wagon.http.pool=false \
  --batch-mode --fail-at-end \
  --settings eng/settings.xml \
  test
```

Some services have additional steps. Check the service's own `CONTRIBUTING.md`:
`sdk/<service>/CONTRIBUTING.md`

---

## Step 4 – Tear Down Resources

When done, remove the deployed resources to avoid incurring costs:

```powershell
eng/common/TestResources/Remove-TestResources.ps1 -ServiceDirectory keyvault
```

> **Full reference**: [`Remove-TestResources.ps1` documentation](../../eng/common/TestResources/Remove-TestResources.ps1.md)

---

## Test Recording / Playback

Most live tests support **record/playback** mode via the Test Proxy so you can run
them offline without deployed Azure resources.

| Mode | Description |
|------|-------------|
| `RECORD` | Makes real service calls and records them |
| `PLAYBACK` | Replays recorded responses; no network calls |
| `LIVE` | Always hits the real service; no recording |

See the [Test Proxy onboarding guide](../../eng/common/testproxy/onboarding/README.md) and
the [Test Proxy Migration wiki page](https://github.com/Azure/azure-sdk-for-java/wiki/Test-Proxy-Migration) for setup instructions.

---

## Parameterized Live Tests

For guidance on writing parameterized tests that can run both as live and playback tests,
see [Parameterized test for SDK live test](https://github.com/Azure/azure-sdk-for-java/wiki/Parameterized-test-for-SDK-live-test) (wiki).
