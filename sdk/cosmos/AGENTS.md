# AGENTS.md — Azure Cosmos DB SDK for Java

This file provides Cosmos-specific guidance for AI agents working in the `sdk/cosmos` subtree.
For general repo-wide guidance, see the [root AGENTS.md](../../AGENTS.md).

## Test resource hygiene (required for any test that touches an account)

Every live test stage in `tests.yml` now runs against a **long lived shared account** that is never
torn down - either a fixed self-owned account in RG `sdk-ci` (the main and Http2 stages) or a thin
client / GSI account. Several matrix legs and concurrent pipeline runs share them, so a database a
test forgets to delete stays on that account forever.

When adding or changing tests in `azure-cosmos-tests`:

- **Create databases with `TestSuiteBase.createTestDatabase(client, "label")`**, never with
  `UUID.randomUUID()`, a fixed literal, or a raw `client.createDatabase(...)` call. The helper embeds
  the CI run id in the id so cleanup can attribute the database to this run without deleting another
  run's in-flight resources. `TestResourceHygieneTest` fails the build on direct database creation.
- **Still delete what you create** in an `@AfterClass(alwaysRun = true)` using `safeDeleteDatabase`.
  `CosmosTestResourceJanitor` cleans up leftovers but **fails the run** when it has to.
- Containers inside a database you delete need no separate cleanup.

### How cleanup works

| Layer | Where | Catches |
| --- | --- | --- |
| `CosmosTestResourceRegistry` + `CosmosTestResourceJanitor` | in the test JVM, at the end of the run | normal completion; deletes leftovers and **fails the run** naming the offending test |
| JVM shutdown hook | in the test JVM | crashes and hard aborts |
| `cleanup-test-resources.yml` post step | pipeline, `condition: always()` | failed jobs, and jobs whose JVM died. Runs on cancellation too, but only within `cancelTimeoutInMinutes` (5 by default), so treat it as best effort there |

Every layer scopes deletion by the run id (`CosmosTestRunId`), derived from `System.JobId` (a GUID that
is unique per job), with the build id carried along so a stray database can be traced back to a build.
A run only ever deletes its own resources, so concurrent legs on a shared account cannot interfere.

When a leak fails a run, TestNG has already written its reports by the time the janitor runs, so the
**published test results show `Tests run: 0` rather than the leak** — which reads like an infrastructure
fault. The leak is reported in the build log and as an ADO issue annotation; look there, not in the Tests
tab.

`CosmosTestAccountJanitor` is the standalone entry point used by the pipeline post step:

```bash
mvn -f sdk/cosmos/azure-cosmos-tests/pom.xml exec:java \
  -Dexec.args="--account-host <uri> --account-key <key>"
```

It deletes only the current job's databases; pass `--run-id` to target another run.

### Guardrails

Two layers, because a static scan alone is not enough:

- **At runtime**, `CosmosTestResourceRegistry` rejects any database id that CI cleanup could not
  attribute to this run, failing the test immediately and naming it. This is the layer that actually
  holds: it catches ids built at runtime, ids swapped inside a file that already has a ratchet
  allowance, and creation through APIs the scanner does not know about. Disable only with
  `-DCOSMOS.TEST_RESOURCE_ID_VALIDATION_ENABLED=false`.
- **Statically**, `TestResourceHygieneTest` ratchets against direct database creation.
  `azure-cosmos-tests/src/test/resources/test-resource-hygiene-baseline.properties` records the
  violations that existed when the check was introduced, and the build fails when a file exceeds its
  entry or a new offending file appears. It runs in the `unit` group, so it gives fast feedback on
  every PR without needing an account. Do not add entries for new tests; when you migrate a file,
  lower or remove its entry.

### Escape hatches

- `-DCOSMOS.TEST_RESOURCE_JANITOR_ENABLED=false` disables the in-process janitor entirely.
- `-DCOSMOS.TEST_RESOURCE_JANITOR_FAIL_ON_LEAK=false` keeps the cleanup but stops it from failing the
  run. Use this only to unblock a pipeline while the leaking test is being fixed.
