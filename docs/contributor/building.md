# Building the Azure SDK for Java

> **See also**: [Getting Started](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/getting-started.md) · [Code Quality](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/code-quality.md)

---

## Prerequisites

Install a JDK and Maven, and configure `JAVA_HOME` and `PATH`. Use **JDK 25** for local development.
See [Getting Started](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/getting-started.md)
for environment setup and [Azure Artifacts Feed Setup](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md#azure-artifacts-feed-setup)
for dependency resolution and authentication.

Many client libraries target Java 8, but build requirements vary by module. The root reactor includes
Spring modules that require JDK 17 or later; JDK 8 and 11 cannot build the entire repository.

Run the commands below from the repository root. Replace placeholders such as `<service>` and `<artifact>`
with the actual directory names.

---

## Common Build Commands

### Build the root reactor (skip tests and common quality checks)

```bash
mvn install -f pom.xml \
  -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip \
  -Drevapi.skip -DskipSpringITs -DskipTests -Dspotbugs.skip -Djacoco.skip -Dspotless.skip
```

> **PowerShell note:** If the `-D` flags cause a parse error, use the stop-parsing token:
> ```powershell
> mvn --% install -f pom.xml -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -DskipSpringITs -DskipTests -Dspotbugs.skip -Djacoco.skip -Dspotless.skip
> ```

### Build a specific service

```bash
mvn install -f sdk/<service>/pom.xml -Dgpg.skip -Drevapi.skip -DskipTests
# Example:
mvn install -f sdk/appconfiguration/pom.xml -Dgpg.skip -Drevapi.skip -DskipTests
```

### Build with tests

Remove `-DskipTests` from the build commands above. Remove `-Djacoco.skip` as well if you want coverage reports.
Spring integration tests have a separate switch, `-DskipSpringITs=false`, and require their own test setup.

### Build selected client libraries

Select libraries from the root reactor by their Maven coordinates. For example, build App Configuration
and any dependencies with matching versions in the reactor:

```bash
mvn install -f pom.xml -Dgpg.skip -Drevapi.skip -DskipTests \
  -pl com.azure:azure-data-appconfiguration -am
```

`-pl` selects projects and `-am` also builds their reactor dependencies. This does not select all Track 2
libraries; there is no Track 2-only aggregate module in the root reactor.

---

## Skipping Analysis Locally

During iterative development you can skip common quality checks and Javadoc generation to speed up the build:

```bash
-Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotbugs.skip=true \
  -Drevapi.skip=true -Djacoco.skip=true -Dspotless.skip=true
```

Append these flags to a Maven build command. They do not disable compilation, snippet verification,
or every module-specific check. `-Dspotless.skip=true` also prevents automatic source formatting.

> **Reminder:** Always run analysis before opening a pull request.  
> See [Code Quality](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/code-quality.md) for the specific commands.

---

## Generating HTML Reports

### SpotBugs, Checkstyle, Revapi, and Javadoc

Generate reports for an individual SDK module. For example:

```bash
mvn verify site:site -f sdk/appconfiguration/azure-data-appconfiguration/pom.xml \
  -Dgpg.skip -Dspotbugs.skip=false
```

The client parent disables SpotBugs by default, so enable it explicitly for this command. Do not use
the local analysis skip flags when generating reports. Maven Site uses the module's configured reports,
not repository-wide aggregate reports.

### JaCoCo test coverage

```bash
mvn verify -f sdk/appconfiguration/azure-data-appconfiguration/pom.xml -Dgpg.skip
```

The client parent merges unit and integration test coverage and generates the HTML report during
`verify`. Running only `mvn test` collects unit test coverage data but does not generate this report.
Do not set `-DskipTests` or `-Djacoco.skip` when collecting coverage.

For SDK modules using the client parent's default reporting configuration, output paths are relative
to the module directory (for the examples above, `sdk/appconfiguration/azure-data-appconfiguration/`):

| Report | Path |
|--------|------|
| SpotBugs | `target/site/spotbugs.html` |
| Checkstyle | `target/site/checkstyle.html` |
| Javadoc | `target/site/apidocs/index.html` |
| Revapi | Linked from `target/site/project-reports.html` |
| Maven Site | `target/site/index.html` |
| JaCoCo | `target/site/test-coverage/index.html` |

---

## Code Snippets in README Files

README samples use the [CodeSnippet Maven Plugin](https://github.com/Azure/azure-sdk-tools/tree/main/packages/java-packages/codesnippet-maven-plugin) so that samples stay in sync with source code.

Snippet injection requires Java and Maven, not Node.js. The client parent configures the plugin to
update snippets during the build and verify them during `verify`.

**Steps to add a new snippet:**

1. Add or update `ReadmeSamples.java` under the library's `src/samples/java/` directory, using its Java package structure.
2. Add snippet blocks using the [snippet reference format](https://github.com/Azure/azure-sdk-tools/tree/main/packages/java-packages/codesnippet-maven-plugin#defining-a-codesnippet-reference). The identifier in the `BEGIN:` and `END:` markers must match the README reference.
3. In `README.md`, add the [injection reference](https://github.com/Azure/azure-sdk-tools/tree/main/packages/java-packages/codesnippet-maven-plugin#injecting-codesnippets-into-readmes):

   ````markdown
   ```java readme-sample-yourSampleName
   // snippet injected here automatically
   ```
   ````

4. Rebuild the package; the snippet is embedded automatically:

   ```bash
  mvn clean install -f sdk/<service>/<artifact>/pom.xml -Dgpg.skip
   ```

5. Verify `README.md` contains the injected sample.

---

## Project Structure: `pom.xml` vs `sdk/<service>/pom.xml`

The repo commonly uses three Maven build scopes:

| File | Purpose |
|------|---------|
| `pom.xml` (root) | Aggregates service areas and shared/tooling modules for repo-wide builds |
| `sdk/<service>/pom.xml` | Service-level aggregator POM for building modules under a single service area |
| `sdk/<service>/<artifact>/pom.xml` | Builds an individual SDK or other module |

Use `sdk/<service>/pom.xml` to build a service area, or the individual module POM for a focused build.
Service pipelines may use additional project selection or profiles; consult the service's `ci.yml`.
