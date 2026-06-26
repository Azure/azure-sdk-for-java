---
name: cosmos-run-integration-tests
description: >
  Run azure-cosmos integration/customer-workflow tests locally exactly like the CI
  pipeline does (build+install on JDK 21, then failsafe `verify` with a test profile).
  USE WHEN: asked to run cosmos integration tests, customer-workflow tests
  (fi-customer-workflows / fi-sm-customer-workflows), reproduce a CI test failure
  locally, or run a specific cosmos test profile via Maven. Covers the JDK-21
  requirement, the two-step build/test split, profile→group→file mapping, and the
  required account env vars.
  NOT FOR: unit tests only, Spark/Kafka connector tests, or non-cosmos modules.
---

# Running azure-cosmos integration tests locally (CI-equivalent)

## TL;DR
CI runs tests in **two separate Maven invocations**, both on **JDK 21**:
1. **Build + install** (`-DskipTests ... install`) — compiles main + test classes, installs jars.
2. **Test** (`verify -P<profile> -DskipCompile=true -DskipTestCompile=true`) — failsafe runs the TestNG suite.

There is no separate "surefire command" — integration tests run through **failsafe** via `mvn verify -P<profile>`.

## ⚠️ Critical: use JDK 21
The `azure-cosmos` / `azure-cosmos-test` jars in `~/.m2` are compiled with **JDK 21** (class file v65).
If your shell `JAVA_HOME` is JDK 17 (v61), `javac --release 17` rejects them with **misleading**
errors like:

```
cannot access com.azure.cosmos.implementation.TestConfigurations
  bad class file: ...azure-cosmos-*.jar(.../TestConfigurations.class)
  class file has wrong version 65.0, should be 61.0
```

This is **NOT** a JPMS / module-path / `javaModulesSurefireArgLine` problem. The parent already sets
`useModulePath=false`; `azure-cosmos` lands on `-classpath` correctly. The only cause is the JDK mismatch.

Local JDK 21: `C:\Program Files\OpenLogic\jdk-21.0.10.7-hotspot`

Set it at the top of every command:
```powershell
$env:JAVA_HOME='C:\Program Files\OpenLogic\jdk-21.0.10.7-hotspot'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
```

## Step 1 — Build + install (JDK 21)
Quote **every** `-D` flag in PowerShell (unquoted `.skip` args get mis-parsed as lifecycle phases).
```powershell
mvn --batch-mode --fail-at-end '-DskipTests' '-Dgpg.skip=true' '-Dmaven.javadoc.skip=true' `
  '-Dcodesnippet.skip=true' '-Dspotbugs.skip=true' '-Dcheckstyle.skip=true' '-Drevapi.skip=true' `
  '-Dspotless.apply.skip=true' '-Dspotless.check.skip=true' '-Djacoco.skip=true' '-Denforcer.skip=true' `
  '-T' '2C' '-pl' 'com.azure:azure-cosmos,com.azure:azure-cosmos-tests' '-am' 'install'
```

## Step 2 — Run a test profile via failsafe (skip compile like CI)
```powershell
mvn '-pl' 'azure-cosmos-tests' 'verify' '-Pfi-sm-customer-workflows' `
  '-DskipCompile=true' '-DskipTestCompile=true' '-DcreateSourcesJar=false' `
  "-DACCOUNT_HOST=$env:ACCOUNT_HOST" "-DACCOUNT_KEY=$env:ACCOUNT_KEY" `
  '-DACCOUNT_CONSISTENCY=Session' '-DCOSMOS.CLIENT_LEAK_DETECTION_ENABLED=true' `
  '-Dgpg.skip=true' '-Dspotbugs.skip=true' '-Dcheckstyle.skip=true' '-Drevapi.skip=true' `
  '-Dspotless.apply.skip=true' '-Dspotless.check.skip=true' '-Djacoco.skip=true' '-Denforcer.skip=true' `
  2>&1 | Tee-Object -FilePath fi-sm-run1.log
```
- Failsafe report: `azure-cosmos-tests/target/failsafe-reports/TestSuite.txt`
- Summary line to look for: `Tests run: N, Failures: F, Errors: E, Skipped: S`

## Profile → group → file mapping (customer workflows)
| Profile | Test group | Account shape | Files |
|---|---|---|---|
| `fi-customer-workflows` | `fi-customer-workflows` | multi-master | 9 test files |
| `fi-sm-customer-workflows` | `fi-sm-customer-workflows` | single-master, multi-region | 1 file: `CustomerWorkflowSingleMasterAvailabilityTest` |

Each profile sets a `suiteXmlFile` (e.g. `src/test/resources/fi-sm-customer-workflows-testng.xml`)
in `azure-cosmos-tests/pom.xml`. Other profiles (`direct`, `multi-master`, `fi-multi-master`,
`thinclient`, etc.) follow the same `-P<id>` pattern.

## Required env vars
| Var | Notes |
|---|---|
| `ACCOUNT_HOST` | e.g. `https://<acct>.documents.azure.com:443/` |
| `ACCOUNT_KEY` | primary key (88 chars) |
| `ACCOUNT_CONSISTENCY` | CI passes `Session` for these profiles; defaults to `Strong` if unset |

## Notes
- `javaModulesSurefireArgLine` (azure-cosmos-tests/pom.xml) is the **runtime** `--add-opens` block
  for reflective test access; the parent injects it into the surefire/failsafe argLine. It does not
  affect compilation.
- CI source of truth: `sdk/cosmos/tests.yml` (`TestGoals: verify`,
  `TestOptions: $(ProfileFlag) -DskipCompile=true -DskipTestCompile=true -DcreateSourcesJar=false`).
- To run a single test, add `'-Dit.test=CustomerWorkflowSingleMasterAvailabilityTest#<method>'`
  (failsafe uses `it.test`, not `test`).