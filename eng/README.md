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
The changelog-only route below uses the existing preflight, not broader path exclusions.

The existing required **Check Spelling** workflow remains unchanged and checks spelling only.
The new [Validate documentation workflow](../.github/workflows/validate-documentation.yml) runs combined spelling,
changelog, and reporting steps in one `validate-documentation` job on one `ubuntu-slim` runner. Its distinct
**Validate documentation** check starts alongside the existing required check; this change does not update rulesets.
Both workflows cover the same supported PR branches without path filters.

Newer **Validate documentation** runs cancel older runs for the same PR. The concurrency group includes the workflow
name and PR number, so it does not cancel runs for other PRs or other workflows.

Within the combined workflow, one checkout and Node 24 setup serve all validation steps.
[Save-PRValidationInputs.ps1](scripts/Save-PRValidationInputs.ps1) saves the entire synthetic merge commit's diff
against its first parent. It includes deletions and both sides of renames, not just the last source commit.
Spelling uses the existing CSpell configuration and ignore rules, with one `npm ci` from the shared spelling
lockfile and the installed binary (never global CSpell).

[Invoke-PRValidation.ps1](scripts/Invoke-PRValidation.ps1) also verifies changelogs for the unified Java PR Build
selection: `SDKType=client`, current `ExcludePaths`, and the shared `Get-PrPkgProperties`/Java metadata helpers.
This includes indirect and template fallback packages and changes to POM versions, Java, or other package inputs,
even when `CHANGELOG.md` was not edited. Explicit `ArtifactDetails.skipVerifyChangeLog` opt-outs remain effective.
Except for the root/docs-only fast path, the shared Build selector's template fallback is retained even when
`ExcludePaths` removes every changed and deleted path.
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
or the summary.

**Verify Links** remains a separate, unchanged workflow.
Package selection retains the existing `ExcludePaths` prefix-matching behavior.

During migration, both workflows run, temporarily duplicating spelling work and using two runners.
After **Validate documentation** has successful runs and its failure behavior is verified, make that check required
while **Check Spelling** is still required. Then remove the old requirement before deleting `check-spelling.yml`
in a separate cleanup. Existing PRs may need an update/new run to report the new check. Until that ruleset transition,
the combined check is not a replacement for existing required validation.

### Changelog-only Azure routing

Only [pullrequest.yml](pipelines/pullrequest.yml) opts in to `UseCombinedPRValidation`. The stage restricts it to
public `azure-sdk` builds of `Azure/azure-sdk-for-java`, `ServiceDirectory=auto`, `SDKType=client`, PR builds, and
non-test-pipeline runs. At runtime, the classifier also requires the canonical GitHub repository, a PR merge ref,
and target `main` (or `refs/heads/main`). This is the initial scope where **Check Spelling** is known to be required.
Private/mirrored/internal repositories, service-specific pipelines (including Cosmos and Spring), other SDK types,
manual/scheduled/CI builds, and release stages do not opt in. Feature, release, and hotfix PR targets retain their
existing validation and Build changelog check.

The existing `generate_job_matrix` job still discovers packages first.
[Classify-PRChanges.ps1](scripts/Classify-PRChanges.ps1) then uses the shared complete merge-parent snapshot helper,
verifies the checked-out SHA against `Build.SourceVersion`, and calls the Java-owned
[eligibility helper](scripts/helpers/PR-ChangeLog-Routing.ps1) **before** clearing any job-local `PackageInfo`.
The matrix checkout already retains full commit history and checks out `Build.SourceVersion`; no extra checkout,
preflight job, or agent is introduced. An unavailable/mismatched snapshot retains all Java jobs and the Build
changelog check.

Azure routing reuses exactly `powershell-yaml` 0.4.7 from a loaded or installed/discoverable module left by the
existing metadata preparation. It does not create another module cache or restore from PSGallery. The shared
bootstrap accepts newer versions, but that is not evidence of this exact pin: an absent pin, incompatible loaded
version, or import failure retains Java workers with an explicit diagnostic. The GitHub runner keeps its separate,
public, run-owned cold bootstrap described above.

