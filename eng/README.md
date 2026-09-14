# Engineering Tools

All the tools/utilities used in Microsoft Azure Java SDK's build config are defined here.

- `common` - Set of common engineering scripts that sync from our azure-sdk-tools repo. Generally shouldn't be edited directly in this repo.

- `pipelines` - Directory that contains yml files for our DevOps pipelines or supporting yml templates for them.

- `versioning` - Version text files (`version_client.txt`, `external_dependencies.txt`) and update tooling.

- `lintingconfigs` - CheckStyle and SpotBugs rule configurations.

## PR Documentation Validation

The unified Java PR pipeline excludes `docs/**`, shared `.github/skills/azsdk-common-*/**` content, and exactly
these repository-root documents: `AGENTS.md`, `CODE_OF_CONDUCT.md`, `CONTRIBUTING.md`, `LICENSE.txt`, `NOTICE.txt`,
`README.md`, `SECURITY.md`, and `SUPPORT.md`. The `docs/` and root-document entries are also listed in
`ExcludePaths` in [pullrequest.yml](pipelines/pullrequest.yml), so they do not select Java packages in mixed PRs.
SDK-package documents, CHANGELOGs, source/resources, and unknown paths gain no trigger exclusions.
Build/Analyze orchestration and the existing test-matrix classifier are unchanged.

The required **Check Spelling** job still checks all supported PR branches without path filters, using the existing
CSpell configuration and ignore rules. In the same job, [Test-RootDocumentationExclusions.ps1](scripts/Test-RootDocumentationExclusions.ps1)
checks the entire tracked-path inventory, even if spelling fails or has no files to check. A native regex prefilter
limits detailed comparisons to root candidates, including unusual root characters needed for culture-aware matching.
This temporary guard rejects longer prefixes (such as `README.md.template` or `README.md/src/Example.java`) and
case-only aliases because package selection still uses prefix matching. Nested names such as `sdk/example/README.md`
do not collide with root exclusions. Git inventory failures also fail the job. Rename a colliding path or remove
its matching root-document exclusion from both lists before adding it. Shared matcher hardening remains an upstream
`azure-sdk-tools` change; do not patch `eng/common` locally. **Verify Links** remains a separate, unchanged workflow.

Run the guard and its regression tests with PowerShell 7, Git, and the CI-declared Pester 5.7.1 (no YAML module required):

```powershell
./eng/scripts/Test-RootDocumentationExclusions.ps1
Import-Module Pester -RequiredVersion 5.7.1
Invoke-Pester -Path @(
    'eng/scripts/tests/PullRequest-Trigger.tests.ps1',
    'eng/scripts/tests/RootDocumentationExclusions.tests.ps1',
    'eng/scripts/tests/Classify-PRChanges.tests.ps1'
) -Tag UnitTest -Output Detailed
```

## Sparse Checkouts

Java-owned pipeline jobs use the native Azure Pipelines
[`checkout` step](https://learn.microsoft.com/azure/devops/pipelines/yaml-schema/steps-checkout)
for both their initial sparse checkout and dependency-driven expansion. This requires agent version
3.253.0/4.253.0 or later and Git 2.25 or later.

- Use `sparseCheckoutPatterns` for non-cone patterns, including file globs and exclusions.
- Preserve the base patterns `/* !/*/ /eng /.config` and the order of job-specific patterns.
- Write literal patterns one per line in a folded YAML scalar (`>-`); YAML joins them with spaces for the checkout task.
- Use `fetchFilter: tree:0` and `fetchDepth: 0` to retain treeless fetches and full commit history.
- Set `fetchTags: false` explicitly to avoid unnecessary tag synchronization and pipeline-dependent defaults.
- Set `AGENT_SOURCE_CHECKOUT_QUIET: 'true'` in each checkout step's `env` to suppress verbose checkout progress output.
- Set `path` explicitly when checking out Java alongside build-tools; it is relative to `$(Pipeline.Workspace)`.

Initial checkouts that can run test-pipeline versioning use `fetchTags: ${{ parameters.TestPipeline }}` instead.
[SetTestPipelineVersion.ps1](common/scripts/SetTestPipelineVersion.ps1) reads local tags to choose the version, so those
jobs still need tags when `TestPipeline` is enabled. Release creation checks and creates tags through the GitHub API
and does not require local tags. Expansion checkouts do not fetch tags again.

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

