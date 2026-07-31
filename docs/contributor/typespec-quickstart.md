# TypeSpec Java Quickstart


This guide covers the end-to-end workflow for generating, building, testing, and releasing a Java SDK from a TypeSpec specification. For OpenAPI 2.0 specs, see [Working with AutoRest](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/autorest.md).

---

## Prerequisites

- Familiarity with [Java Azure SDK Design Guidelines](https://azure.github.io/azure-sdk/java_introduction.html)
- Java 17 or 21 (recommended), Maven, Node.js 22+
- `tsp-client` installed globally: `npm install -g @azure-tools/typespec-client-generator-cli`
- Git

---

## Workflow Overview

```
TypeSpec spec (azure-rest-api-specs)
    ↓ tsp-client init / update
Generated Java SDK (azure-sdk-for-java)
    ↓ mvn verify
Build + test
    ↓ Prepare-Release.ps1
Release
```

---

## 1. Module Naming

- `service` = service directory name in [azure-rest-api-specs](https://github.com/Azure/azure-rest-api-specs), e.g. `storage`
- `module` = Java artifact ID derived from namespace, e.g. `azure-storage-blob` for `com.azure.storage.blob`
- Output directory: `sdk/<service>/<module>/`

---

## 2. Generate the SDK

### Option A: SDK Generation Pipeline (recommended for first-time)

1. Configure `tspconfig.yaml` in [azure-rest-api-specs](https://github.com/Azure/azure-rest-api-specs) with:
   - `parameters.service-dir.default` = `sdk/<service>`
   - `options.@azure-tools/typespec-java.emitter-output-dir` = `{output-dir}/{service-dir}/<module>`
   - `namespace` for typespec-java
2. Run the [SDK Generation Pipeline](https://dev.azure.com/azure-sdk/internal/_build?definitionId=7421) with the path to `tspconfig.yaml` and the service `api-version`.

### Option B: Local first-time generation

```bash
# From the root of azure-sdk-for-java:
tsp-client init --tsp-config https://github.com/Azure/azure-rest-api-specs/blob/<commit-sha>/specification/<service>/tspconfig.yaml
```

The URL **must** reference a specific commit SHA (not a branch name).

### Option C: Follow-up (re-generation after spec changes)

```bash
# From sdk/<service>/<module>/ directory:
tsp-client update
```

To test with locally modified TypeSpec, add `--save-inputs` and then re-run `tsp-client generate --save-inputs`.

---

## 3. Build

```bash
# Full build including tests
mvn verify -f sdk/<service>/<module>/pom.xml

# Install to local Maven repository (skip Javadoc / tests)
mvn install -f sdk/<service>/<module>/pom.xml -Dgpg.skip -Drevapi.skip -DskipTests
```

Required files for a new module:

| File | Purpose |
|------|---------|
| `eng/versioning/version_client.txt` | Version registration |
| `sdk/<service>/pom.xml` | Service-level aggregator POM |
| `sdk/<service>/ci.yml` | CI pipeline |

See [Adding a Module](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/adding-a-module.md) for full details.

---

## 4. Customize the SDK

### `partial-update` (in-place customization)

Set `partial-update: true` in `tspconfig.yaml` emitter options. TypeSpec-Java will preserve hand-written modifications in `Client`, `AsyncClient`, `ClientBuilder`, and `models` package on re-generation. Remove `@Generated` from any method you've modified.

### `customization-class` (advanced AST customization)

```yaml
"@azure-tools/typespec-java":
  customization-class: customization/src/main/java/MyCustomization.java
```

---

## 5. Improve Documentation

### README

- Update `[product_documentation]` URL, getting started section, and key concept section.
- All links must be absolute. Run `cspell` to check spelling.

### Samples

- Edit `ReadmeSamples.java` between `// BEGIN: ...` / `// END: ...` markers.
- Build the project → the codesnippet plugin auto-injects them into `README.md`.

See [JavaDoc and Code Snippets](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/javadocs.md).

---

## 6. Tests

### Generated tests

TypeSpec-Java generates test files under `src/test/java/<namespace>/generated/`. A `<Client>TestBase` class is also generated.

### Live tests

```bash
export AZURE_TEST_MODE=LIVE
mvn test -f sdk/<service>/<module>/pom.xml
```

### Record mode

```bash
export AZURE_TEST_MODE=RECORD
mvn test -f sdk/<service>/<module>/pom.xml
# Then push recordings:
test-proxy push -a sdk/<service>/<module>/assets.json
```

### Playback mode (default in CI)

```bash
export AZURE_TEST_MODE=PLAYBACK   # or omit (playback is the default)
mvn test -f sdk/<service>/<module>/pom.xml
```

⚠️ Use `TestBase.setPlaybackSyncPollerPollInterval` on `SyncPoller` in LRO tests.

See [Test Proxy Migration](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/test-proxy-migration.md) for recording infrastructure.

---

## 7. Feature Checklist

| Feature | Notes |
|---------|-------|
| OAuth2 / ApiKey auth | Supported out of the box |
| Custom auth (JWT, HMAC-SHA256) | Implement as `HttpPipelinePolicy` subclass |
| LRO (long-running operations) | Default chained polling strategy; contact SDK team for non-standard patterns |
| File upload | `application/octet-stream` and `multipart/form-data` supported |

---

## 8. Code Review

1. Open a **draft** PR and self-review against the [Review Checklist](#review-checklist) below.
2. Mark as "ready to review" and comment `@Azure/dpg-devs for awareness`.
3. Open [apiview.dev](https://apiview.dev/) — upload `target/<module>-<version>-sources.jar` for architect review.

---

## 9. Release

See [Release Checklist](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/release-checklist.md) and [aka.ms/azsdk/releases/partnerinfo](https://aka.ms/azsdk/releases/partnerinfo).

Key steps:
1. Update `CHANGELOG.md` (include dependency updates for non-first releases)
2. Run `Prepare-Release.ps1` — verify release date
3. After release, approve and merge the auto-generated "Increment versions" PR

---

## Troubleshooting CI Failures

| CI Check | Fix |
|----------|-----|
| Version mismatch | `python eng/versioning/update_versions.py --sr` |
| Spelling errors | Fix or add to `.vscode/cspell.json` |
| SpotBugs / Checkstyle | Fix code; see [code-quality.md](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/code-quality.md) |
| RevApi breaking change | Add suppression to `eng/code-quality-reports/src/main/resources/revapi/revapi.json` |
| TypeSpec code outdated | Re-run `tsp-client update` |
| Insufficient test coverage | Lower thresholds for beta: add `<jacoco.min.linecoverage>0.2</jacoco.min.linecoverage>` to POM |

---

## Review Checklist

- [ ] Namespace approved by SDK arch board
- [ ] `README.md` has service introduction, Getting Started, Key Concepts, Examples
- [ ] All README links are absolute (no locale like `en-us`)
- [ ] Sample code in `README.md` is in a Java file under `samples/` and synchronized via codesnippet plugin
- [ ] CI passes
- [ ] Tests use `TestBase.setPlaybackSyncPollerPollInterval` for LRO APIs
- [ ] Test recordings contain no secrets

---

## See Also

- [Working with AutoRest](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/autorest.md) — for OpenAPI 2.0 specs
- [Adding a Module](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/adding-a-module.md)
- [Building](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/building.md)
- [JavaDoc and Code Snippets](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/javadocs.md)
- [Test Proxy Migration](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/test-proxy-migration.md)
