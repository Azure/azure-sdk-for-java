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

Missing or malformed PR, session SHA, or findings produces one
`FAIL / missing-inputs` verdict.

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
```

## Verdicts

| Verdict | Meaning | Reviewer action |
| --- | --- | --- |
| `PASS` | Independently verified | Keep |
| `DOWNGRADE` | Real but overstated | Apply the critic's lower severity or question form |
| `FAIL` | Unverified, duplicate, out of scope, or incorrect | Drop |

## Reason codes

`missing-inputs`, `citation-mismatch`, `unreachable`, `not-in-diff`,
`unknown-rule-id`, `exception-applies`, `ci-owned`,
`duplicate`, `over-escalated`, `weak-evidence`, `no-correct-form`,
`harmful-fix`, and `out-of-scope`.

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
one row per supplied finding, the declared columns, a valid reason code, and a
verdict spelled exactly `PASS`, `DOWNGRADE`, or `FAIL`. Synonyms such as
`Confirmed` are invalid. If dispatch fails or the critique is malformed, the
reviewer uses `noop`.
