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
| `janitor.yml` | scheduled every 6h | cancelled and timed-out jobs, where nothing in the job ever ran |

Every layer scopes deletion by the run id (`CosmosTestRunId`), derived from `System.JobId` (a GUID that
is unique per job), with the build id carried along so a stray database can be traced back to a build.

When a leak fails a run, TestNG has already written its reports by the time the janitor runs, so the
**published test results show `Tests run: 0` rather than the leak** — which reads like an infrastructure
fault. The leak is reported in the build log and as an ADO issue annotation; look there, not in the Tests
tab. Databases from *other* runs are only removed by the age based
sweep, whose threshold (8h) is comfortably longer than the longest test stage.

`CosmosTestAccountJanitor` is the standalone entry point used by both pipeline layers:

```bash
mvn -f sdk/cosmos/azure-cosmos-tests/pom.xml exec:java \
  -Dexec.args="--account-host <uri> --account-key <key> --older-than 8"
```

Omit `--older-than` to delete only the current job's databases.

### Guardrail

`TestResourceHygieneTest` is a ratchet:
`azure-cosmos-tests/src/test/resources/test-resource-hygiene-baseline.properties` records the
violations that existed when the check was introduced, and the build fails when a file exceeds its
entry or a new offending file appears. Do not add entries for new tests; when you migrate a file,
lower or remove its entry.

### Escape hatches

- `-DCOSMOS.TEST_RESOURCE_JANITOR_ENABLED=false` disables the in-process janitor entirely.
- `-DCOSMOS.TEST_RESOURCE_JANITOR_FAIL_ON_LEAK=false` keeps the cleanup but stops it from failing the
  run. Use this only to unblock a pipeline while the leaking test is being fixed.
