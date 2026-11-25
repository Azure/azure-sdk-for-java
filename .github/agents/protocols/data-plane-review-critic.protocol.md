# Data-Plane Reviewer and Critic Protocol

This file is the canonical wire contract between the Data-Plane Reviewer and
the Data-Plane Review Critic.

## Required inputs

| Input | Form |
| --- | --- |
| PR | `owner/repo#number` |
| Session SHA | Full 40-character PR head SHA |
| Package | Maven artifact ID |
| Change class | `new-module`, `new-version`, or `maintenance` |
| Prior workflow comment | Latest marker comment or `none` |
| Findings | Verbatim candidate report under `## Findings` |
| Required response | Populated verdict-table skeleton |

Missing or malformed PR, session SHA, findings, or response skeleton produces one
`REJECT / missing-inputs` verdict.

## Dispatch template

```markdown
You are the Data-Plane Review Critic. Verify only the supplied findings.

PR: Azure/azure-sdk-for-java#<number>
Session SHA: <sha>
Package: <artifact-id>
Change class: <new-module|new-version|maintenance>
Prior workflow comment: <comment|none>

## Findings

<verbatim candidate report>

## Required response

Copy the block below exactly without code fences or this heading. Change only
each `<verdict>`, downgrade target, reason, and the summary counts. Keep target
and reason as `--` for `PASS`. For `DOWNGRADE`, use an exact lower target and
`over-escalated`. For `REJECT`, keep target as `--` and use one exact reason
code. The first output line must remain `## Data-Plane Review Critique`.

## Data-Plane Review Critique

**Session SHA:** <sha>
**Findings evaluated:** <n>

| # | Finding | State | Verdict | Target | Reason |
| --- | --- | --- | --- | --- | --- |
| 1 | [<actual-rule-id>] `<actual-path:line>` | <new|carried-forward> | <verdict> | -- | -- |

**Summary:** <n> PASS, <n> DOWNGRADE, <n> REJECT
```

The reviewer adds one pre-populated row per candidate. The critic must not
change row numbers, rule IDs, citations, states, headings, metadata, or
columns.

## Verdicts

| Verdict | Meaning | Reviewer action |
| --- | --- | --- |
| `PASS` | Independently verified | Keep; target and reason remain `--` |
| `DOWNGRADE` | Real but overstated | Apply the explicit lower target |
| `REJECT` | Unverified, duplicate, out of scope, or incorrect | Drop; target remains `--` |

## Reason codes

`missing-inputs`, `citation-mismatch`, `unreachable`, `not-in-diff`,
`unknown-rule-id`, `exception-applies`, `ci-owned`,
`duplicate`, `over-escalated`, `weak-evidence`, `no-correct-form`,
`harmful-fix`, and `out-of-scope`.

`PASS` uses target and reason `--`. `DOWNGRADE` uses target `Warning`,
`Suggestion`, or `Question` and reason `over-escalated`. `REJECT` uses target
`--` and exactly one reason code from this list. Free-text reasons are invalid.

## Severity ceilings

| Change class | Maximum |
| --- | --- |
| `new-module` | Blocking where the rule permits it |
| `new-version` | Blocking where the rule permits it |
| `maintenance` | Warning, except stable API breaks and credential exposure |

Naming and documentation findings are capped at Suggestion. A rule based only
on a `YOU SHOULD` or `YOU SHOULD NOT` statement cannot be Blocking.

## Failure handling

The output is malformed unless it has the exact heading and metadata fields,
one row per supplied finding, the declared columns, unchanged candidate states,
a valid target and reason combination, and a verdict spelled exactly `PASS`,
`DOWNGRADE`, or `REJECT`. Synonyms such as `Confirmed` are invalid. If dispatch
fails or the critique is malformed, the reviewer uses `noop`.
