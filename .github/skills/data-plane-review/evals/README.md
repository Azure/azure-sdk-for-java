# Data-Plane Reviewer Evaluations

Vally evaluations for source-backed Java data-plane review rules.

Fixtures are synthetic PR snapshots. They provide PR metadata, unified
per-file diffs, and full package-tree evidence when a rule requires unchanged
package context. Expected findings belong only in the eval rubric.

Run from the repository root:

```powershell
.\.github\skills\data-plane-review\evals\run-evals.ps1
```

Use `-Runs 1` while iterating and increase the run count before broadening a
rule.
