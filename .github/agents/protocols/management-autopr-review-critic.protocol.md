# Management AutoPR Reviewer to Critic Protocol

The reviewer dispatches the critic once after self-verification.

## Required inputs

- PR: `Azure/azure-sdk-for-java#<number>`
- Session SHA: full 40-character PR head SHA
- Package and release type
- Prior workflow comment, or `none`
- Candidate concerns, including ID, severity, state, cited file, affected
  symbol or release entry, evidence, explanation, and requested action when
  Blocking or Warning

Missing PR, SHA, or candidate concerns produces one report-level
`FAIL / missing-inputs`.

## Verdicts

| Verdict | Meaning | Reviewer action |
| --- | --- | --- |
| `PASS` | Independently verified and properly calibrated | Keep |
| `DOWNGRADE` | Evidence supports verification, not an assertion | Convert to a concise Warning question |
| `FAIL` | Unsupported, duplicate, out of scope, or pre-existing | Drop |

## Reason codes

- `missing-inputs`
- `citation-mismatch`
- `not-in-diff`
- `out-of-scope`
- `rule-conditions-not-met`
- `known-exception`
- `duplicate`
- `already-resolved`
- `overstated`
- `no-action`

## Dispatch template

```markdown
You are the Management AutoPR Review Critic. Verify only these candidates.

PR: Azure/azure-sdk-for-java#<number>
Session SHA: <sha>
Package: <package>
Release type: <stable|beta>

## Prior workflow comment

<comment or none>

## Candidate concerns

<candidates including declared severity>
```

If the critic returns additional concerns, ignore them. If dispatch fails or
the response is malformed, the unattended reviewer emits no concern.
