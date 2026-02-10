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

## Summary: What to Provide Next

Priority items to collect:

1. **3-5 real spec PR links** (one per error type) with their pipeline build IDs
2. **Actual log snippets** from failed pipeline runs (copy-paste the relevant `[GENERATE]`/`[COMPILE]` lines)
3. **The exact GitHub check name** that shows Java SDK validation on spec PRs
