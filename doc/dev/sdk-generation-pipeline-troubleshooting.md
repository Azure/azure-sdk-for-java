# Java SDK Generation Pipeline Troubleshooting Guide

This guide helps you **identify which category** a pipeline failure belongs to and **apply the solution**.

1. [tspconfig.yaml Errors](#1-tspconfig-errors) — misconfigurations in the spec repo's `tspconfig.yaml`
2. [Customization Errors](#2-customization-errors) — hand-written SDK code conflicts with regenerated code
3. [Intermittent Errors](#3-intermittent-errors) — transient failures, resolved by rerun

> Related: [SDK Validation FAQ](https://aka.ms/azsdk/sdk-automation-faq) | [SDK Generation Pipelines](https://eng.ms/docs/products/azure-developer-experience/develop/sdk-generation-pipelines) | [SDK Release Pipeline](https://eng.ms/docs/products/azure-developer-experience/develop/sdk-release/sdk-release-pipeline) | [Language - Java Teams channel][teams-java]

---

## 1. tspconfig Errors

**How to identify:** The error message contains `[COMPILE] Maven build fail.` with Maven reactor errors like `Could not find the selected project in the reactor` or `Project is duplicated in the reactor`. These errors occur when `tspconfig.yaml` has missing or invalid Java emitter configuration.

### 1.1 Missing or Invalid `namespace`

**Error:**
```
[ERROR] Could not find the selected project in the reactor: com.azure.resourcemanager:azure-resourcemanager-{service}
[ERROR] [COMPILE] Maven build fail.
```

This error occurs when:
- The `namespace` option is missing from `@azure-tools/typespec-java` section
- The `namespace` format is invalid (e.g., `azure.resourcemanager.xxx` instead of `com.azure.resourcemanager.xxx`)

**Solution:** Add or fix `namespace` in `tspconfig.yaml` in the spec repo:
```yaml
options:
  "@azure-tools/typespec-java":
    namespace: "com.azure.resourcemanager.yourservice"  # mgmt-plane
    # or
    namespace: "com.azure.yourservice"                  # data-plane
```

Namespace must match `com\.azure(\.\w+)+`. Examples:
- `com.azure.resourcemanager.fabric` (mgmt)
- `com.azure.ai.contentsafety` (data-plane)

> **Pipeline examples:**
> - [Missing namespace](https://dev.azure.com/azure-sdk/public/_build/results?buildId=5866955)
> - [Invalid namespace format](https://dev.azure.com/azure-sdk/public/_build/results?buildId=5866976)

### 1.2 Mismatched `emitter-output-dir`

**Error:**
```
[ERROR] Project 'com.azure.resourcemanager:azure-resourcemanager-{service}:{version}' is duplicated in the reactor
[ERROR] [COMPILE] Maven build fail.
```

This error occurs when `emitter-output-dir` points to a different folder than expected, causing the generated project to conflict with an existing one.

**Solution:** Ensure `emitter-output-dir` matches the convention and the package name:
```yaml
emitter-output-dir: "{project-root}/sdk/{service}/{package-name}"
```

> **Real-world example:** ["EngSys plan to modify tspconfig.yaml, use emitter-output-dir option instead of..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1755227248942?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1755227248942&parentMessageId=1755227248942)

### 1.3 No Java Emitter Section

**Behavior:** If the `@azure-tools/typespec-java` section is completely absent from `tspconfig.yaml`, the Java SDK generation is **skipped silently** — no error is produced, and no Java SDK is generated.

**Solution:** If you need Java SDK generation, add a valid `@azure-tools/typespec-java` section with `namespace` and `emitter-output-dir` to `tspconfig.yaml`.

---

## 2. Customization Errors

**How to identify:** Errors occur when existing customization code in the SDK repository is incompatible with regenerated code. These can manifest as:
- `[GENERATE] Code generation failed. tsp-client init fails` — customization class references a type that no longer exists
- `[COMPILE] Maven build fail.` — compilation errors in hand-written code referencing missing types/methods

The pipeline may auto-retry with customization disabled and show:
```
One reason of the compilation failure is that the existing code customization in SDK repository
being incompatible with the class generated from updated TypeSpec source.
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

Without `partial-update: true`, regeneration will overwrite the output directory and your customization files will be lost. Without `customization-class`, the generator won't compile or apply your custom code.

### 2.1 Customization Class References Missing Type

**Error:**
```
java.lang.IllegalArgumentException: {TypeName} does not exist in package {package}
Caused by: java.lang.RuntimeException: Unable to complete customization
[GENERATE] Code generation failed. tsp-client init fails
```

This occurs when a customization class (e.g., `OpenAICustomizations.java`) tries to modify a type that was renamed or removed in the spec update.

**Solution:**
1. Check the spec PR diff to see which types were renamed/removed
2. Update your customization class to reference the new type names
3. Push the fix and re-trigger the pipeline

> **Pipeline example:** [Build 5867018](https://dev.azure.com/azure-sdk/public/_build/results?buildId=5867018) — customization referenced `ChatCompletionsOptions` which was renamed

> **Real-world examples:**
> - ["I am creating a new API version... pull request..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1748961827942?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1748961827942&parentMessageId=1748961827942)
> - ["Microsoft.Advisor: New 2025-05-01-preview API version..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1748476281517?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1748476281517&parentMessageId=1748476281517)
> - ["For my PR, Java SDK failed Pipelines..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1741850079812?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1741850079812&parentMessageId=1741850079812)

### 2.2 Missing or Merged Members After Spec Update

**Error:**
```
Compilation failure: cannot find symbol
  symbol: variable {propertyName}
[COMPILE] Maven build fail.
```

This occurs when the spec removes or renames a property/method, but generated or customization code still references the old name.

**Solution:**
1. Check the spec PR diff to see what changed
2. Update your code to match the new generated surface

> **Pipeline example:** [Build 5867028](https://dev.azure.com/azure-sdk/public/_build/results?buildId=5867028) — property `stream` was removed from the model

> **Real-world examples:**
> - ["I have this pr where java sdk validation is failing — Unify C..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1761843942953?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1761843942953&parentMessageId=1761843942953)
> - ["I am blocked on a SDK generation error that's obscure..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1753990113135?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1753990113135&parentMessageId=1753990113135)
> - ["I'm migrating my team's spec to Typespec and ran into the following generation..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1741302215643?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1741302215643&parentMessageId=1741302215643)

### 2.3 Duplicate Operations in TypeSpec

**Error:**
```
error duplicate-symbol: Duplicate name: "{operationName}"
[GENERATE] Code generation failed. tsp-client init fails
```

This is a **TypeSpec compilation error** (not Java). It occurs when two operations in the TypeSpec spec resolve to the same name.

**Solution:** Rename one of the conflicting operations in the TypeSpec spec using `@clientName` directive:
```typespec
@clientName("getWidgetDetails")
op getWidget(@path id: string): WidgetDetails;
```

> **Pipeline example:** [Build 5866071](https://dev.azure.com/azure-sdk/public/_build/results?buildId=5866071) — duplicate `getChatCompletions` operation

> **Real-world examples:**
> - ["I'm from the Azure Key Vault team, and we noticed that there were two directories..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1754078538934?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1754078538934&parentMessageId=1754078538934)
> - ["I have an api specs PR where I'm adding a new service..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1749141203016?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1749141203016&parentMessageId=1749141203016)

---

## 3. Intermittent Errors

**How to identify:** The failure is in dependency download, npm install, or CI infrastructure — not in code generation or compilation logic. Error messages mention network timeouts, `Could not resolve dependencies`, `Could not transfer artifact`, or failures in injected DevOps tasks.

**Solution for all intermittent errors: Re-run the pipeline.**

### 3.1 Maven Dependency Download Failure

**Error:** `[COMPILE] Maven build fail.` with `Could not resolve dependencies` or connection timeout.

### 3.2 Maven Central JAR Download Failure (Changelog)

**Error:** Pipeline fails during changelog generation after successful compile (downloading previous JAR from Maven Central).

> **Real-world example:** ["Java SDK Maven Error: repo.maven.apache.org failed: Network is unreachable (connect failed) and 'parent.relativePath'"](https://teams.microsoft.com/l/message/19:5e673e41085f4a7eaaf20823b85b2b53@thread.skype/1748476281517?tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&parentMessageId=1748476281517&teamName=Azure%20SDK&channelName=Language%20-%20Java&createdTime=1748476281517)

### 3.3 npm / tsp-client Installation Failure

**Error:** `npm ci` fails or `tsp-client init` fails with npm registry errors.

### 3.4 Git Operations Failure

**Error:** `[GENERATE] Code generation failed. Finding sdk folder fails: {error}`

### 3.5 Component Detection / CI Infrastructure Failure

**Error:** Pipeline fails on injected tasks like "Component Detection".


> **If failure persists after 2-3 retries**, it is likely a genuine issue, not intermittent. Review the error details and consult sections 1 or 2 above.

---

## Quick Reference

| Error Pattern | Category | Solution |
|---|---|---|
| `Could not find the selected project in the reactor` | tspconfig | Add or fix `namespace` in tspconfig.yaml ([1.1](#11-missing-or-invalid-namespace)) |
| `Project is duplicated in the reactor` | tspconfig | Fix `emitter-output-dir` ([1.2](#12-mismatched-emitter-output-dir)) |
| Java SDK not generated (no error) | tspconfig | Add `@azure-tools/typespec-java` section ([1.3](#13-no-java-emitter-section)) |
| `{Type} does not exist in package` + `Unable to complete customization` | Customization | Update customization class ([2.1](#21-customization-class-references-missing-type)) |
| `cannot find symbol` + `Maven build fail` | Customization | Match code to new API surface ([2.2](#22-missing-or-merged-members-after-spec-update)) |
| `duplicate-symbol: Duplicate name` | Customization | Rename via `@clientName` ([2.3](#23-duplicate-operations-in-typespec)) |
| `Could not resolve dependencies` | Intermittent | Re-run pipeline ([3.1](#31-maven-dependency-download-failure)) |
| `Cannot found built jar` | Intermittent | Re-run pipeline ([3.2](#32-maven-central-jar-download-failure-changelog)) |
| npm / tsp-client registry errors | Intermittent | Re-run pipeline ([3.3](#33-npm--tsp-client-installation-failure)) |
| `Finding sdk folder fails` | Intermittent | Re-run pipeline ([3.4](#34-git-operations-failure)) |
| Component Detection failure | Intermittent | Re-run pipeline ([3.5](#35-component-detection--ci-infrastructure-failure)) |

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
