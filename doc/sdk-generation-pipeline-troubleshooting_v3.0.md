# Java SDK Generation Pipeline Troubleshooting Guide

This guide helps you **identify which category** a pipeline failure belongs to and **apply the solution**.

1. [tspconfig.yaml Errors](#1-tspconfig-errors) — misconfigurations in the spec repo's `tspconfig.yaml`
2. [Customization Errors](#2-customization-errors) — hand-written SDK code conflicts with regenerated code
3. [Intermittent Errors](#3-intermittent-errors) — transient failures, resolved by rerun

> Related: [SDK Validation FAQ](https://aka.ms/azsdk/sdk-automation-faq) | [SDK Generation Pipelines](https://eng.ms/docs/products/azure-developer-experience/develop/sdk-generation-pipelines) | [SDK Release Pipeline](https://eng.ms/docs/products/azure-developer-experience/develop/sdk-release/sdk-release-pipeline) | [Language - Java Teams channel][teams-java]

---

## 1. tspconfig Errors

**How to identify:** The error message contains `[VALIDATE][tspconfig.yaml]` or `[GENERATE] Code generation failed. No sdk folder found.`, or generation output lands in the wrong directory.

### 1.1 Missing `namespace`

**Error:**
```
[VALIDATE][tspconfig.yaml] options.@azure-tools/typespec-java.namespace is REQUIRED for Java SDK
```

**Solution:** Add `namespace` to `tspconfig.yaml` in the spec repo:
```yaml
options:
  "@azure-tools/typespec-java":
    namespace: "com.azure.resourcemanager.yourservice"  # mgmt-plane
    # or
    namespace: "com.azure.yourservice"                  # data-plane
```

### 1.2 Invalid `namespace` Format

**Error:**
```
[VALIDATE][tspconfig.yaml] namespace SHOULD start with "com.azure."
```

**Solution:** Namespace must match `com\.azure(\.\w+)+`. Examples:
- `com.azure.resourcemanager.fabric` (mgmt)
- `com.azure.ai.contentsafety` (data-plane)

### 1.3 Mismatched `emitter-output-dir`

**Error:** Generation succeeds but output lands in wrong directory, or POM/CI integration fails.

**Solution:** Ensure `emitter-output-dir` matches the convention:
```yaml
emitter-output-dir: "{project-root}/sdk/{service}/{package-name}"
```

> **Real-world example:** ["EngSys plan to modify tspconfig.yaml, use emitter-output-dir option instead of..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1755227248942?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1755227248942&parentMessageId=1755227248942)

### 1.4 No SDK Folder Found

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

### 2.1 Customization Incompatible with New Generated Code

**Error:** Compilation errors referencing customization classes or methods that no longer exist after spec update.

**Solution:**
1. Review the compilation errors in the pipeline log
2. Update your customization code to match the new generated API surface
3. Push the fix and re-trigger the pipeline

> **Real-world examples:**
> - ["I am creating a new API version... pull request..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1748961827942?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1748961827942&parentMessageId=1748961827942)
> - ["Microsoft.Advisor: New 2025-05-01-preview API version..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1748476281517?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1748476281517&parentMessageId=1748476281517)
> - ["For my PR, Java SDK failed Pipelines..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1741850079812?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1741850079812&parentMessageId=1741850079812)

### 2.2 Missing or Merged Members After Spec Update

**Error:** Compilation errors referencing types, methods, or properties that were renamed/removed/merged.

**Solution:**
1. Check the spec PR diff to see what changed
2. Update your code to match the new generated surface

> **Real-world examples:**
> - ["I have this pr where java sdk validation is failing — Unify C..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1761843942953?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1761843942953&parentMessageId=1761843942953)
> - ["I am blocked on a SDK generation error that's obscure..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1753990113135?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1753990113135&parentMessageId=1753990113135)
> - ["I'm migrating my team's spec to Typespec and ran into the following generation..."](https://teams.microsoft.com/l/message/19%3A5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/1741302215643?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&createdTime=1741302215643&parentMessageId=1741302215643)

### 2.3 Duplicate or Ambiguous Operations

**Error:** Two operations resolve to the same Java method name, or a hand-written method conflicts with a generated one.

**Solution:** Rename the conflicting operation in the TypeSpec spec using `@clientName` directive, or adjust the customization code.

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
| `namespace is REQUIRED` | tspconfig | Add `namespace` to tspconfig.yaml ([1.1](#11-missing-namespace)) |
| `namespace SHOULD start with "com.azure."` | tspconfig | Fix namespace format ([1.2](#12-invalid-namespace-format)) |
| Output in wrong directory | tspconfig | Fix `emitter-output-dir` ([1.3](#13-mismatched-emitter-output-dir)) |
| `No sdk folder found` | tspconfig | Add valid Java emitter config ([1.4](#14-no-sdk-folder-found)) |
| `Maven build fail` + customization errors | Customization | Update customization code ([2.1](#21-customization-incompatible-with-new-generated-code)) |
| Missing types/methods after spec change | Customization | Match code to new API surface ([2.2](#22-missing-or-merged-members-after-spec-update)) |
| Duplicate method names | Customization | Rename via `@clientName` ([2.3](#23-duplicate-or-ambiguous-operations)) |
| `Could not resolve dependencies` | Intermittent | Re-run pipeline ([3.1](#31-maven-dependency-download-failure)) |
| `Cannot found built jar` | Intermittent | Re-run pipeline ([3.2](#32-maven-central-jar-download-failure-changelog)) |
| npm / tsp-client errors | Intermittent | Re-run pipeline ([3.3](#33-npm--tsp-client-installation-failure)) |
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
