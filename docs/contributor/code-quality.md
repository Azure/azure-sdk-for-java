# Code Quality Tools

> **See also**: [Building](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/building.md)

---

## Overview

The build is configured with four code-quality tools that run automatically in CI:

| Tool | What It Checks |
|------|---------------|
| **CheckStyle** | Code style and naming conventions |
| **SpotBugs** | Potential bugs via static analysis |
| **Revapi** | Breaking API changes against the latest GA release |
| **JaCoCo** | Test coverage thresholds |

All four are configured to **fail the build** on violations.  
Always run them locally before opening a pull request.

---

## Running CheckStyle and SpotBugs Locally

```bash
mvn spotbugs:check checkstyle:checkstyle-aggregate \
  -DskipTests -Dgpg.skip \
  -pl "<groupId>:<artifactId>" -am
```

Example for `azure-core`:

```bash
mvn spotbugs:check checkstyle:checkstyle-aggregate \
  -DskipTests -Dgpg.skip \
  -pl "com.azure:azure-core" -am
```

---

## Testing for Breaking API Changes (Revapi)

```bash
mvn revapi:check
```

This compares the current API surface against the latest GA version on Maven Central
and reports any incompatible changes.

### Adding a RevApi Suppression

For an approved API compatibility exception or a confirmed false positive, add
`revapi-suppressions.json` next to the SDK's `pom.xml`. The client SDK parents
automatically append this file to the shared RevApi configuration when it exists;
no SDK-specific Maven plugin configuration or empty suppression file is needed.

For example, a narrowly scoped suppression can use:

```json
[
  {
    "extension": "revapi.differences",
    "id": "sdk-suppressions",
    "configuration": {
      "ignore": true,
      "differences": [
        {
          "code": "java.method.visibilityIncreased",
          "old": "method java.lang.Long com.azure.search.documents.util.SearchPagedResponse::getCount()",
          "justification": "Non-breaking change as class is final."
        }
      ]
    }
  }
]
```

Use precise API signatures or narrowly scoped matchers, and justify every exception.
Local ownership does not change the approval requirements for breaking changes.
Keep shared analyzer, versioning, and cross-SDK policy under `eng/lintingconfigs/revapi/`;
move only SDK-specific exceptions into the local file. SDK-specific transform
configuration, such as Jackson annotation-removal exceptions, preview-annotation
filters, and approved class exclusions, also belongs in the local file.
See [Search's configuration](../../sdk/search/azure-search-documents/revapi-suppressions.json)
for an example.

Empty arrays in prefix-based `allowedPrefixes` or `ignoredPackages` settings match
the key's prefix itself (for example, `"kotlin": []`). These are active rules, not
empty suppression lists.

Keep local extension instances separate from shared instances: use a distinct `id`
(such as `sdk-suppressions`) or omit it. Reusing a shared instance's ID merges its
configuration rather than overriding it, and repeated scalar settings can fail.
Do not disable `revapi.failOnMissingConfigurationFiles`: the default failure behavior
protects the required shared configuration too.

When migrating an SDK, remove its matching central exceptions and any redundant
child-POM file registration. Use `revapi-suppressions.json`, not `revapi.json`, for
the local configuration. Analysis output remains in `target/revapi.json`, separate
from the suppression input.

After building the SDK JAR, run from its directory:

```bash
mvn revapi:check
```

---

## Generating HTML Quality Reports

```bash
mvn install site:site site:stage -Dgpg.skip
```

Report output locations:

| Report | Path |
|--------|------|
| SpotBugs | `eng/spotbugs-aggregate-report/target/spotbugs/spotbugsXml.html` |
| CheckStyle | `target/staging/checkstyle-aggregate.html` |
| JavaDoc | `target/staging/apidocs/index.html` |
| Revapi | `target/staging/revapi-aggregate-report.html` |
| Maven Site | `target/staging/index.html` |

## Generating JaCoCo Coverage Report

```bash
mvn test -Dgpg.skip -Dinclude-non-shipping-modules
```

Report: `eng/jacoco-test-coverage/target/site/test-coverage/index.html`

---

## Skipping Analysis During Local Development

Add these flags to any Maven command for a faster local build:

```
-Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotbugs.skip=true -Drevapi.skip=true
```

> **Do not skip these in your final PR build.** CI will check them regardless.

---

## Configuration Files

Shared linting configuration lives under `eng/lintingconfigs/`. SDK-local suppression
files live next to the SDK's `pom.xml`:

| File | Purpose |
|------|---------|
| `eng/lintingconfigs/checkstyle/{clientcore,track2,vnext}/checkstyle.xml` | Shared CheckStyle rules |
| `eng/lintingconfigs/checkstyle/track2/checkstyle-suppressions.xml` | Per-module suppressions |
| `eng/lintingconfigs/spotbugs/spotbugs-exclude.xml` | SpotBugs exclusion filters |
| `eng/lintingconfigs/revapi/{clientcore,track2}/revapi.json` | Shared RevApi policy and cross-SDK exceptions |
| `sdk/<service>/<artifact>/checkstyle-suppressions.xml` | SDK-local Checkstyle suppressions |
| `sdk/<service>/<artifact>/revapi-suppressions.json` | SDK-local RevApi exceptions |
| `sdk/<service>/<artifact>/spotbugs-exclude.json` | SDK-local SpotBugs exclusion filters |

### Adding a CheckStyle Suppression

If a package has a legitimately long name (approved by architects), add a suppression:

```xml
<!-- eng/lintingconfigs/checkstyle/track2/checkstyle-suppressions.xml -->
<suppress checks="PackageName" files="com/azure/resourcemanager/<verylongsegment>/.*\.java"/>
```

> **Important:** Never disable CheckStyle or SpotBugs rules globally.  
> File-scoped suppressions require justification in the PR description.

---

## Javadoc Guidelines

- See [JavaDoc & Code Snippets](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/javadocs.md)
