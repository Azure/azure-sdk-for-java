# Verification Checklist for Troubleshooting Guide

Items to collect from the [azure-rest-api-specs](https://github.com/Azure/azure-rest-api-specs) repo and pipeline logs to make the troubleshooting guide more concrete.

---

## 1. Spec Repo: tspconfig.yaml Real Examples

Need real PR examples showing each tspconfig error pattern:

- [ ] **PR where `namespace` was missing** — a PR that failed Java SDK validation because `@azure-tools/typespec-java` had no `namespace`
- [ ] **PR where `namespace` format was wrong** — e.g., used `azure.resourcemanager.xxx` instead of `com.azure.resourcemanager.xxx`
- [ ] **PR where `emitter-output-dir` was mismatched** — output went to wrong sdk folder
- [ ] **PR where Java emitter section was completely absent** — resulted in `No sdk folder found`

Reference for correct format:
- ARM tspconfig sample: https://aka.ms/azsdk/tspconfig-sample-mpg
- Data-plane tspconfig sample: https://aka.ms/azsdk/tspconfig-sample-dpg

---

## 2. Spec Repo: Customization-Related PRs

Need real PR examples showing customization breakage:

- [ ] **PR where spec change broke existing customization** — e.g., model renamed, method removed, causing `[COMPILE] Maven build fail.` with the auto-retry message
- [ ] **PR where types were merged/renamed** — compilation error referencing old type names
- [ ] **PR where duplicate operations caused method name collision**
- [ ] **PR showing Swagger-to-TypeSpec migration conflict** — existing package had `swagger/` dir

For each, need:
- Spec PR link (in azure-rest-api-specs)
- Corresponding SDK validation pipeline build link/ID
- The specific compilation error from the log

---

## 3. Pipeline Log Prefixes

Need actual pipeline log snippets for each error category. Key prefixes to search for in pipeline logs:

### Generation stage prefixes
- `[GENERATE]` — code generation step
- `[VALIDATE]` — tspconfig/parameter validation
- `[COMPILE]` — Maven build step
- `[POM]` — POM.xml integration
- `[CI]` — ci.yml integration
- `[VERSION]` — version_client.txt update

### Specific error strings to capture from real logs
- [ ] `[VALIDATE][tspconfig.yaml] options.@azure-tools/typespec-java.namespace is REQUIRED for Java SDK`
- [ ] `[VALIDATE][tspconfig.yaml] namespace SHOULD start with "com.azure."`
- [ ] `[GENERATE] Code generation failed. No sdk folder found.`
- [ ] `[GENERATE] Code generation failed. tsp-client init fails: {actual error}`
- [ ] `[COMPILE] Maven build fail.` — with customization-related Java compilation errors
- [ ] `[COMPILE] Maven build fail.` — with `Could not resolve dependencies` (intermittent)
- [ ] `Generate a fresh package from TypeSpec. If there was prior customization on the package...`
- [ ] `Existing package in SDK was from Swagger. It cannot be automatically converted to package from TypeSpec.`
- [ ] `Cannot found built jar in {path}`
- [ ] `[GENERATE] Code generation failed. Finding sdk folder fails: {actual error}`

---

## 4. Pipeline Build IDs

Need example pipeline build IDs for each failure type (from [SDK Validation pipeline](https://dev.azure.com/azure-sdk/public/_build)):

- [ ] Build ID: tspconfig validation failure
- [ ] Build ID: customization compile failure (with auto-retry)
- [ ] Build ID: intermittent Maven download failure
- [ ] Build ID: intermittent npm/tsp-client failure
- [ ] Build ID: Component Detection failure

---

## 5. Spec Repo CI Checks to Document

The spec PR triggers these SDK-related checks (from `.github/workflows/`):

| Check | What it does | Relevant to TSG? |
|---|---|---|
| `typespec-validation` | Validates TypeSpec specs via `TypeSpec-Validation.ps1` | Yes — tspconfig errors surface here |
| `spec-gen-sdk-status` | Aggregates SDK validation results into GitHub check | Yes — this is where Java validation status appears |
| `breaking-change` | Detects breaking changes in API | Maybe — can explain customization breakage |
| `lintdiff` | API style compliance | No |
| `avocado` | Directory structure validation | No |
| `SDK-Suppressions-Label` | SDK suppression management | Maybe — if suppression hides real error |

- [ ] Confirm which check name shows Java SDK validation results on spec PRs
- [ ] Get screenshot or example of what a failed Java SDK check looks like on a spec PR

---

## 6. Spec Repo Documentation Links to Reference

These docs from the spec repo should be linked in the troubleshooting guide:

- [ ] `documentation/ci-fix.md` — CI fix guide (mentions Java SDK check owner: weidongxu-microsoft)
- [ ] `documentation/typespec-rest-api-dev-process.md` — full TypeSpec development workflow
- [ ] `documentation/directory-structure.md` — correct directory layout
- [ ] PR templates:
  - `.github/PULL_REQUEST_TEMPLATE/control_plane_template.md` (ARM)
  - `.github/PULL_REQUEST_TEMPLATE/data_plane_template.md` (data-plane)
  - `.github/PULL_REQUEST_TEMPLATE/sdk_configuration_template.md` (SDK config only)

---

## 7. Decision Tree Data Needed

To build a quick-triage flowchart, confirm these decision points:

- [ ] **Q: Did the error occur before or after `[COMPILE]`?**
  - Before → likely tspconfig (section 1)
  - After → likely customization (section 2) or intermittent (section 3)
- [ ] **Q: Does the same error occur on re-run?**
  - No → intermittent (section 3)
  - Yes → tspconfig or customization
- [ ] **Q: Does the error mention `tspconfig.yaml` or `[VALIDATE]`?**
  - Yes → tspconfig (section 1)
  - No → customization (section 2)

---

## 8. Spec Repo Edits to Reproduce Each Error (Verification PRs)

> **Spec repo location:** `~/lib/Github/Azure/azure-rest-api-specs`
>
> **Workflow:** For each test, create a branch from `main`, make the edit, push, and open a PR to `azure-rest-api-specs`. The SDK validation pipeline will run automatically and produce the expected error.
>
> **Recommended test service:** Use **`specification/fabric/resource-manager/Microsoft.Fabric/Fabric/`** (ARM) or **`specification/contosowidgetmanager/Contoso.WidgetManager/`** (data-plane) — both are well-known TypeSpec projects with stable Java emitter configs.

### 8.1 Reproduce tspconfig Errors (Category 1)

#### 8.1.1 Missing `namespace` → expect `[VALIDATE]` error

**File:** `specification/fabric/resource-manager/Microsoft.Fabric/Fabric/tspconfig.yaml`

**Edit:** Remove the `namespace` line from the `@azure-tools/typespec-java` section:

```yaml
# BEFORE (correct)
  "@azure-tools/typespec-java":
    emitter-output-dir: "{output-dir}/{service-dir}/azure-resourcemanager-fabric"
    flavor: azure
    namespace: "com.azure.resourcemanager.fabric"    # ← DELETE this line

# AFTER (broken — triggers error 1.1)
  "@azure-tools/typespec-java":
    emitter-output-dir: "{output-dir}/{service-dir}/azure-resourcemanager-fabric"
    flavor: azure
```

**Expected error:**
```
[VALIDATE][tspconfig.yaml] options.@azure-tools/typespec-java.namespace is REQUIRED for Java SDK
```

#### 8.1.2 Invalid `namespace` format → expect `[VALIDATE]` error

**File:** `specification/fabric/resource-manager/Microsoft.Fabric/Fabric/tspconfig.yaml`

**Edit:** Change `namespace` to an invalid format (missing `com.azure.` prefix):

```yaml
# BEFORE
    namespace: "com.azure.resourcemanager.fabric"

# AFTER (broken — triggers error 1.2)
    namespace: "azure.resourcemanager.fabric"
```

**Expected error:**
```
[VALIDATE][tspconfig.yaml] namespace SHOULD start with "com.azure."
```

#### 8.1.3 Mismatched `emitter-output-dir` → output lands in wrong directory

**File:** `specification/fabric/resource-manager/Microsoft.Fabric/Fabric/tspconfig.yaml`

**Edit:** Change `emitter-output-dir` to a path that doesn't match the expected convention:

```yaml
# BEFORE
    emitter-output-dir: "{output-dir}/{service-dir}/azure-resourcemanager-fabric"

# AFTER (broken — triggers error 1.3; output goes to wrong folder)
    emitter-output-dir: "{output-dir}/{service-dir}/azure-resourcemanager-wrongname"
```

**Expected result:** Generation succeeds but output lands in wrong directory, POM/CI integration may fail or produce mismatched package.

#### 8.1.4 Java emitter section completely absent → `No sdk folder found`

**File:** `specification/fabric/resource-manager/Microsoft.Fabric/Fabric/tspconfig.yaml`

**Edit:** Delete the entire `@azure-tools/typespec-java` block (all 4 lines: the key + emitter-output-dir + flavor + namespace):

```yaml
# DELETE this entire block:
  "@azure-tools/typespec-java":
    emitter-output-dir: "{output-dir}/{service-dir}/azure-resourcemanager-fabric"
    flavor: azure
    namespace: "com.azure.resourcemanager.fabric"
```

**Expected error:**
```
[GENERATE] Code generation failed. No sdk folder found.
```

### 8.2 Reproduce Customization Errors (Category 2)

> These require a service that has an **existing Java SDK with customization code** in `azure-sdk-for-java`. Good candidates:
> - **`azure-ai-openai`** (69 custom files, spec at `specification/cognitiveservices/OpenAI.Inference/`)
> - **`azure-compute-batch`** (31 custom files, spec at `specification/batch/`)
>
> The goal is to change the spec so that the regenerated code **conflicts** with the existing hand-written customization.

#### 8.2.1 Rename a model → customization references old name → compile error

**File:** Pick a `.tsp` file with a model that is referenced by Java customization code.

**Example using `specification/cognitiveservices/OpenAI.Inference/`:**

1. Find a model used in customization. In the Java SDK, `azure-ai-openai` has custom files under `src/main/java/com/azure/ai/openai/` that reference generated model types.
2. In the spec `.tsp` file, rename a model (e.g., change a model name or a property name).
3. The Java customization code still references the old name → `[COMPILE] Maven build fail.`

**Concrete edit approach:**
```
# In any .tsp model file under specification/cognitiveservices/OpenAI.Inference/models/
# Find a model like:
model ChatCompletionsOptions {
  ...
}

# Rename it (this will break Java customization that references ChatCompletionsOptions):
model ChatCompletionRequestOptions {
  ...
}
```

**Expected error:**
```
[COMPILE] Maven build fail.
```
Followed by pipeline auto-retry message:
```
Generate a fresh package from TypeSpec. If there was prior customization on the package,
please check whether it causes failure, and fix them before apiview.
```

#### 8.2.2 Remove a property or method → compile error on missing member

**File:** A `.tsp` file under a service with Java customization.

**Edit:** Remove a property from a model that Java customization code references (e.g., a getter/setter used in custom wrapper classes).

**Expected error:** `[COMPILE] Maven build fail.` with Java compilation error like:
```
error: cannot find symbol
  symbol:   method getPropertyName()
```

#### 8.2.3 Duplicate operations → method name collision

**File:** A `.tsp` file defining operations.

**Edit:** Add a new operation that resolves to the same Java method name as an existing one:

```typespec
# Example: add a second "get" operation with a name the Java emitter maps to the same method
@get
op getWidget(...): Widget;

@get
@route("widgets/{name}/details")
op getWidget(...): WidgetDetails;   // ← same name, different route → collision
```

**Expected error:** `[COMPILE] Maven build fail.` — duplicate method in generated code.

### 8.3 Intermittent Errors (Category 3)

> **These cannot be reliably reproduced by editing spec files.** They are transient infrastructure failures (Maven download timeout, npm registry error, etc.). The strategy for verifying these in the TSG is:
>
> 1. **Search existing pipeline history** for builds that failed with intermittent errors and succeeded on re-run
> 2. **Capture log snippets** from those builds
> 3. **Do NOT try to deliberately break Maven/npm in the spec repo** — it won't work
>
> Instead, verify the TSG for intermittent errors by:

- [ ] Find a build where `Could not resolve dependencies` appeared and a re-run succeeded
- [ ] Find a build where `Cannot found built jar` appeared (changelog JAR download)
- [ ] Find a build where npm/tsp-client install failed transiently
- [ ] Find a build where `Finding sdk folder fails` was a transient git issue
- [ ] Find a build where Component Detection failed

**Where to search:** [SDK Validation pipeline builds](https://dev.azure.com/azure-sdk/public/_build) — filter by `Result = Failed`, then look at re-runs of the same PR that succeeded.

---

## 9. Step-by-Step: Creating a Verification PR

```bash
# 1. Go to the spec repo
cd ~/lib/Github/Azure/azure-rest-api-specs

# 2. Create a test branch
git checkout -b test/java-tsg-verify-1.1-missing-namespace

# 3. Make the edit (example: remove namespace from fabric tspconfig)
#    Edit: specification/fabric/resource-manager/Microsoft.Fabric/Fabric/tspconfig.yaml
#    Remove the "namespace" line from @azure-tools/typespec-java section

# 4. Commit and push
git add specification/fabric/resource-manager/Microsoft.Fabric/Fabric/tspconfig.yaml
git commit -m "test: verify TSG checklist item 1.1 - missing namespace"
git push origin test/java-tsg-verify-1.1-missing-namespace

# 5. Open PR on GitHub (target: main)
#    - Title: "[DO NOT MERGE] Test: Java SDK TSG verification - missing namespace"
#    - The SDK validation pipeline will run automatically
#    - Record the pipeline build ID and error output

# 6. After verification, close the PR without merging
```

**Repeat for each error type** using separate branches:
- `test/java-tsg-verify-1.1-missing-namespace`
- `test/java-tsg-verify-1.2-invalid-namespace`
- `test/java-tsg-verify-1.3-wrong-emitter-output-dir`
- `test/java-tsg-verify-1.4-no-java-section`
- `test/java-tsg-verify-2.1-rename-model` (use cognitiveservices/OpenAI.Inference)

---

## 10. Quick Reference: Files to Edit per Error Type

| TSG Section | Error Type | Spec File to Edit | What to Change |
|---|---|---|---|
| 1.1 | Missing namespace | `specification/fabric/.../Fabric/tspconfig.yaml` | Delete `namespace:` line from Java section |
| 1.2 | Invalid namespace | `specification/fabric/.../Fabric/tspconfig.yaml` | Change `com.azure.resourcemanager.fabric` → `azure.resourcemanager.fabric` |
| 1.3 | Wrong emitter-output-dir | `specification/fabric/.../Fabric/tspconfig.yaml` | Change output dir path to non-matching name |
| 1.4 | No Java section | `specification/fabric/.../Fabric/tspconfig.yaml` | Delete entire `@azure-tools/typespec-java` block |
| 2.1 | Model renamed | `specification/cognitiveservices/OpenAI.Inference/models/*.tsp` | Rename a model that Java customization references |
| 2.2 | Property removed | `specification/cognitiveservices/OpenAI.Inference/models/*.tsp` | Remove a property used by Java custom code |
| 2.3 | Duplicate operation | Any service `.tsp` operations file | Add operation with same Java method name |
| 3.x | Intermittent | N/A — cannot reproduce by spec edit | Search pipeline history for transient failures |

---

## Summary: What to Provide Next

Priority items to collect:

1. **3-5 real spec PR links** (one per error type) with their pipeline build IDs
2. **Actual log snippets** from failed pipeline runs (copy-paste the relevant `[GENERATE]`/`[COMPILE]` lines)
3. **The exact GitHub check name** that shows Java SDK validation on spec PRs
