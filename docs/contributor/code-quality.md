# Code Quality Tools

> **See also**: [Building](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/building.md)

---

## Overview

The client SDK parents configure the following quality tools. CI selects the checks and enforcement
settings for each job; not every job runs every tool.

| Tool | What It Checks |
|------|---------------|
| **CheckStyle** | Code style and naming conventions |
| **SpotBugs** | Potential bugs via static analysis |
| **Revapi** | Breaking API changes against the latest GA release |
| **JaCoCo** | Test coverage thresholds |
| **Spotless** | Java source formatting |

Enabled checks can fail the build on violations, depending on the SDK and pipeline configuration.
The client parent skips SpotBugs by default, although some SDKs, including `azure-core`, enable it.
Spotless can update source formatting during `process-sources` and checks it during `verify`.
Run the relevant checks locally before opening a pull request.

---

## Running CheckStyle and SpotBugs Locally

Run these commands from the repository root, replacing `<groupId>:<artifactId>` with the SDK's Maven
coordinates. `-am` includes dependencies with matching versions in the reactor.

```bash
mvn test-compile spotbugs:check checkstyle:check \
  -DskipTests -Dgpg.skip -Dspotbugs.skip=false \
  -pl "<groupId>:<artifactId>" -am
```

Example for `azure-core`:

```bash
mvn test-compile spotbugs:check checkstyle:check \
  -DskipTests -Dgpg.skip -Dspotbugs.skip=false \
  -pl "com.azure:azure-core" -am
```

`test-compile` refreshes production, test, and sample bytecode without running tests. Direct SpotBugs
goals do not compile sources themselves, so omitting compilation can analyze stale classes or fail
on a clean checkout. `-Dspotbugs.skip=false` enables analysis even when the SDK inherits the parent's
default skip setting. `checkstyle:check` enforces violations; generate HTML reports separately below.

### Adding a SpotBugs Exclusion

For a confirmed false positive or an approved exception, add `spotbugs-exclude.xml`
next to the SDK's `pom.xml`. The client SDK parents (including ClientCore and the
legacy data SDK parent) automatically append this optional file to the shared
exclusions. No SDK-level SpotBugs plugin declaration, filter registration, or merge
command is needed. Both direct `spotbugs:check` invocations and lifecycle builds use
the same filters. The Azure client parents and SDK build tool inherit this behavior
from `azure-sdk-parent`; `clientcore-parent` provides the equivalent configuration.
Existing SDK-specific detector and analysis overrides remain independent of filter
composition.

Use SpotBugs' filter format, with a comment explaining why the exception is needed:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<FindBugsFilter
    xmlns="https://github.com/spotbugs/filter/4.10.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="https://github.com/spotbugs/filter/4.10.0 https://raw.githubusercontent.com/spotbugs/spotbugs/4.10.0/spotbugs/etc/findbugsfilter.xsd">
  <!-- The API distinguishes a missing byte array from an empty byte array. -->
  <Match>
    <Class name="com.azure.example.models.ExampleModel"/>
    <Method name="getBytes"/>
    <Bug pattern="PZLA_PREFER_ZERO_LENGTH_ARRAYS"/>
  </Match>
</FindBugsFilter>
```

Keep exclusions as narrow as possible. SDK-specific classes, methods, fields, and
packages belong in the SDK's local file; only cross-SDK policy belongs in
`eng/lintingconfigs/spotbugs/track2/spotbugs-exclude.xml`. The shared include filter
is applied independently by the modern client parents.

The parents use SpotBugs' native `excludeFilterFiles` list and Maven's
`combine.children="append"` inheritance. Paths are resolved from each module's
base directory, including in reactor builds. A missing local file is optional;
a missing or malformed configured filter is an error. Do not replace the inherited
list in a child POM.

Both source files keep the name `spotbugs-exclude.xml`. Since the Maven plugin copies
filters by basename, the parents reference the SDK-local file using Maven's encoded
`project.baseUri` and a `#sdk` URI fragment. File URLs ignore the fragment when
reading the source, but the plugin retains it in the copied resource's name,
preventing collisions with the shared filter. Keep this fragment in the parent
configuration. Shared paths use native separators to keep copied resources in the
build output directory on Windows as well as Linux.

Periodically remove exclusions for deleted or renamed targets and redundant rules.
Do not remove a rule merely because a filtered report is empty. Re-run analysis
with all proposed removals together; for a local audit, disable only the discovery
profile while retaining shared policy:

```bash
mvn compile spotbugs:check -Dspotbugs.skip=false "-P!local-spotbugs-exclude"
```

`-Dspotbugs.excludeFilterFile=` no longer disables the inherited filters. Keep an
existing local file when no exclusions remain, using an empty `FindBugsFilter`
element as a placeholder. Empty and absent local files both leave shared policy
active. Configuration regression coverage lives
in `eng/scripts/tests/SpotBugs-Configuration.tests.ps1` (Pester `UnitTest` and
Maven-backed `IntegrationTest` tags).

---

## Testing for Breaking API Changes (Revapi)

After building the SDK JAR, run this command from the SDK's directory:

