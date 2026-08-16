# Building the Azure SDK for Java

> **See also**: [Getting Started](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/getting-started.md) · [Code Quality](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/code-quality.md)

---

## Prerequisites

Install the build tooling once before running the commands below:

```bash
mvn install -f eng/code-quality-reports/pom.xml
```

The build system supports **JDK 8 through JDK 21**. All commands below work on any supported JDK.

---

## Common Build Commands

### Build everything (skip tests and analysis)

```bash
mvn install -f pom.xml \
  -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip \
  -Drevapi.skip -DskipSpringITs -DskipTests -Dspotbugs.skip -Djacoco.skip
```

> **PowerShell note:** If the `-D` flags cause a parse error, use the stop-parsing token:
> ```powershell
> mvn --% install -f pom.xml -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -DskipSpringITs -DskipTests -Dspotbugs.skip -Djacoco.skip
> ```

### Build a specific service

```bash
mvn install -f sdk/<servicename>/pom.xml -Dgpg.skip -Drevapi.skip -DskipTests
# Example:
mvn install -f sdk/appconfiguration/pom.xml -Dgpg.skip -Drevapi.skip -DskipTests
```

### Build with tests

Remove `-DskipTests` from any of the above commands.

### Build the Track 2 client libraries

```bash
mvn install -f pom.xml -Dgpg.skip -Drevapi.skip -DskipTests \
  -pl com.azure:jacoco-test-coverage -am
```

---

## Skipping Analysis Locally

During iterative development you can skip all code-quality tools to speed up the build:

```bash
-Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotbugs.skip=true -Drevapi.skip=true
```

> **Reminder:** Always run analysis before opening a pull request.  
> See [Code Quality](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/code-quality.md) for the specific commands.

---

## Generating HTML Reports

### Spotbugs, CheckStyle, Revapi, and JavaDocs

```bash
mvn install site:site site:stage -Dgpg.skip
```

### JaCoCo test coverage

```bash
mvn test -Dgpg.skip -Dinclude-non-shipping-modules
```

Report output locations after generation:

| Report | Path |
|--------|------|
| SpotBugs | `eng/spotbugs-aggregate-report/target/spotbugs/spotbugsXml.html` |
| CheckStyle | `target/staging/checkstyle-aggregate.html` |
| JavaDoc | `target/staging/apidocs/index.html` |
| Revapi | `target/staging/revapi-aggregate-report.html` |
| Maven Site | `target/staging/index.html` |
| JaCoCo | `eng/jacoco-test-coverage/target/site/test-coverage/index.html` |

---

## Code Snippets in README Files

README samples use the [CodeSnippet Maven Plugin](https://github.com/Azure/azure-sdk-tools/tree/main/packages/java-packages/codesnippet-maven-plugin) so that samples stay in sync with source code.

**Steps to add a new snippet:**

1. Install [Node.js](https://nodejs.org/en/download/).
2. Create `ReadmeSamples.java` in `src/samples/java/` for the library.
3. Add snippet blocks using the [snippet reference format](https://github.com/Azure/azure-sdk-tools/tree/main/packages/java-packages/codesnippet-maven-plugin#defining-a-codesnippet-reference).
4. In `README.md`, add the [injection reference](https://github.com/Azure/azure-sdk-tools/tree/main/packages/java-packages/codesnippet-maven-plugin#injecting-codesnippets-into-readmes):

   ````markdown
   ```java readme-sample-yourSampleName
   // snippet injected here automatically
   ```
   ````

5. Rebuild the package; the snippet is embedded automatically:

   ```bash
   mvn clean install path/to/client/pom.xml
   ```

6. Verify `README.md` contains the injected sample.

---

## Project Structure: `pom.xml` vs `sdk/<service>/pom.xml`

The repo commonly uses two Maven build scopes:

| File | Purpose |
|------|---------|
| `pom.xml` (root) | Aggregates _all_ modules for repo-wide builds |
| `sdk/<service>/pom.xml` | Service-level aggregator POM for building modules under a single service area |

Use `sdk/<service>/pom.xml` when building within a service pipeline context or when you want to build only one service area.
