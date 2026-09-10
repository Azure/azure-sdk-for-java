# Engineering Tools

All the tools/utilities used in Microsoft Azure Java SDK's build config are defined here.

- `common` - Set of common engineering scripts that sync from our azure-sdk-tools repo. Generally shouldn't be edited directly in this repo.

- `pipelines` - Directory that contains yml files for our DevOps pipelines or supporting yml templates for them.

- `versioning` - Version text files (`version_client.txt`, `external_dependencies.txt`) and update tooling.

- `lintingconfigs` - CheckStyle and SpotBugs rule configurations.

## Sparse Checkouts

Java-owned pipeline jobs use the native Azure Pipelines
[`checkout` step](https://learn.microsoft.com/azure/devops/pipelines/yaml-schema/steps-checkout)
for both their initial sparse checkout and dependency-driven expansion. This requires agent version
3.253.0/4.253.0 or later and Git 2.25 or later.

- Use `sparseCheckoutPatterns` for non-cone patterns, including file globs and exclusions.
- Preserve the base patterns `/* !/*/ /eng /.config` and the order of job-specific patterns.
- Use `fetchFilter: tree:0`, `fetchDepth: 0`, and `fetchTags: true` to retain the existing treeless fetch, full history, and tags.
- Set `path` explicitly when checking out Java alongside build-tools; it is relative to `$(Pipeline.Workspace)`.

Java builds compute additional paths after generating project lists and updating POM files. Use
[pipelines/templates/steps/sparse-checkout-repo-initialized.yml](pipelines/templates/steps/sparse-checkout-repo-initialized.yml)
with `Paths: $(SparseCheckoutDirectories)` to expand these checkouts. The variable remains a JSON array.
The template prepares a space-separated `SparseCheckoutPatterns` variable containing the original patterns followed
by the additional paths, then passes it to a second native checkout. Both checkouts use `path: s` to retain the
existing source location. Empty path lists and full checkouts do not need a second checkout.

The agent performs a forced checkout even with `clean: false`. Before checkout, the helper saves tracked-file changes
as a binary patch under `$(Agent.TempDirectory)`. After successful checkout it verifies that the source revision has
not changed and restores the patch, including changes to POM files generated during dependency discovery.
The second checkout uses `clean: false` to retain generated untracked files. Restoration failures fail the job and
leave the patch available for diagnosis; do not replace this template with an unprotected second checkout.

Layered jobs keep private mirrored repositories on full checkouts so later expansion does not require persisted credentials.
Configurable external docs repositories still use the shared custom template because their repository names are selected
at runtime. Shared pipeline templates under `common` are maintained upstream in `azure-sdk-tools`.

Run the expansion regression tests from the repository root using PowerShell 7 and Pester 5.7.1:

```powershell
Import-Module Pester -RequiredVersion 5.7.1
Invoke-Pester -Path eng/scripts/tests/Sparse-Checkout.tests.ps1 -Output Detailed
```

Before rolling out to an agent pool, validate PR, FromSource, private-mirror, and multi-repository publishing jobs on
Windows and Linux. Compare source revisions, checked-out files, preserved POM edits, and checkout time and transfer size.

---

For developer guides (building, testing, code quality, versioning), see the consolidated documentation hub:
**[`docs/`](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/README.md)**

