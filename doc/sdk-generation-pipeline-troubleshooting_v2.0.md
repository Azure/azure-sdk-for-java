# Java SDK Generation Pipeline Troubleshooting Guide

This guide covers common issues encountered during the Java SDK generation pipeline and how to resolve them. Issues are grouped into three categories:

1. [tspconfig.yaml Related Errors](#1-tspconfig-related-errors)
2. [Customization Related Errors](#2-customization-related-errors)
3. [Intermittent Errors (Resolved by Rerun)](#3-intermittent-errors-resolved-by-rerun)
4. [Escalation Guidance](#4-escalation-guidance)

> This guide is continuously updated. For the latest discussions, see the [Language - Java Teams channel][teams-java].

---

## Related Pipelines

- **SDK Validation Pipeline** — [SDK Validation FAQ](https://aka.ms/azsdk/sdk-automation-faq)
- **SDK Generation Pipeline** — [Using the SDK Generation Pipelines](https://eng.ms/docs/products/azure-developer-experience/develop/sdk-generation-pipelines)
- **SDK Release Pipeline** — [Run release SDK pipeline](https://eng.ms/docs/products/azure-developer-experience/develop/sdk-release/sdk-release-pipeline)
- **PR Build Failures** — [Troubleshoot PR build failures](https://eng.ms/docs/products/azure-developer-experience/support/troubleshoot/PR-failing)

---

## Overview

The Java SDK generation pipeline uses [TypeSpec](https://typespec.io/) (or legacy Swagger/AutoRest) to generate client library code. The main entry points are:

- [`eng/automation/generate.py`](../eng/automation/generate.py) — orchestrates both management-plane and data-plane generation
- [`eng/automation/generate_utils.py`](../eng/automation/generate_utils.py) — core generation logic (TypeSpec & AutoRest)
- [`eng/automation/generate_data.py`](../eng/automation/generate_data.py) — data-plane specific generation with customization fallback

A typical TypeSpec generation flow:

1. **Environment setup** — install Python, `tsp-client`, set `JAVA_HOME`
2. **Run `tsp-client init`** — invoke the TypeSpec Java emitter to generate code
3. **Detect output folder** — locate the generated SDK package via `tsp-location.yaml`
4. **Integrate into repo** — update `ci.yml`, `pom.xml`, `version_client.txt`, `CHANGELOG.md`
5. **Compile** — run `mvn clean verify` (mgmt) or `mvn clean package` (data-plane)
6. **Changelog detection** — compare with the previous published JAR from Maven Central

---

## 1. tspconfig Related Errors

The `tspconfig.yaml` file lives in the **azure-rest-api-specs** repository and controls how the TypeSpec emitter generates SDK code. Misconfigurations in this file are the most common source of generation failures.

### 1.1 Missing `namespace`

**Error message:**
```
[VALIDATE][tspconfig.yaml] options.@azure-tools/typespec-java.namespace is REQUIRED for Java SDK
```

**Cause:** The `namespace` field under `options.@azure-tools/typespec-java` is not set in `tspconfig.yaml`.

**Fix:** Add the `namespace` field to your `tspconfig.yaml`:
```yaml
options:
  "@azure-tools/typespec-java":
    namespace: "com.azure.resourcemanager.yourservice"  # management-plane
    # or
    namespace: "com.azure.yourservice"                  # data-plane
```

### 1.2 Invalid `namespace` Format

**Error message:**
```
[VALIDATE][tspconfig.yaml] namespace SHOULD start with "com.azure."
```

**Cause:** The namespace does not match the required pattern `com\.azure(\.\w+)+`.

**Fix:** Ensure the namespace starts with `com.azure.` and uses only valid Java package name segments. Examples:
- Management-plane: `com.azure.resourcemanager.fabric`
- Data-plane: `com.azure.ai.contentsafety`

### 1.3 Mismatched `emitter-output-dir` and `namespace`

**Symptom:** Generation succeeds but the output lands in an unexpected directory, or integration steps (POM updates, CI config) fail.

**Cause:** The `emitter-output-dir` path doesn't align with the `namespace` or the expected SDK folder structure.

**Fix:** Ensure the `emitter-output-dir` matches the convention:
```yaml
emitter-output-dir: "{project-root}/sdk/{service}/{package-name}"
```
Where `{package-name}` corresponds to the Maven artifact name derived from your namespace (e.g., `azure-resourcemanager-fabric`).

### 1.4 No SDK Folder Found After Generation

**Error message:**
```
[GENERATE] Code generation failed. No sdk folder found.
```

**Cause:** After `tsp-client init` completes, the pipeline checks `git status --porcelain **/tsp-location.yaml` to discover which SDK folder was created or updated. If no `tsp-location.yaml` was modified, the pipeline assumes generation produced no output.

**Possible reasons:**
- The `tspconfig.yaml` doesn't have a Java emitter configuration at all
- The `emitter-output-dir` points to a location outside the repo
- The TypeSpec project has no operations or models to generate

**Fix:** Verify that `tspconfig.yaml` includes a valid `@azure-tools/typespec-java` section with correct `namespace` and `emitter-output-dir`.

### 1.5 SDK Release Type Validation Failures

**Error messages:**
```
Invalid SDK release type [{type}], only support 'stable' or 'beta'.
SDK release type is [stable], but API version [{ver}] is preview.
Both [API version] and [SDK release type] parameters are required for self-serve generation.
```

**Cause:** The `sdkReleaseType` and `apiVersion` parameters passed to the pipeline are inconsistent or incomplete.

**Fix:**
- Use only `stable` or `beta` for `sdkReleaseType`
- Do not pair a `stable` release type with a preview API version (e.g., `2024-01-01-preview`)
- When triggering self-serve generation, provide **both** `apiVersion` and `sdkReleaseType`

> **Real-world examples:**
> - ["I am creating a new API version... pull request..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1748961827942?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1748961827942&parentMessageId=1748961827942) — version/release type mismatch
> - ["My team has an existing Java sdk for a stable version..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1748900355124?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1748900355124&parentMessageId=1748900355124) — stable vs preview confusion
> - ["Does anyone know how to make sure preview versions aren't..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1745338247039?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1745338247039&parentMessageId=1745338247039)
> - ["Java sdk generation step is failing with below error..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1744960440431?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1744960440431&parentMessageId=1744960440431)

### 1.6 TypeSpec Version / Deprecated Decorator Errors

**Symptom:** Generation fails with errors about unrecognized or deprecated TypeSpec decorators (e.g., `@service({title: ...})`, `@pageable`).

**Cause:** TypeSpec compiler versions evolve and deprecate decorators. For example, TypeSpec 0.67+ removed support for previously deprecated usage like `@service({title: ...})` and `@pageable`.

**Fix:**
- Update your TypeSpec files to use the current decorator syntax
- Check the [TypeSpec changelog](https://typespec.io/) for migration guidance on deprecated decorators
- Ensure the `@typespec/compiler` version in [`eng/emitter-package.json`](../eng/emitter-package.json) is compatible with your spec

> **Real-world examples:**
> - ["TypeSpec had deprecated pageable decorator in Core libs..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1754623358318?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1754623358318&parentMessageId=1754623358318)
> - ["Due to TypeSpec 0.67 discontinued most of the deprecated usage..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1742785557922?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1742785557922&parentMessageId=1742785557922)
> - ["We are seeing Java SDK validation failure on our Swagger PR..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1760004948450?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1760004948450&parentMessageId=1760004948450)

### 1.7 `emitter-output-dir` Configuration Change

**Symptom:** Generation fails or outputs to the wrong location after an EngSys configuration change.

**Cause:** EngSys may update `tspconfig.yaml` to use `emitter-output-dir` instead of other options for specifying the output directory.

**Fix:** Follow the latest guidance in the spec PR and ensure your `tspconfig.yaml` uses the current format:
```yaml
emitter-output-dir: "{project-root}/sdk/{service}/{package-name}"
```

> **Real-world example:**
> - ["EngSys plan to modify tspconfig.yaml, use emitter-output-dir option instead of..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1755227248942?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1755227248942&parentMessageId=1755227248942)

---

## 2. Customization Related Errors

The Java SDK generation pipeline supports hand-written customization code that coexists with generated code. Customization can fail when generated APIs change and become incompatible with existing custom code.

### 2.1 How Customization Works

| Mechanism | Description |
|---|---|
| **Generated code headers** | Files containing `"Code generated by Microsoft (R) TypeSpec Code Generator"` or `"Code generated by Microsoft (R) AutoRest Code Generator"` are treated as generated. Only these are deleted during re-generation; hand-written files are preserved. |
| **`customization-class`** | An emitter option naming a fully-qualified Java class that applies customizations during generation. |
| **`partial-update`** | When `true`, generated code is merged with existing code rather than fully replacing it. |

### 2.2 Compilation Failure Due to Customization Incompatibility

**Error message:**
```
[COMPILE] Maven build fail.
```
*(with Java compilation errors referencing customization classes or methods that no longer exist in generated code)*

**Cause:** The TypeSpec spec changed (e.g., a model was renamed, a method signature changed), and the hand-written customization code references the old generated API surface.

**What the pipeline does automatically:**
- On failure, the pipeline **retries with customization disabled** (`customization-class=` empty, `partial-update=false`)
- If the retry succeeds, you'll see:
  ```
  Generate a fresh package from TypeSpec. If there was prior customization on the package,
  please check whether it causes failure, and fix them before apiview.
  ```

**Fix:**
1. Review the compilation errors in the pipeline log
2. Update your customization code to match the new generated API surface
3. Push the updated customization code and re-trigger the pipeline

> **Real-world examples:**
> - ["I am creating a new API version... pull request..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1748961827942?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1748961827942&parentMessageId=1748961827942) — new API version broke existing customization
> - ["Microsoft.Advisor: New 2025-05-01-preview API version..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1748476281517?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1748476281517&parentMessageId=1748476281517)
> - ["For my PR, Java SDK failed Pipelines..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1741850079812?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1741850079812&parentMessageId=1741850079812)

### 2.3 Swagger-to-TypeSpec Migration Conflict

**Warning message:**
```
Existing package in SDK was from Swagger. It cannot be automatically converted to package from TypeSpec.
```

**Cause:** The existing SDK package was originally generated from Swagger (has a `swagger/` directory), but the spec has migrated to TypeSpec.

**Fix:** This requires manual migration. Coordinate with the SDK team to:
1. Remove or archive the Swagger-based package
2. Generate a fresh package from the TypeSpec definition
3. Manually port any customization code to the new package structure

### 2.4 Customization Fallback Behavior

During **spec-PR-validation** or **batch generation** runs, the pipeline has a two-stage fallback:

1. **First attempt:** Generate normally with customization enabled
2. **If generation or compile fails:** Clean the SDK folder entirely (`shutil.rmtree`), then regenerate with `disable_customization=True`

If the second attempt succeeds, the PR will contain a fresh package **without customizations**. You must manually re-apply and fix your customization code before merging.

### 2.5 Missing or Merged Members After Spec Update

**Symptom:** Compilation errors referencing types, methods, or properties that no longer exist or have been renamed/merged in the generated code.

**Cause:** The API surface changed (e.g., two types were merged, a property was renamed, a method was removed), and your code references the old members.

**Fix:**
1. Check the spec PR diff to understand what changed
2. Update your call sites or custom types to match the new generated surface
3. Consult the API changelog for the specific breaking changes

> **Real-world examples:**
> - ["I have this pr where java sdk validation is failing — Unify C..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1761843942953?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1761843942953&parentMessageId=1761843942953)
> - ["I am blocked on a SDK generation error that's obscure..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1753990113135?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1753990113135&parentMessageId=1753990113135)
> - ["I'm preparing to merge this PR..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1753120660695?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1753120660695&parentMessageId=1753120660695)
> - ["I'm migrating my team's spec to Typespec and ran into the following generation..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1741302215643?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1741302215643&parentMessageId=1741302215643)

### 2.6 Duplicate or Ambiguous Operations

**Symptom:** Generation or compilation fails due to two operations resolving to the same method name, or a hand-written method conflicts with a generated one.

**Cause:** The spec defines operations that produce duplicate Java method signatures, or a customization class defines a method that clashes with a generated operation.

**Fix:**
- Rename the conflicting operation in the TypeSpec spec using `@clientName` directive
- Or adjust the customization code to avoid the name collision

> **Real-world examples:**
> - ["I'm from the Azure Key Vault team, and we noticed that there were two directories..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1754078538934?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1754078538934&parentMessageId=1754078538934)
> - ["I have an api specs PR where I'm adding a new service..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1749141203016?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1749141203016&parentMessageId=1749141203016)

### 2.7 Internal Generator Bugs

**Symptom:** The error stack mentions an internal generator class (like `RootMethod` or `ParameterHelpers`) and the message looks like an assertion or unexpected code path.

**Cause:** A bug in the Java TypeSpec emitter or AutoRest Java plugin.

**Fix:**
1. Clean out old caches and re-run
2. Update to the latest generator release
3. If the problem remains, report a bug to the [Java SDK team][teams-java] including the exact error and a minimal repro

> **Real-world examples:**
> - ["I am getting this error on trying to generate and infact I a..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1750421103502?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1750421103502&parentMessageId=1750421103502)
> - ["In Release sql microsoft.sql 2024 11 01 preview v..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1752727264176?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1752727264176&parentMessageId=1752727264176)

---

## 3. Intermittent Errors (Resolved by Rerun)

These errors are caused by transient network or infrastructure issues. They are **not** related to your spec or code and can typically be resolved by simply **re-running the pipeline**.

### 3.1 Maven Dependency Download Failure

**Symptoms:**
- `[COMPILE] Maven build fail.` with errors mentioning failed dependency resolution
- Maven logs showing `Could not resolve dependencies`, `Could not transfer artifact`, or connection timeout errors

**Cause:** Transient network issues when Maven attempts to download dependencies from Maven Central or other configured repositories.

**Fix:** Re-run the pipeline. The pipeline does not have built-in retry logic for Maven commands. If the issue persists across multiple runs, check:
- Whether the dependency actually exists in Maven Central
- Whether there's an ongoing Maven Central outage

### 3.2 Maven Central JAR Download Failure (Changelog Generation)

**Symptom:** The pipeline fails during changelog generation after a successful compile.

**Cause:** The pipeline downloads the previously published JAR from Maven Central (`https://repo1.maven.org/maven2/...`) to compare APIs for changelog generation. This HTTP request can fail due to network issues.

**Fix:** Re-run the pipeline. If this is a **brand-new package** with no prior published version, the download is expected to fail gracefully (HTTP 404), and the pipeline should continue. If it doesn't, report a pipeline bug.

### 3.3 npm / tsp-client Installation Failure

**Symptoms:**
- Failure during environment setup (`npm ci` fails)
- `tsp-client init` fails with npm registry errors

**Cause:** Transient npm registry connectivity issues during installation of `tsp-client` or TypeSpec compiler packages.

**Fix:** Re-run the pipeline. If the error persists, check:
- Whether the TypeSpec package versions in [`eng/emitter-package.json`](../eng/emitter-package.json) are published and available

### 3.4 Git Operations Failure

**Symptom:**
```
[GENERATE] Code generation failed. Finding sdk folder fails: {error}
```

**Cause:** The pipeline runs `git status --porcelain` or other git commands that can occasionally fail due to file system or environment issues.

**Fix:** Re-run the pipeline.

### 3.5 Component Detection / CI Infrastructure Failures

**Symptom:** Pipeline fails on injected tasks like "Component Detection" rather than on generation or compile steps.

**Cause:** Azure DevOps injected tasks can have transient failures unrelated to your code.

**Fix:** Re-run the pipeline.

> **Real-world example:**
> - ["That issue where the injected Component Detection was failing..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1744747819857?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1744747819857&parentMessageId=1744747819857)

### 3.6 General Guidance for Intermittent Failures

If you encounter a pipeline failure that doesn't match any of the specific error patterns above:

1. **Check the full pipeline log** for the root cause — look for the `[GENERATE]` or `[COMPILE]` prefixed messages
2. **Re-run the pipeline once** — many transient issues resolve on retry
3. **If the failure persists after 2-3 retries**, it is likely a genuine spec or code issue rather than an intermittent failure. Review the error details and consult the sections above
4. **Report persistent infrastructure issues** to the SDK engineering systems team

---

## 4. Escalation Guidance

If you have followed all the troubleshooting steps in this guide and the issue still remains, the problem is likely not related to pipeline usage or configuration. Escalate to the appropriate team:

1. **Java SDK / Generator issues** — Post in the [Language - Java Teams channel][teams-java] with:
   - The spec PR link
   - The pipeline build link and build ID
   - The exact error message from the log
   - What you've already tried
2. **TypeSpec compiler issues** — File an issue on [TypeSpec GitHub](https://github.com/microsoft/typespec)
3. **Pipeline infrastructure issues** — See [Troubleshoot PR build failures](https://eng.ms/docs/products/azure-developer-experience/support/troubleshoot/PR-failing)
4. **General SDK support** — See [Get help with Azure SDKs development](https://eng.ms/docs/products/azure-developer-experience/support/support#get-help-with-the-azure-sdks-development)

---

## Quick Reference: Error Message Index

| Error Message | Category | Section |
|---|---|---|
| `namespace is REQUIRED` | tspconfig | [1.1](#11-missing-namespace) |
| `namespace SHOULD start with "com.azure."` | tspconfig | [1.2](#12-invalid-namespace-format) |
| `No sdk folder found` | tspconfig | [1.4](#14-no-sdk-folder-found-after-generation) |
| `Invalid SDK release type` | tspconfig | [1.5](#15-sdk-release-type-validation-failures) |
| `Maven build fail` (with customization errors) | Customization | [2.2](#22-compilation-failure-due-to-customization-incompatibility) |
| `cannot be automatically converted to package from TypeSpec` | Customization | [2.3](#23-swagger-to-typespec-migration-conflict) |
| `Maven build fail` (with dependency download errors) | Intermittent | [3.1](#31-maven-dependency-download-failure) |
| `Cannot found built jar` | Intermittent | [3.2](#32-maven-central-jar-download-failure-changelog-generation) |
| `tsp-client init fails` (npm errors) | Intermittent | [3.3](#33-npm--tsp-client-installation-failure) |
| `Finding sdk folder fails` | Intermittent | [3.4](#34-git-operations-failure) |
| Component Detection failure | Intermittent | [3.5](#35-component-detection--ci-infrastructure-failures) |
| Deprecated TypeSpec decorator errors | tspconfig | [1.6](#16-typespec-version--deprecated-decorator-errors) |
| `emitter-output-dir` misconfiguration | tspconfig | [1.7](#17-emitter-output-dir-configuration-change) |
| Missing/merged members after spec update | Customization | [2.5](#25-missing-or-merged-members-after-spec-update) |
| Duplicate operation names | Customization | [2.6](#26-duplicate-or-ambiguous-operations) |
| Internal generator assertion/bug | Customization | [2.7](#27-internal-generator-bugs) |

---

<!-- Reference Links -->
[teams-java]: https://teams.microsoft.com/l/channel/19%3a5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/Language%2520-%2520Java?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47 "Language - Java Teams channel"