```bash
mvn revapi:check
```

This compares the current API surface against the latest GA version available through the configured
Maven repositories and reports incompatible changes. The client parent uses a `versionFormat` filter
to exclude prerelease versions from the baseline.

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
          "old": "method void com.azure.search.documents.knowledgebases.models.KnowledgeBaseImageContent::<init>(java.lang.String)",
          "justification": "Non-breaking change: constructor visibility increased on a final class."
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
See [Search's configuration](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/revapi-suppressions.json)
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

Periodically revalidate SDK-local suppressions against the current GA baseline.
Check proposed removals together: overlapping rules may each appear unnecessary
when removed individually, even though at least one is still required. Remove empty
extension blocks and delete the file when no configuration remains. A missing prior
GA baseline is inconclusive, not evidence that a suppression is unnecessary.

---

## Generating HTML Quality Reports

Generate reports for an individual SDK module. Run from the repository root; for example:

```bash
mvn verify site:site -f sdk/appconfiguration/azure-data-appconfiguration/pom.xml \
  -Dgpg.skip -Dspotbugs.skip=false
```

Enable SpotBugs explicitly and do not use the local analysis skip flags when generating reports.
Maven Site generates the module's configured reports, not repository-wide aggregate reports.

For modules using the client parent's default reporting configuration, output paths are relative to
the module directory (for this example, `sdk/appconfiguration/azure-data-appconfiguration/`):

| Report | Path |
|--------|------|
| SpotBugs | `target/site/spotbugs.html` |
| CheckStyle | `target/site/checkstyle.html` |
| JavaDoc | `target/site/apidocs/index.html` |
| Revapi | Linked from `target/site/project-reports.html` |
| Maven Site | `target/site/index.html` |

## Generating JaCoCo Coverage Report

```bash
mvn verify -f sdk/appconfiguration/azure-data-appconfiguration/pom.xml -Dgpg.skip
```

The client parent merges unit and integration test coverage and generates the HTML report during
`verify`. Running only `mvn test` collects unit test coverage data but does not generate this report.
Do not set `-DskipTests` or `-Djacoco.skip` when collecting coverage.

Report: `target/site/test-coverage/index.html`, relative to the SDK module directory.

---

## Skipping Analysis During Local Development

Append these flags to a Maven build command to skip common quality checks and Javadoc generation
during local iteration:

```bash
-Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotbugs.skip=true \
  -Drevapi.skip=true -Djacoco.skip=true -Dspotless.skip=true
```

These flags do not disable compilation, snippet verification, or every module-specific check.
`-Dspotless.skip=true` also prevents automatic source formatting.

> **Do not skip the required checks in your final PR build.** Follow the SDK's CI configuration,
> and explicitly enable SpotBugs with `-Dspotbugs.skip=false` when running it locally.

---

## Configuration Files

Shared linting configuration lives under `eng/lintingconfigs/`. SDK-local suppression
files live next to the SDK's `pom.xml`:

| File | Purpose |
|------|---------|
| `eng/lintingconfigs/checkstyle/{clientcore,track2,vnext}/checkstyle.xml` | Shared CheckStyle rules |
| `eng/lintingconfigs/checkstyle/track2/checkstyle-suppressions.xml` | Fallback suppressions when an SDK has no local file |
| `eng/lintingconfigs/spotbugs/track2/spotbugs-exclude.xml` | Shared cross-SDK SpotBugs exclusions |
| `eng/lintingconfigs/spotbugs/spotbugs-include.xml` | Shared SpotBugs inclusion policy for modern clients |
| `eng/lintingconfigs/revapi/{clientcore,track2}/revapi.json` | Shared RevApi policy and cross-SDK exceptions |
| `sdk/<service>/<artifact>/checkstyle-suppressions.xml` | SDK-local Checkstyle suppressions |
| `sdk/<service>/<artifact>/revapi-suppressions.json` | SDK-local RevApi exceptions |
| `sdk/<service>/<artifact>/spotbugs-exclude.xml` | SDK-local SpotBugs exclusion filters |

### Adding a CheckStyle Suppression

For an architect-approved long package name, add a narrowly scoped entry inside the `<suppressions>`
element in the SDK's local `checkstyle-suppressions.xml`. For example:

```xml
<suppress checks="PackageName"
          files="com[/\\]azure[/\\]resourcemanager[/\\]kubernetesconfiguration[/\\].*\.java"/>
```

`[/\\]` matches both Unix and Windows path separators. Replace the example package with the specific
approved package; do not broaden the exception beyond the affected SDK.

The client parents automatically select an SDK-local file when it exists. Unlike SpotBugs and RevApi
configuration, local CheckStyle suppressions **replace**, rather than append to, the shared suppression
file. When creating a local file, retain the applicable shared suppressions. Changes to the central
file do not affect SDKs already using a local file; reserve central changes for cross-SDK policy.

> **Important:** Never disable CheckStyle or SpotBugs rules globally.  
> File-scoped suppressions require justification in the PR description.

---

## Javadoc Guidelines

- See [JavaDoc & Code Snippets](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/javadocs.md)
