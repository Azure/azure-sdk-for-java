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

The linting configuration files live under `eng/lintingconfigs/`:

| File | Purpose |
|------|---------|
| `eng/lintingconfigs/checkstyle/track2/checkstyle.xml` | CheckStyle rules |
| `eng/lintingconfigs/checkstyle/track2/checkstyle-suppressions.xml` | Per-module suppressions |
| `eng/lintingconfigs/spotbugs/spotbugs-exclude.xml` | SpotBugs exclusion filters |

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