Skipping Build, Analyze, and tests requires at least one actual, applicable package changelog edit. Every path
must be either its exact `sdk/<service>/<package>/CHANGELOG.md` or an already-reviewed root document / `docs/`
text file (`.md` or `.txt`) outside protected input directories. Packages must be track 2 with metadata SDK type
`client` or `mgmt`, have one directly selected owner, and match their real Java POM and service- or package-level
CI artifact metadata. Changelogs and POMs must be existing regular files in both Git revisions. Companion documents
must be regular files in their applicable revision. `skipVerifyChangeLog` remains an opt-out from content checking,
not permission to bypass Java workers.

New packages/POM changes, added/deleted/renamed package changelogs, package READMEs (which still need snippet and
Analyze validation), source/test/resources, Swagger/codegen/TypeSpec, engineering inputs, unsupported layouts/types,
and unknown paths do not qualify. Empty diffs and missing, corrupt, indirect-only, or unmapped package metadata
also cannot authorize the route. In the covered scope, an unproven non-companion `CHANGELOG.md` change retains all
Java workers, even if the older basename classifier would have suppressed tests. Other existing documentation-only
test suppression is unchanged. Invalid changelog **content** does not need a Java build to fail: the required
combined check still runs the real content validator and fails the PR.

For a proven change set, the classifier emits `RunBuild=false`, `RunAnalyze=false`, and `RunTests=false`, preserves
the original metadata under `$(Build.ArtifactStagingDirectory)/pr-validation/PackageInfo`, and clears only the
matrix job's original package files. The same directory keeps `diff.json` and `routing.json` with the reason and
elapsed time. The existing `JavaTestsSuppressed` tag remains. The preflight stays allocated; two Build/Analyze
workers are avoided in addition to the test workers already suppressed by the earlier classifier.

`SkipBuildChangeLogValidation` is a separate coverage output: covered, nonempty main PRs use the existing required
combined job instead of repeating the Build changelog step, including functional PRs that still run Java.
`ForceFullValidation=true` disables both routing shortcuts and the earlier test suppression, restoring the Build
changelog check too. Missing/unknown job outputs default to running workers, and missing coverage output retains
the Build changelog step. Success guards prevent cancellation or failed preflight from becoming a successful Azure
check. All non-covered Build invocations, SDK-type behavior, artifact opt-outs, and explicit `ForRelease=true`
checks in batch, automatic, and patch releases remain unchanged. Automatic dated-entry checks also remain intact.

**Scheduling tradeoff:** Build and Analyze gain a dependency on the existing matrix job for public unified PRs.
Azure does [not expose `System.PullRequest.TargetBranch` in templates](https://learn.microsoft.com/azure/devops/pipelines/build/variables#system-variables-devops-services),
so non-main public unified PRs wait too, although they do not use the new bypass. Service/private/non-PR/release jobs
are not serialized by this change. Ordinary PRs must now wait for the preflight's checkout, metadata discovery, and
matrix generation before Build/Analyze can be scheduled; this is not a claim of lower latency for every PR.
Local Windows measurements were 0.23-0.37 seconds for a complete non-changelog snapshot/routing probe (five runs,
no optional modules loaded), and 1.6-2.3 seconds for eligibility against copied real core, communication, and
management metadata (excluding dependency restore and prior package selection). Hosted queue time and end-to-end
Azure scheduling were not measured locally. The focused tests verify YAML wiring/conditions, not server-side
template expansion.

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
under `eng/scripts/`, including changelog eligibility, scope, output/dependency contracts, and metadata-before-clearing
regressions. Integration tests also make real Git edits against copied repository CI/POM/changelog metadata.
Their YAML bootstrap uses the same public pinned requirement. On Windows, the loaded YAML
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

The true-empty npm download-cache probe is a separate network check, not equivalent to a successful run using
cached locked packages. Local empty-cache validation was blocked by verified TLS connectivity to
`registry.npmjs.org`; it was not counted as passing. Do not disable TLS or change global feeds to bypass that failure.

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
