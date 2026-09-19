---
name: data-plane-review
description: Reviews Azure Java data-plane SDK pull requests using source-cited, high-confidence Java and general Azure SDK rules.
---

# Java Data-Plane Review

Use only for non-draft `Azure/azure-sdk-for-java` PRs targeting `main` that
change a Java data-plane client library under `sdk/`. Exclude management
libraries (`com.azure.resourcemanager` or `azure-resourcemanager-*`), Spring
libraries, examples-only repositories, and changes with no affected data-plane
package.

Review a PR only when it changes a package's public Java surface, implementation
behavior, POM, version metadata, CHANGELOG, README, samples, tests, generated
metadata, or generation configuration. On a synchronize event, review only the
pushed range. Supporting files may be read after the gate passes.

## Rule references

Read [`references/rule-summary.md`](references/rule-summary.md) first. Then load
only the references relevant to the changed files and constructs:

| Reference | Load when |
| --- | --- |
| [`client-api.md`](references/client-api.md) | Public clients, builders, methods, overloads, module exports, or implementation types change |
| [`operations.md`](references/operations.md) | Paging, LRO, async, validation, exceptions, pipeline, tracing, or context changes |
| [`models-security.md`](references/models-security.md) | Models, extensible values, credentials, authentication, or sensitive logging change |
| [`versioning-build-generation.md`](references/versioning-build-generation.md) | Public compatibility, API/package versions, POMs, module setup, or generated code changes |
| [`documentation-testing.md`](references/documentation-testing.md) | README, samples, JavaDoc, or tests change |

The reference files are the canonical rule definitions. This skill defines
scope, orchestration, verification, and output only.

## Review passes

1. Pin the PR head SHA and classify the package as `new-module`,
   `new-version`, or `maintenance`.
2. Inventory the complete affected package at the pinned head, not only the
   changed-file list. Include the package root, `src/main`, `src/test` and
   `src/samples` outside nested `generated` segments, resources, POM, version
   metadata, CHANGELOG, README, generated metadata, and generation
   configuration when present. Use unchanged files to verify existing
   documentation, samples, and tests; findings still require changed evidence.
3. Build the changed public API picture before applying individual rules:
   clients, builders, operations, models, exceptions, package metadata, and
   documentation.
4. Load the applicable rule references from the table above.
5. Produce candidates only for changed evidence. Do not report legacy issues.
6. Do not duplicate the exact output of a deterministic check. A distinct
   design issue or repository-prescribed remediation may still be useful.
7. Self-verify every candidate, then dispatch the Data-Plane Review Critic
   using
   [`../../agents/protocols/data-plane-review-critic.protocol.md`](../../agents/protocols/data-plane-review-critic.protocol.md).
8. Apply `PASS`, `DOWNGRADE`, and `FAIL` verdicts without override.
9. Emit one report or the no-findings form. Do not post it.

## Severity and finding form

| Severity | Glyph | Meaning |
| --- | --- | --- |
| Blocking | 🔴 | High-confidence security, stable compatibility, or release defect that must be addressed before merge |
| Warning | 🟡 | High-confidence SDK design or packaging defect likely to cause customer pain |
| Suggestion | 💡 | Supported improvement or lower-risk consistency issue |
| Question | -- | Evidence is incomplete and author context can change the outcome |

Questions are not findings: use a plain bullet without a bracketed rule ID,
severity glyph, or prescribed fix.

Every finding uses:

````markdown
**[<RULE-ID>] Short title** -- `path/to/file:line`

> exact changed evidence

Concise reason with an authoritative source link.

**Correct form:**

```text
replacement API, shape, or exact repository command
```
````

Bracketed IDs are reserved for findings actually raised. Order sections
Blocking, Warning, Suggestion, Questions. Cap the report at 15 findings and
group repeated naming or documentation issues.

## Verification

Before dispatching the critic, confirm every candidate:

1. cites exact evidence at the pinned SHA;
2. was introduced by this PR;
3. uses a rule defined in a loaded reference and no higher severity;
4. satisfies every trigger and exception in the cited rule;
5. is not the exact output of an existing deterministic check;
6. states the correct form, not only what is wrong;
7. does not require a hand edit to generated output;
8. does not expose sensitive values.

## Report

```markdown
## Java Data-Plane SDK Review

_Automated review by Copilot. Reviewed `<package>` at `<short-sha>`. This is
advisory and does not replace APIView, CI, or human review._

No findings.
```

When findings exist, replace `No findings.` with the non-empty severity
sections. Do not add praise, a diff summary, or rules considered but not raised.
