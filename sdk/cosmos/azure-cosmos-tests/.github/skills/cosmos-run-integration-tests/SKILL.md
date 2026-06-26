---
name: cosmos-run-integration-tests
description: >
  Run azure-cosmos integration/customer-workflow tests locally, closely following the CI
  pipeline (build+install, then failsafe `verify` with a test profile) using one consistent
  JDK for both steps.
  USE WHEN: asked to run cosmos integration tests, customer-workflow tests
  (fi-customer-workflows / fi-sm-customer-workflows), reproduce a CI test failure
  locally, or run a specific cosmos test profile via Maven. Covers the same-JDK
  build/test requirement, the two-step build/test split, profile→group→file mapping, and the
  required account env vars.
  NOT FOR: unit tests only, Spark/Kafka connector tests, or non-cosmos modules.
---

# Running azure-cosmos integration tests locally (CI-equivalent)

## TL;DR
CI runs tests in **two separate Maven invocations**, both on the **same JDK** (the version CI pins
via `JavaTestVersion` in `eng/pipelines/templates/variables/globals.yml`):
1. **Build + install** (`-DskipTests ... install`) — compiles main + test classes, installs jars.
2. **Test** (`verify -P<profile> -DskipCompile=true -DskipTestCompile=true`) — failsafe runs the TestNG suite.

Because step 2 skips compilation, it runs the classes/jars step 1 produced — so **both steps must use
the same JDK** (see below).

There is no separate "surefire command" — integration tests run through **failsafe** via `mvn verify -P<profile>`.

## ⚠️ Critical: use the *same* JDK for build (step 1) and test (step 2)
Step 1 compiles main + test classes and installs the `azure-cosmos` / `azure-cosmos-tests` jars into
`~/.m2` using whatever `JAVA_HOME` you build with. A local incremental `install` packages those classes
at the **build JDK's class-file version** (the `java9plus` profile's `default-compile` /
`default-testCompile` use `<release>${java.vm.specification.version}</release>`, i.e. the build VM's
version). Step 2 then **skips compilation** (`-DskipCompile=true -DskipTestCompile=true`) and runs those
already-built classes.

So if step 1 and step 2 use **different** JDKs you get class-file version mismatches. Example: build on a
newer JDK (class file v65/v69), then run step 2 on JDK 17 (v61) and `javac` / the runtime rejects the
jars with **misleading** errors like:

```
cannot access com.azure.cosmos.implementation.TestConfigurations
  bad class file: ...azure-cosmos-*.jar(.../TestConfigurations.class)
  class file has wrong version 65.0, should be 61.0
```

This is **NOT** a JPMS / module-path / `javaModulesSurefireArgLine` problem. The parent already sets
`useModulePath=false`; `azure-cosmos` lands on `-classpath` correctly. The only cause is the JDK mismatch
between the two steps.

**Fix:** pick one JDK and use it for *both* steps — ideally the major version CI uses for tests
(`JavaTestVersion` in `eng/pipelines/templates/variables/globals.yml`). Point `JAVA_HOME` at that JDK and
prepend it to `PATH` at the top of every command (adjust the path to your local install):
```powershell
$env:JAVA_HOME='<path-to-your-jdk>'   # e.g. C:\Program Files\OpenLogic\jdk-21.0.10.7-hotspot
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
```

## Step 1 — Build + install (use the same JDK as step 2)
Quote **every** `-D` flag in PowerShell (unquoted `.skip` args get mis-parsed as lifecycle phases).
```powershell
mvn --batch-mode --fail-at-end '-DskipTests' '-Dgpg.skip=true' '-Dmaven.javadoc.skip=true' `
  '-Dcodesnippet.skip=true' '-Dspotbugs.skip=true' '-Dcheckstyle.skip=true' '-Drevapi.skip=true' `
  '-Dspotless.apply.skip=true' '-Dspotless.check.skip=true' '-Djacoco.skip=true' '-Denforcer.skip=true' `
  '-T' '2C' '-pl' 'com.azure:azure-cosmos,com.azure:azure-cosmos-tests' '-am' 'install'
```

## Step 2 — Run a test profile via failsafe (skip compile like CI)
```powershell
mvn '-pl' 'com.azure:azure-cosmos-tests' 'verify' '-Pfi-sm-customer-workflows' `
  '-DskipCompile=true' '-DskipTestCompile=true' '-DcreateSourcesJar=false' `
  "-DACCOUNT_HOST=$env:ACCOUNT_HOST" "-DACCOUNT_KEY=$env:ACCOUNT_KEY" `
  '-DACCOUNT_CONSISTENCY=Session' '-DCOSMOS.CLIENT_LEAK_DETECTION_ENABLED=true' `
  '-Dgpg.skip=true' '-Dspotbugs.skip=true' '-Dcheckstyle.skip=true' '-Drevapi.skip=true' `
  '-Dspotless.apply.skip=true' '-Dspotless.check.skip=true' '-Djacoco.skip=true' '-Denforcer.skip=true' `
  2>&1 | Tee-Object -FilePath fi-sm-run1.log
```
- Failsafe report: `sdk/cosmos/azure-cosmos-tests/target/failsafe-reports/TestSuite.txt`
- Summary line to look for: `Tests run: N, Failures: F, Errors: E, Skipped: S`

## Profile → group → file mapping (customer workflows)
| Profile | Test group | Account shape | Files |
|---|---|---|---|
| `fi-customer-workflows` | `fi-customer-workflows` | multi-master | 9 test files |
| `fi-sm-customer-workflows` | `fi-sm-customer-workflows` | single-master, multi-region | 1 file: `CustomerWorkflowSingleMasterAvailabilityTest` |

Each profile sets a `suiteXmlFile` (e.g. `src/test/resources/fi-sm-customer-workflows-testng.xml`)
in `sdk/cosmos/azure-cosmos-tests/pom.xml`. Other profiles (`direct`, `multi-master`, `fi-multi-master`,
`thinclient`, etc.) follow the same `-P<id>` pattern.

## Required env vars
| Var | Notes |
|---|---|
| `ACCOUNT_HOST` | e.g. `https://<acct>.documents.azure.com:443/` |
| `ACCOUNT_KEY` | primary key (88 chars) |
| `ACCOUNT_CONSISTENCY` | CI passes `Session` for these profiles; defaults to `Strong` if unset |

## Notes
- `javaModulesSurefireArgLine` (sdk/cosmos/azure-cosmos-tests/pom.xml) is the **runtime** `--add-opens` block
  for reflective test access; the parent injects it into the surefire/failsafe argLine. It does not
  affect compilation.
- CI source of truth: `sdk/cosmos/tests.yml` (`TestGoals: verify`,
  `TestOptions: $(ProfileFlag) -DskipCompile=true -DskipTestCompile=true -DcreateSourcesJar=false`).
- The commands above add a few **local-convenience flags CI does not use** (e.g. `-Denforcer.skip=true`
  plus the various `*.skip` flags) to speed up local runs. They are intentional — this skill reproduces
  the test run, not a byte-for-byte CI build. Drop `-Denforcer.skip=true` if you also want CI's enforcer
  checks locally.
- To run a single test, add `'-Dit.test=CustomerWorkflowSingleMasterAvailabilityTest#<method>'`
  (failsafe uses `it.test`, not `test`).