# Java SDK Generation Pipeline Troubleshooting Guide

This guide covers common issues encountered during the Java SDK generation pipeline and how to resolve them. Issues are grouped into three categories:

1. [tspconfig.yaml Related Errors](#1-tspconfig-related-errors)
2. [Customization Related Errors](#2-customization-related-errors)
3. [Intermittent Errors (Resolved by Rerun)](#3-intermittent-errors-resolved-by-rerun)

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

### 3.5 General Guidance for Intermittent Failures

If you encounter a pipeline failure that doesn't match any of the specific error patterns above:

1. **Check the full pipeline log** for the root cause — look for the `[GENERATE]` or `[COMPILE]` prefixed messages
2. **Re-run the pipeline once** — many transient issues resolve on retry
3. **If the failure persists after 2-3 retries**, it is likely a genuine spec or code issue rather than an intermittent failure. Review the error details and consult the sections above
4. **Report persistent infrastructure issues** to the SDK engineering systems team

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
