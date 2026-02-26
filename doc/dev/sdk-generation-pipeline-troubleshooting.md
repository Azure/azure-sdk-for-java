# Java SDK Generation Pipeline Troubleshooting Guide (v5.0)

This guide helps you **identify which category** a pipeline failure belongs to and **apply the solution**.

> Applies to: **Java SDK generation pipeline** (spec PR validation / spec PR generation)

1. [Quick Triage](#quick-triage)
2. [tspconfig.yaml Errors](#1-tspconfig-errors) — misconfigurations in the spec repo's `tspconfig.yaml`
3. [Customization Errors](#2-customization-errors) — hand-written SDK code conflicts with regenerated code
4. [Intermittent Errors](#3-intermittent-errors) — transient failures, usually resolved by rerun
5. [Escalation](#escalation)

> Related: [SDK Validation FAQ](https://aka.ms/azsdk/sdk-automation-faq) | [SDK Generation Pipelines](https://eng.ms/docs/products/azure-developer-experience/develop/sdk-generation-pipelines) | [SDK Release Pipeline](https://eng.ms/docs/products/azure-developer-experience/develop/sdk-release/sdk-release-pipeline) | [Language - Java Teams channel][teams-java]

---

## Verification Status

- ✅ **Verified** — confirmed by real pipeline cases
- ⚠️ **Unverified** — inferred from docs/logs; not yet validated end-to-end
- ⚠️ **Unable to Verify** — intermittent/infra; not reliably reproducible on demand

---

## Quick Triage

Find the most specific signal in the failure log and jump directly:

| Log Signal | Category | Jump |
|---|---|---|
| `[VALIDATE][tspconfig.yaml]` | tspconfig | [1. tspconfig Errors](#1-tspconfig-errors) |
| `namespace is REQUIRED` | tspconfig | [1.1 Missing `namespace`](#11-missing-namespace) |
| `namespace SHOULD start with "com.azure."` | tspconfig | [1.2 Invalid `namespace` Format](#12-invalid-namespace-format) |
| `PackageName` + `must match pattern` | tspconfig | [1.4 Namespace Segment Too Long](#14-namespace-segment-too-long) |
| `Google Java Formatter encountered errors` + `<identifier> expected` | tspconfig | [1.3 Invalid Namespace Characters](#13-invalid-namespace-characters) |
| `not supported by Fluent Premium` | tspconfig | [1.5 Unsupported Emitter Option (Fluent Premium)](#15-unsupported-emitter-option-fluent-premium) |
| `[GENERATE] Code generation failed. No sdk folder found.` | tspconfig | [1.7 No SDK Folder Found](#17-no-sdk-folder-found) |
| `[COMPILE] Maven build fail.` + customization class/method referenced | customization | [2. Customization Errors](#2-customization-errors) |
| `Generate a fresh package from TypeSpec` | customization fallback | [2.4 Pipeline Customization Fallback](#24-pipeline-customization-fallback) |
| `Could not resolve dependencies` / `Could not transfer artifact` | intermittent | [3.1 Maven Dependency Download Failure](#31-maven-dependency-download-failure) |
| `npm ci` fails / `tsp-client init` fails | intermittent | [3.3 npm / tsp-client Installation Failure](#33-npm--tsp-client-installation-failure) |
| `Component Detection` task fails | intermittent | [3.5 Component Detection / CI Infrastructure Failure](#35-component-detection--ci-infrastructure-failure) |
| None match | unknown | [Escalation](#escalation) |

---

## 1. tspconfig Errors

**How to identify:** The error message contains `[VALIDATE][tspconfig.yaml]` or `[GENERATE] ...`, or generation output lands in the wrong directory.

### 1.1 Missing `namespace`

**Status:** ✅ Verified

**Error:**
```
[VALIDATE][tspconfig.yaml] options.@azure-tools/typespec-java.namespace is REQUIRED for Java SDK
```

**Error (real-world example):**
```
[ERROR] Could not find the selected project in the reactor: com.azure.resourcemanager:azure-resourcemanager-<package>
[ERROR] [COMPILE] Maven build fail.
```

**Solution:** Add `namespace` to `tspconfig.yaml` in the spec repo:
```yaml
options:
  "@azure-tools/typespec-java":
    namespace: "com.azure.resourcemanager.yourservice"  # mgmt-plane
    # or
    namespace: "com.azure.yourservice"                  # data-plane
```

**Notes:** If validate does not block it, TypeSpec generation may still succeed, but Maven may later fail with:
```
Could not find the selected project in the reactor
```

### 1.2 Invalid `namespace` Format

**Status:** ⚠️ Unverified

**Error:**
```
[VALIDATE][tspconfig.yaml] namespace SHOULD start with "com.azure."
```

**Solution:** Namespace must match `com\.azure(\.\w+)+`. Examples:
- `com.azure.resourcemanager.fabric` (mgmt)
- `com.azure.ai.contentsafety` (data-plane)

### 1.3 Invalid Namespace Characters

**Status:** ✅ Verified

**Error (common pattern):**
- `Google Java Formatter encountered errors`
- `error: <identifier> expected`

**Error (real-world example):**
```
Google Java Formatter encountered errors:
error: <identifier> expected
... com.azure.resourcemanager:azure-resourcemanager-<pkg>.<Something> ...
```

**Root cause:** `namespace` contains illegal Java package characters (commonly `:` from Maven coordinates like `groupId:artifactId`).

**Solution:** Use a valid Java package name (letters/digits/underscore + `.` separators). Example:
```yaml
options:
  "@azure-tools/typespec-java":
    # ❌ wrong: "com.azure.resourcemanager:azure-resourcemanager-foo"
    namespace: "com.azure.resourcemanager.foo"  # ✅
```

### 1.4 Namespace Segment Too Long

**Status:** ✅ Verified

**Error (common pattern):**
- `[COMPILE] Maven build fail.`
- Checkstyle `PackageName` error with `must match pattern`

**Error (real-world example):**
```
[ERROR] Name 'com.azure.resourcemanager.<verylongsegment>...' must match pattern
'^(?=.{9,80}$)((com.microsoft|com.azure)(\.[a-z][a-z0-9]{1,31})*)+$'. [PackageName]
[ERROR] Failed to execute goal ... maven-checkstyle-plugin ... There are N errors reported by Checkstyle
```

**Root cause:** One `.`-separated segment in the namespace exceeds 32 characters (Checkstyle package rules).

**Solution:** Shorten the long segment so each segment is within 32 characters and keep the whole package name within typical limits.

### 1.5 Unsupported Emitter Option (Fluent Premium)

**Status:** ✅ Verified

**Error (common pattern):**
- `not supported by Fluent Premium`

**Error (real-world example):**
```
error @azure-tools/typespec-java/generator-error: ... - Unhandled error.
java.lang.IllegalStateException: Package 'com.azure.resourcemanager.<pkg>' is not supported by Fluent Premium
```

**Root cause:** `tspconfig.yaml` enables an emitter option (for example `premium: true`) that is not supported for the target package.

**Solution:** Remove the unsupported option from `options.@azure-tools/typespec-java`.

### 1.6 Mismatched `emitter-output-dir`

**Status:** ⚠️ Unverified

**Error:** Generation succeeds but output lands in wrong directory, or POM/CI integration fails.

**Solution:** Ensure `emitter-output-dir` matches the convention:
```yaml
emitter-output-dir: "{project-root}/sdk/{service}/{package-name}"
```

### 1.7 No SDK Folder Found

**Status:** ⚠️ Unverified

**Error:**
```
[GENERATE] Code generation failed. No sdk folder found.
```

**Solution:** Verify `tspconfig.yaml` has a valid `@azure-tools/typespec-java` section with correct `namespace` and `emitter-output-dir`.

---

## 2. Customization Errors

**How to identify:** `[COMPILE] Maven build fail.` with Java compilation errors referencing hand-written classes/methods that no longer exist in generated code. The pipeline may auto-retry with customization disabled and show:
```
Generate a fresh package from TypeSpec. If there was prior customization on the package,
please check whether it causes failure, and fix them before apiview.
```

> **Background:** Azure SDK packages contain both *generated code* (auto-produced from TypeSpec) and *customization code* (hand-written by SDK developers). See [SDK Code Structure](sdk-code-structure.md) for details.

**Prerequisites for customization:** When adding customization code, two options must be set in `tspconfig.yaml`:
```yaml
options:
  "@azure-tools/typespec-java":
    customization-class: "com.azure.resourcemanager.yourservice.customization"
    partial-update: true
```
- **`customization-class`** — the Java package containing your hand-written customization classes
- **`partial-update`** — set to `true` so the generator preserves your custom files instead of overwriting the entire output directory

### 2.1 Customization Incompatible with New Generated Code

**Status:** ⚠️ Unverified

**Error:** Compilation errors referencing customization classes or methods that no longer exist after spec update.

**Solution:**
1. Review the compilation errors in the pipeline log
2. Update your customization code to match the new generated API surface
3. Push the fix and re-trigger the pipeline

### 2.2 Missing or Merged Members After Spec Update

**Status:** ⚠️ Unverified

**Error:** Compilation errors referencing types, methods, or properties that were renamed/removed/merged.

**Solution:**
1. Check the spec PR diff to see what changed
2. Update your code to match the new generated surface

### 2.3 Duplicate or Ambiguous Operations

**Status:** ⚠️ Unverified

**Error:** Two operations resolve to the same Java method name, or a hand-written method conflicts with a generated one.

**Solution:** Rename the conflicting operation in the TypeSpec spec using `@clientName` directive, or adjust the customization code.

### 2.4 Pipeline Customization Fallback

**Status:** ⚠️ Unverified

**Symptom:** Pipeline succeeds but PR shows:
```
Generate a fresh package from TypeSpec...
```

**Root cause:** First compile (with customization) failed → pipeline regenerates with customization disabled → second run passes but **drops all customization**.

**Solution:** Fix the *first* customization compilation failure and rerun. Do not merge a fallback result if customization should be preserved.

---

## 3. Intermittent Errors

**How to identify:** The failure is in dependency download, npm install, or CI infrastructure — not in generation/compilation logic. Error messages mention network timeouts, `Could not resolve dependencies`, `Could not transfer artifact`, or failures in injected DevOps tasks.

**Solution for all intermittent errors: Re-run the pipeline.**

> **If failure persists after 2-3 retries**, it is likely a genuine issue, not intermittent. Review the error details and consult sections 1 or 2 above.

### 3.1 Maven Dependency Download Failure

**Status:** ✅ Verified

**Error:** `[COMPILE] Maven build fail.` with `Could not resolve dependencies` / `Could not transfer artifact` / connection timeout.

**Error (real-world example):**
```
[FATAL] Non-resolvable parent POM for com.azure:customization-loader:1.0.0-beta.1:
Could not transfer artifact ... from/to central (https://repo.maven.apache.org/maven2):
Connect to repo.maven.apache.org:443 ... failed: Network is unreachable (connect failed)
and 'parent.relativePath' points at wrong local POM
```

### 3.2 Maven Central JAR Download Failure (Changelog)

**Status:** ⚠️ Unable to Verify

**Error:** Pipeline fails during changelog generation after successful compile (downloading previous JAR from Maven Central).

### 3.3 npm / tsp-client Installation Failure

**Status:** ⚠️ Unable to Verify

**Error:** `npm ci` fails or `tsp-client init` fails with npm registry errors.

### 3.4 Git Operations Failure

**Status:** ⚠️ Unable to Verify

**Error:** `[GENERATE] Code generation failed. Finding sdk folder fails: {error}`

### 3.5 Component Detection / CI Infrastructure Failure

**Status:** ⚠️ Unable to Verify

**Error:** Pipeline fails on injected tasks like "Component Detection".

---

## Escalation

If the issue persists after following this guide, post in the [Language - Java Teams channel][teams-java] with:
- Spec PR link
- Pipeline build link and build ID
- Exact error message
- What you've already tried

Other resources: [TypeSpec GitHub](https://github.com/microsoft/typespec) | [Troubleshoot PR failures](https://eng.ms/docs/products/azure-developer-experience/support/troubleshoot/PR-failing) | [SDK support](https://eng.ms/docs/products/azure-developer-experience/support/support#get-help-with-the-azure-sdks-development)

<!-- Reference Links -->
[teams-java]: https://teams.microsoft.com/l/channel/19%3a5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/Language%2520-%2520Java?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47 "Language - Java Teams channel"
