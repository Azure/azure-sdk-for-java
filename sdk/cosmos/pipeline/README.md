# Cosmos live-test fixed accounts

This directory holds the **single-secret JSON configuration** used to point the Cosmos
Java live tests at fixed, self-owned Cosmos DB accounts (no central EngSys
provisioner). It is the Track A mechanism from the retargeting plan.

## Files

| File | Purpose |
| --- | --- |
| `live-test-accounts.schema.json` | JSON schema (draft-07) for the single secret. |
| `live-test-accounts.sample.json` | Example with all logical accounts (placeholder values). |
| `resolve-cosmos-test-account.sh` | Pre-step parser: reads the JSON secret + a selector, exports `ACCOUNT_HOST`/`ACCOUNT_KEY`. |
| `resolve-cosmos-test-account.tests.sh` | Local tests for the parser (no ADO required). |
| `resolve-test-account-steps.yml` | Reusable `PreTestRunSteps` template that runs the parser for a selector. |

## How it works

1. A **single ADO variable-group secret** (`sub-config-cosmos-azure-cloud-test-resources`)
   holds a JSON document (see schema) mapping a **logical account name** to its
   `{ endpoint, key, secondaryKey, ... }`.
2. Each pipeline stage passes a **selector** (the logical name it needs).
3. A **pre-step** runs `resolve-cosmos-test-account.sh`, which validates the JSON,
   looks up the selected account, and exports `ACCOUNT_HOST`, `ACCOUNT_KEY` (and
   `SECONDARY_ACCOUNT_KEY` when present) for the tests to read via `TestConfigurations`.
   It does NOT set `ACCOUNT_CONSISTENCY`/`PREFERRED_LOCATIONS` — those are matrix-controlled
   per leg.
4. The account provisioning script (in [`account-provisioning/`](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/cosmos/pipeline/account-provisioning))
   regenerates the accounts + JSON on every ephemeral-tenant rotation
   (`New-CosmosLiveTestAccounts.ps1` creates the accounts and outputs the JSON). The
   secret is then updated manually with that JSON, so pipelines pick up refreshed
   endpoints/keys automatically.

All Cosmos live-test matrix legs run on **linux** agents, so the parser is bash + jq.

## Selectors (from the test matrices)

`single-session`, `single-session-pmerge`, `single-strong`, `single-session-split`,
`single-session-cfp-split`, `single-strong-split`, `single-strong-cfp-split`,
`multiregion-strong`, `multimaster-multiregion-session`, `multimaster-session-control`,
`multimaster-session-http2`, `multimaster-session-circuit`,
`multimaster-multiregion-session-fi`, `multimaster-multiregion-session-split`,
`multimaster-session-cfp-split`, `multiregion-tc-session`, `gsi-single-session`,
`kafka-session`.

The `*-split` and `*-cfp-split` accounts separately isolate the two
partition-split-forcing profiles. This prevents `-Psplit` and `-Pcfp-split` jobs from
queueing backend splits on the same account.
The `*-fi` account isolates fault-injection profiles from normal traffic so injected
availability failures are not compounded by unrelated shared-account load.

## Wiring a stage (tests.yml / kafka.yml)

Use the reusable steps template in `PreTestRunSteps` and set
`DisableAzureResourceCreation: true` so no per-run provisioning/service-connection deploy
happens:

```yaml
DisableAzureResourceCreation: true
PreTestRunSteps:
  - template: /sdk/cosmos/pipeline/resolve-test-account-steps.yml
    parameters:
      AccountSelector: multimaster-multiregion-session   # literal, or $(AccountSelector) from the matrix
```

`ACCOUNT_HOST`/`ACCOUNT_KEY` set by the resolve step are consumed by `TestConfigurations`
via environment variables; no test-code change is required. The key is emitted with the
azure-sdk double-set convention (`_ACCOUNT_KEY` secret to mask it in logs + a plain
`ACCOUNT_KEY` so it reaches the Maven task's env).

### Per-leg selection (multi-account stages)

The main `Cosmos_live_test` stage spans many consistency/topology configs, so each leg of
`live-platform-matrix.json` carries an `AccountSelector` field and the stage passes
`AccountSelector: $(AccountSelector)`. Single-account stages pass a literal selector.

### Currently wired

- `Cosmos_live_test` (main) — per-leg selector.
- `Cosmos_Live_Test_Http2` — `multimaster-session-http2`.

NOT yet wired to the JSON selector (need follow-up): the thin-client stages and GSI (their accounts need
thin-client / GSI-preview enablement that plain account creation doesn't do), Kafka
(entangled with AAD — belongs in the separate AAD pipeline), and Spring (uses its own
test-resources with `AZURE_SPRING_TENANT_ID`).

### Fixed thin-client and GSI accounts

These stages do not use the JSON selector. They pass fixed credentials to Maven as
`ACCOUNT_HOST`/`ACCOUNT_KEY` and to the post-job database cleanup:

| Stage (`Cosmos_Live_Test_` prefix) | Endpoint variable | Key variable |
| --- | --- | --- |
| `Http2NetworkFault`, `ThinClient`, `ThinClient_EndpointProbe` | `thinclient-test-endpoint` | `thinclient-test-key` |
| `ThinClient_MultiRegion` | `thin-client-canary-multi-region-session-endpoint` | `thin-client-canary-multi-region-session-key` |
| `ThinClient_MultiMaster` | `thin-client-canary-multi-writer-session-endpoint` | `thin-client-canary-multi-writer-session-key` |
| `GSI` | `gsi-pipeline-uri` | `gsi-pipeline-key` |

Their selected profiles use these accounts for setup and tests; they do not require
ARM-generated `NEW_ACCOUNT_*` or secondary-key outputs. This includes the query and
consistency-override legs of `ThinClient` and both transport legs of `Http2NetworkFault`.

All six stages set `DisableAzureResourceCreation: true` to skip per-run ARM resource
configuration, deployment, and removal. Maven feed authentication and the federated
test runner remain enabled. The legacy `ArmTemplateParameters` in their matrices are
unused while provisioning is disabled; consistency and other test inputs still apply.
The endpoint-probe stage still leaves `COSMOS.THINCLIENT_ENABLED` unset, and the
network-fault stage retains its iptables setup and per-profile transport selection.

### Secret upkeep

Every account in the secret carries a `secondaryKey` — `AzureKeyCredentialTest` (in the
`-Pfast` legs) calls `TestConfigurations.SECONDARY_MASTER_KEY`, so a missing secondary key
would fall back to the emulator key and fail against a live account.

## Secret variable

The JSON lives in the ADO variable-group secret
`sub-config-cosmos-azure-cloud-test-resources` (Cosmos "user administered" variable
group). The resolve steps template reads it by default. Refresh it manually whenever the
accounts are re-provisioned (e.g. after an ephemeral-tenant rotation) by pasting the
regenerated JSON from `New-CosmosLiveTestAccounts.ps1`.

## Local test

```bash
bash sdk/cosmos/pipeline/resolve-cosmos-test-account.tests.sh
```

Check all six stages' provisioning opt-outs, fixed test/cleanup credentials,
profile selection, transport settings, and authentication configuration offline (Python 3 with PyYAML,
also used by `eng/automation/requirement.txt`):

```bash
python3 sdk/cosmos/pipeline/test_fixed_account_pipeline.py
```
