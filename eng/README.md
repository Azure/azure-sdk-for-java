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

The existing required **Check Spelling** workflow remains unchanged and checks spelling only.
The new [Validate documentation workflow](../.github/workflows/validate-documentation.yml) runs combined spelling,
changelog, and reporting steps in one `validate-documentation` job on one `ubuntu-slim` runner. Its distinct
**Validate documentation** check starts alongside the existing required check; this change does not update rulesets.
Both workflows cover the same supported PR branches without path filters.

Within the combined workflow, one checkout and Node 24 setup serve all validation steps.
[Save-PRValidationInputs.ps1](scripts/Save-PRValidationInputs.ps1) saves the entire synthetic merge commit's diff
against its first parent. It includes deletions and both sides of renames, not just the last source commit.
Spelling uses the existing CSpell configuration and ignore rules, with one `npm ci` from the shared spelling
lockfile and the installed binary (never global CSpell).

[Invoke-PRValidation.ps1](scripts/Invoke-PRValidation.ps1) also verifies changelogs for the unified Java PR Build
selection: `SDKType=client`, current `ExcludePaths`, and the shared `Get-PrPkgProperties`/Java metadata helpers.
This includes indirect and template fallback packages and changes to POM versions, Java, or other package inputs,
even when `CHANGELOG.md` was not edited. Explicit `ArtifactDetails.skipVerifyChangeLog` opt-outs remain effective.
Missing or malformed required metadata fails validation instead of silently dropping a package. The real
`Confirm-ChangeLogEntry` runs with `ForRelease=false`; a dated entry still receives its automatic release checks,
including section content and release-date validation. Only already-excluded root/docs-only diffs (or proven empty
diffs) avoid package discovery. A successful check is **not** a certificate that arbitrary `CHANGELOG.md` paths,
unknown layouts, source/resources, or code-generation inputs can skip Build/Analyze.

The YAML module is restored only when metadata is needed. The Java-owned
[requirements file](scripts/pr-validation-requirements.psd1) pins `powershell-yaml` 0.4.7, matching the shared
helper's requirement. `Save-Module` restores it from public PSGallery into the run-owned module directory, without
registering feeds or changing persistent module settings. Cold runners need PowerShell 7 (included in
`ubuntu-slim`), Git, the workflow's Node 24 setup, and access to PSGallery and the public npm registry. No Maven
build, authenticated Azure feed, or Azure resources are needed.

Spelling failures do not skip changelog validation. The final reporting step includes each
step outcome, package/version/path, and validation errors in the job summary; failed, missing, or unexpectedly
skipped required steps fail the combined check. Setup failures stay failures, and cancellation is not converted to
success. File content is printed with runner-command processing suspended and escaped when added to annotations
or the summary. Azure Build's existing PR changelog verification remains temporarily enabled for parity burn-in;
non-PR and release validation are unchanged.

**Verify Links** remains a separate, unchanged workflow.
Package selection retains the existing `ExcludePaths` prefix-matching behavior.

During migration, both workflows run, temporarily duplicating spelling work and using two runners.
After **Validate documentation** has successful runs and its failure behavior is verified, make that check required
while **Check Spelling** is still required. Then remove the old requirement before deleting `check-spelling.yml`
in a separate cleanup. Existing PRs may need an update/new run to report the new check. Until that ruleset transition,
the combined check is not a replacement for existing required validation.

Run the trigger and classifier regression tests with PowerShell 7, Git, and the CI-declared Pester 5.7.1
(no YAML module required):

```powershell
Import-Module Pester -RequiredVersion 5.7.1
Invoke-Pester -Path @(
    'eng/scripts/tests/PullRequest-Trigger.tests.ps1',
    'eng/scripts/tests/Classify-PRChanges.tests.ps1'
) -Tag UnitTest -Output Detailed
```

The combined validation tests use synthetic repositories outside `sdk/` and the existing `UnitTest` discovery
under `eng/scripts/`. Their YAML bootstrap uses the same public pinned requirement. On Windows, the loaded YAML
assembly must stay outside Pester's disposable `TestDrive`; set `PR_VALIDATION_TEST_MODULES` to a task-owned cache,
or use the default `azure-java-pr-validation-test-modules` directory below the system temporary directory.

```powershell
Import-Module Pester -RequiredVersion 5.7.1
Invoke-Pester -Path eng/scripts/tests/PRValidation.tests.ps1 -Tag UnitTest -Output Detailed
# Also exercise real Java metadata parity and the exact CI-locked CSpell binary:
Invoke-Pester -Path eng/scripts/tests/PRValidation.tests.ps1 -Tag IntegrationTest -Output Detailed
# Optional network probe with an empty npm download cache (public npm access required):
$env:PR_VALIDATION_TEST_COLD_NPM = 'true'
Invoke-Pester -Path eng/scripts/tests/PRValidation.tests.ps1 -FullNameFilter 'Locked CSpell behavior*' -Output Detailed
Remove-Item Env:PR_VALIDATION_TEST_COLD_NPM

# Local diagnostics (use a new task-owned output directory):
./eng/scripts/Save-PRValidationInputs.ps1 -OutputDirectory "$env:TEMP/pr-validation-local" `
    -SourceCommittish HEAD -TargetCommittish main
./eng/scripts/Invoke-PRValidation.ps1 -Check Spelling -OutputDirectory "$env:TEMP/pr-validation-local"
./eng/scripts/Invoke-PRValidation.ps1 -Check Changelogs -OutputDirectory "$env:TEMP/pr-validation-local"
```

The snapshot, package-selection diff, exported `PackageInfo` JSON, and per-check results stay in that output
directory. On a failure, inspect the named step log and summary, then correct the reported file, POM version,
artifact metadata, or dependency download. Changelog format guidance is in the
[release policy's Change Logs section](https://azure.github.io/azure-sdk/policies_releases.html#change-logs).

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
