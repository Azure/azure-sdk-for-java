# Management AutoPR Reviewer Evaluations

Vally evaluations for the unattended Java management AutoPR reviewer.

The primary gate is false-positive resistance: expected generated POM churn,
additive APIs, legitimate service-folder naming, and already-raised concerns
must not become new findings. Positive fixtures cover each high-value rule.

Fixtures are synthetic PR snapshots, not production SDK code. They must not
contain instructions to the reviewer or labels revealing the expected result.
Expected behavior belongs in the eval rubric.

The workflow and eval defaults must use the same review model. Run the
`true-negatives` suite repeatedly before broadening scope or adding a rule.

Run from the repository root:

```powershell
.\.github\skills\evals\management-autopr-reviewer\run-evals.ps1 -Suite true-negatives
```

The runner expects a built sibling checkout at `..\vally`. Building Vally
requires npm authentication for its private Microsoft packages.
