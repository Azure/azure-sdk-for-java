# Java SDK Generation Pipeline Troubleshooting Guide

This guide helps you **identify which category** a pipeline failure belongs to and **apply the solution**.

> Applies to: **Java SDK generation pipeline** (spec PR validation / spec PR generation)

- [Quick Triage](#quick-triage)
- [tspconfig.yaml Errors](#1-tspconfig-errors) — misconfigurations in the spec repo's `tspconfig.yaml`
- [Customization Errors](#2-customization-errors) — hand-written SDK code conflicts with regenerated code
- [Intermittent Errors](#3-intermittent-errors) — transient failures, usually resolved by rerun
- [Escalation](#escalation)

> Related: [SDK Validation FAQ](https://aka.ms/azsdk/sdk-automation-faq) | [SDK Generation Pipelines](https://eng.ms/docs/products/azure-developer-experience/develop/sdk-generation-pipelines) | [SDK Release Pipeline](https://eng.ms/docs/products/azure-developer-experience/develop/sdk-release/sdk-release-pipeline) | [Language - Java Teams channel][teams-java]

---

## Quick Triage

Find the most specific signal in the failure log and jump directly:

| Log Signal | Category | Jump |
|---|---|---|
| `[COMPILE] Maven build fail.` + Checkstyle `PackageName` `must match pattern` | tspconfig | [1.1 Namespace Segment Too Long](#11-namespace-segment-too-long) |
| `not supported by Fluent Premium` | tspconfig | [1.2 Unsupported Emitter Option (Fluent Premium)](#12-unsupported-emitter-option-fluent-premium) |
| Verify Swagger and TypeSpec Code Generation check fails | tspconfig | [1.3 Verify Swagger and TypeSpec Code Generation fails](#13-verify-swagger-and-typespec-code-generation-fails) |
| `[COMPILE] Maven build fail.` + customization class/method referenced | customization | [2. Customization Errors](#2-customization-errors) |
| `Could not resolve dependencies` / `Could not transfer artifact` | intermittent | [3.1 Maven Dependency Download Failure](#31-maven-dependency-download-failure) |
| None match | unknown | [Escalation](#escalation) |

---

## 1. tspconfig Errors

**How to identify:** The error message contains `[VALIDATE][tspconfig.yaml]` or `[GENERATE] ...`, or generation output lands in the wrong directory.

**References:**
- TypeSpec configuration (tspconfig): https://typespec.io/docs/handbook/configuration/configuration/
- Azure TypeSpec Autorest emitter reference: https://azure.github.io/typespec-azure/docs/emitters/typespec-autorest/reference/emitter/
- Azure TypeSpec Java emitter reference: https://azure.github.io/typespec-azure/docs/emitters/clients/typespec-java/reference/emitter/

### 1.1 Namespace Segment Too Long

**Log signal:**
- `[COMPILE] Maven build fail.`
- Checkstyle `PackageName` error with `must match pattern`

**Error (real-world example):**
```
[ERROR] Name 'com.azure.resourcemanager.<verylongsegment>...' must match pattern
'^(?=.{9,80}$)((com.microsoft|com.azure)(\.[a-z][a-z0-9]{1,31})*)+$'. [PackageName]
[ERROR] Failed to execute goal ... maven-checkstyle-plugin ... There are N errors reported by Checkstyle
```

**Root cause:** The namespace does not match the repository's Checkstyle `PackageName` rule.

**Solution:**

1. **Check whether the namespace is approved.** If the long namespace has been reviewed and approved (e.g. it matches the service name exactly):
   - Add a Checkstyle suppression to [`eng/lintingconfigs/checkstyle/track2/checkstyle-suppressions.xml`](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/eng/lintingconfigs/checkstyle/track2/checkstyle-suppressions.xml#L109-L110). For example:
     ```xml
     <!-- Suppress the long package name in yourservice -->
     <suppress checks="PackageName" files="com.azure.resourcemanager.yourverylongservicename.*" />
     ```
2. **If the namespace is not approved**, rename or shorten the namespace until it matches the `PackageName` rule shown in the error log. In practice, check the generated package name against the regex and reduce any part of the namespace that causes the mismatch, such as an overly long segment or an overall package name that is too long.

### 1.2 Unsupported Emitter Option (Fluent Premium)

**Log signal:**
- `not supported by Fluent Premium`

**Error (real-world example):**
```
error @azure-tools/typespec-java/generator-error: ... - Unhandled error.
java.lang.IllegalStateException: Package 'com.azure.resourcemanager.<pkg>' is not supported by Fluent Premium
```

**Root cause:** `tspconfig.yaml` enables an emitter option (for example `premium: true`) that is not supported for the target package.

**Solution:** Remove the unsupported option from `options.@azure-tools/typespec-java`.

---

### 1.3 Verify Swagger and TypeSpec Code Generation fails

**Log signal:**
- Verify Swagger and TypeSpec Code Generation check fails in Java SDK CI - java-pullrequest

**Error (real-world example):**
```
Exception: /mnt/vss/_work/1/s/eng/scripts/Compare-CurrentToCodegeneration.ps1:225
Line |
 225 |  $job | Receive-Job 2>$null | Out-Null
     |  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     | ScriptHalted

##[error]PowerShell exited with code '1'.
```

**Root cause:**
The **Verify Swagger and TypeSpec Code Generation** step in the Java SDK PR pipeline re-runs code generation using repository-local configuration and compares regenerated output with committed code. This check usually fails because regeneration produces large diffs.
Common causes include:
- `tspconfig.yaml` has incorrect Java emitter settings.
- In `tspconfig.yaml`, `api-version` changes can directly change operation/model counts.
- In `tspconfig.yaml`, `enable-sync-stack` changes can add or remove a large set of sync client classes.
- In `tspconfig.yaml`, `flavor` changes (for example, `azure` vs `standard`) change generated code style and shape.

When operation counts change significantly, **api-version drift** is the most common cause. If `api-version` is not explicitly fixed in `tspconfig.yaml`, it may default to the latest version, which can differ from the version used when the checked-in SDK was originally generated.

**Solution:**
1. Check the pipeline code-generation diff, re-run local generation with the same repository configuration, compare against the SDK output in the generated PR, and update the relevant `tspconfig.yaml` settings (for example `api-version`, `enable-sync-stack`, `flavor`).
2. Update `tspconfig.yaml` in the spec repo PR, commit the change, and rerun generation to regenerate the SDK PR.

Example (API version mismatch): pin `api-version` in `tspconfig.yaml` so the Verify step and SDK generation use the same version, then rerun generation.
```yaml
options:
  "@azure-tools/typespec-java":
    api-version: "2025-12-01"   # ✅ pin the api-version used for generation
```

---

## 2. Customization Errors

**How to identify:** `[COMPILE] Maven build fail.` with Java compilation errors referencing hand-written classes/methods that no longer exist in generated code. The pipeline may auto-retry with customization disabled and show:
```
Generate a fresh package from TypeSpec. If there was prior customization on the package,
please check whether it causes failure, and fix them before apiview.
```

> **Background:** Azure SDK packages usually contain both *generated code* (auto-produced from TypeSpec) and *customization code* (hand-written by SDK developers):
> - *generated code*: produced by the generator; API surface may change when the spec or generator changes, which can break compilation.
> - *customization code*: maintained by SDK developers; commonly wired via `customization-class` and preserved during regeneration with `partial-update: true`.
>
> Reference: autorest.java customization-base: https://github.com/Azure/autorest.java/tree/main/customization-base

**Prerequisites for customization:** When adding customization code, two options must be set in `tspconfig.yaml`:
```yaml
options:
  "@azure-tools/typespec-java":
    customization-class: "com.azure.resourcemanager.yourservice.customization"
    partial-update: true
```
- **`customization-class`** — the Java package containing your hand-written customization classes
- **`partial-update`** — set to `true` so the generator preserves your custom files instead of overwriting the entire output directory

---

## 3. Intermittent Errors

**How to identify:** The failure is in dependency download, npm install, or CI infrastructure — not in generation/compilation logic. Error messages mention network timeouts, `Could not resolve dependencies`, `Could not transfer artifact`, or failures in injected DevOps tasks.

**Solution for all intermittent errors: Re-run the pipeline.**

> **If failure persists after 2-3 retries**, it is likely a genuine issue, not intermittent. Review the error details and consult sections 1 or 2 above.

### 3.1 Maven Dependency Download Failure

**Error:** `[COMPILE] Maven build fail.` with `Could not resolve dependencies` / `Could not transfer artifact` / connection timeout.

**Error (real-world example):**
```
[FATAL] Non-resolvable parent POM for com.azure:customization-loader:1.0.0-beta.1:
Could not transfer artifact ... from/to central (https://repo.maven.apache.org/maven2):
Connect to repo.maven.apache.org:443 ... failed: Network is unreachable (connect failed)
and 'parent.relativePath' points at wrong local POM
```

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
