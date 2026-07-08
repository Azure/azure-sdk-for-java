# Test Proxy Migration


The **test proxy** is the standard recording/playback infrastructure for Azure SDK tests. It stores recordings in an external [azure-sdk-assets](https://github.com/Azure/azure-sdk-assets) repository, keeping them out of the main SDK repo.

---

## Overview

The test proxy sits between the test client and the live Azure endpoint during recording, capturing HTTP requests and responses. During playback, it serves the recorded responses.

**Key benefits:**

1. **Repo size** — recordings move out of the main repo to a dedicated assets repo
2. **Shared infrastructure** — single recording format across all Azure SDK languages
3. **Performance testing** — test proxy can serve as a local stub for benchmarks

---

## Phase 1: Migrate to Test Proxy

### 1a. Extend `TestProxyTestBase`

Change your test base class from `TestBase` to `TestProxyTestBase`:

```java
public abstract class MyServiceClientTestBase extends TestProxyTestBase {
    // existing setup code ...
}
```

### 1b. Delete Old Recordings

Remove old recordings from `src/test/resources/session-records/`.

### 1c. Record with the New Proxy

Set the test mode and run:

```bash
export AZURE_TEST_MODE=RECORD
mvn test -f sdk/<service>/<module>/pom.xml
```

New recordings land in a git-excluded `.assets/` folder at the repo root (not committed to the main repo).

### 1d. Sanitize Secrets (if needed)

Add sanitizers in your test base class — they must be registered only after the playback client or record policy is initialized. See [TableClientTestBase](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/tables/azure-data-tables/src/test/java/com/azure/data/tables/TableClientTestBase.java#L61) for an example.

---

## Phase 2: Push Recordings to the Assets Repo (First-Time Setup)

This is a **one-time step** per library. Skip if your library already has an `assets.json`.

### Prerequisites

- Library is already migrated to test proxy (Phase 1)
- Git ≥ 2.25.0
- PowerShell Core ≥ 7.0
- Global git config with `user.name` and `user.email`
- Membership in the `azure-sdk-write` GitHub group

### Steps

1. From `sdk/<service>/<module>/`, run the generation script:

   ```powershell
   ../../../eng/common/testproxy/onboarding/generate-assets-json.ps1 -InitialPush
   ```

   This:
   - Creates `assets.json` in the library directory
   - Pushes the recordings to the [azure-sdk-assets](https://github.com/Azure/azure-sdk-assets) repo
   - Deletes the recording files from the local repo

2. Verify with `git status`:
   - A new `assets.json` should appear in your library directory
   - Recording files appear as deleted

3. Commit both changes to the language repo.

---

## Phase 3: Ongoing Usage

### Playback (default in CI)

```bash
export AZURE_TEST_MODE=PLAYBACK   # or omit; PLAYBACK is the default
mvn test -f sdk/<service>/<module>/pom.xml
```

The test proxy automatically checks out the correct recording tag.

To find where recordings are stored locally:

```bash
test-proxy config locate -a ./assets.json
```

### Recording New/Updated Tests

```bash
export AZURE_TEST_MODE=RECORD
mvn test -f sdk/<service>/<module>/pom.xml

# Push updated recordings to the assets repo
test-proxy push -a sdk/<service>/<module>/assets.json
```

After pushing, `assets.json` is updated with a new tag. **Include this `assets.json` change in your PR.**

---

## Test Proxy CLI Commands

```bash
# Check out recordings for a library
test-proxy restore -a sdk/<service>/<module>/assets.json

# Push updated recordings
test-proxy push -a sdk/<service>/<module>/assets.json

# Show where recordings are stored locally
test-proxy config locate -a sdk/<service>/<module>/assets.json
```

---

## Reference Links

- [Test Proxy tool README](https://github.com/Azure/azure-sdk-tools/blob/main/tools/test-proxy/Azure.Sdk.Tools.TestProxy/README.md#installation)
- [Asset sync documentation](https://github.com/Azure/azure-sdk-tools/blob/main/tools/test-proxy/documentation/asset-sync/README.md)
- [Example migration PR](https://github.com/Azure/azure-sdk-for-java/pull/35167)

---

## See Also

- [Unit Testing](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/unit-testing.md)
- [Live Testing](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/live-testing.md)
- [TypeSpec Quickstart — Tests](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/typespec-quickstart.md#6-tests)
