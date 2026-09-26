# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
.SYNOPSIS
Classifies pull request paths to determine which Java validation jobs are required.

.DESCRIPTION
Classifies changes for the unified Java pull request pipeline. It is deny-first and
defaults unknown paths to full Java test validation. When every changed path has a
specialized non-Java validation route, it clears the job-local PackageInfo directory
so Create-PrJobMatrix produces an empty matrix.

Opted-in pipelines can suppress Build and Analyze for proven package-root
changelog-only public unified main PRs. This configuration requires enforced
Validate documentation coverage before merge. Pipelines without opt-in keep
Build and Analyze enabled. Existing test-only suppression is unchanged and adds
the JavaTestsSuppressed build tag for reporting.

.PARAMETER DiffPath
Path to the diff.json file produced by Generate-PR-Diff.ps1. ChangedFiles and
DeletedFiles from the document are classified. This parameter cannot be used
with ChangedFiles.

.PARAMETER ChangedFiles
Repository-relative paths to classify directly instead of reading diff.json.
This is intended for local diagnostics and unit tests. This parameter cannot
be used with DiffPath.

.PARAMETER PackageInfoDirectory
Job-local PackageInfo directory used to generate the PR test matrix. When the
classification does not require Java tests, JSON files below this directory
are removed so Create-PrJobMatrix.ps1 emits an empty matrix. If omitted, the
script reports the classification without modifying PackageInfo.

.PARAMETER ForceFullValidation
Forces Java tests to remain enabled regardless of path classification. The
pipeline can also enable this behavior by defining its ForceFullValidation
variable as true, which is passed through the FORCE_FULL_VALIDATION environment
variable.

.PARAMETER PassThru
Writes the classification object to the output pipeline for local diagnostics
and tests. Azure Pipelines normally consumes the emitted task variables instead.

.PARAMETER UseCombinedPRValidation
Enables main-target changelog coverage checks, assuming Validate documentation is
enforced as a required check before this configuration merges. Required-check settings
are not probed or changed here. Runtime repository, pipeline, and path scope still
constrain the route.

.PARAMETER ValidationDirectory
Directory for the complete merge-parent snapshot, routing report, and original
PackageInfo copy when the new route suppresses workers. Defaults beside DiffPath.

.PARAMETER RepositoryRoot
Checkout to inspect for complete Git changes and real Java package metadata.
#>

[CmdletBinding(DefaultParameterSetName = 'Diff')]
param(
    [Parameter(Mandatory = $true, ParameterSetName = 'Diff')]
    [string]$DiffPath,

    [Parameter(Mandatory = $true, ParameterSetName = 'Paths')]
    [AllowEmptyCollection()]
    [string[]]$ChangedFiles,

    [Parameter()]
    [string]$PackageInfoDirectory,

    [Parameter()]
    [switch]$ForceFullValidation,

    [Parameter()]
    [switch]$PassThru,

    [Parameter()]
    [switch]$UseCombinedPRValidation,

    [Parameter()]
    [string]$ServiceDirectory,

    [Parameter()]
    [string]$SdkType = 'client',

    [Parameter()]
    [string]$ValidationDirectory,

    [Parameter()]
    [string]$RepositoryRoot = (Join-Path $PSScriptRoot '..' '..')
)

Set-StrictMode -Version 3
$ErrorActionPreference = 'Stop'

function Normalize-RepositoryPath {
    param([Parameter(Mandatory = $true)][string]$Path)

    return ($Path -replace '\\', '/' -replace '^\./', '').TrimStart('/')
}

function New-PathClassification {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$Category,
        [Parameter(Mandatory = $true)][bool]$RequiresJavaTests,
        [Parameter(Mandatory = $true)][string]$Reason
    )

    return [PSCustomObject]@{
        Path = $Path
        Category = $Category
        RequiresJavaTests = $RequiresJavaTests
        Reason = $Reason
    }
}

function Get-PathClassification {
    param([Parameter(Mandatory = $true)][string]$Path)

    $normalizedPath = Normalize-RepositoryPath $Path
    $fileName = [System.IO.Path]::GetFileName($normalizedPath)
    $knownDocumentNames = @(
        'AGENTS.md',
        'CHANGELOG.md',
        'CODE_OF_CONDUCT.md',
        'CONTRIBUTING.md',
        'README.md',
        'SAMPLE.md',
        'SECURITY.md',
        'SUPPORT.md',
        'TROUBLESHOOTING.md'
    )

    # These paths can affect shipped artifacts or test behavior regardless of extension.
    if ($normalizedPath -match '(^|/)src/(main|test|test-shared)(/|$)') {
        return New-PathClassification $normalizedPath 'source-or-test-input' $true `
            'Files under src/main, src/test, or src/test-shared can affect runtime artifacts or test behavior.'
    }

    # Swagger Markdown is still a live AutoRest input for a small set of legacy packages.
    if ($normalizedPath -match '(^|/)(swagger|codegen)(/|$)' -or
        $fileName -in @('tsp-location.yaml', 'tspconfig.yaml')) {
        return New-PathClassification $normalizedPath 'code-generation-input' $true `
            'Code-generation inputs require generator validation and affected Java tests.'
    }

    if ($normalizedPath -match '^sdk/spring/scripts/.*managed_external_dependencies\.txt$') {
        return New-PathClassification $normalizedPath 'version-or-dependency-input' $true `
            'Version, dependency, BOM, and release inputs require Java build validation.'
    }

    if ($normalizedPath -match '^sdk/cosmos/(pipeline|test-resources)(/|$)' -or
        $normalizedPath -match '^sdk/spring/(pipeline|scripts)(/|$)') {
        return New-PathClassification $normalizedPath 'engineering-input' $true `
            'Service-specific engineering-system changes require full Java validation.'
    }

    $isCspellConfiguration = $normalizedPath -eq '.vscode/cspell.json' -or
        $normalizedPath -match '^sdk/[^/]+/cspell\.(json|ya?ml)$' -or
        $normalizedPath -match '^sdk/[^/]+/[^/]+/cspell\.(json|ya?ml)$'
    if ($normalizedPath -match '^eng/lintingconfigs/(checkstyle|spotbugs|revapi)/' -or
        $fileName -match '(?i)^(checkstyle(-suppressions)?\.xml|spotbugs-(include|exclude)\.xml|revapi(-suppressions)?\.json)$' -or
        $isCspellConfiguration) {
        return New-PathClassification $normalizedPath 'validation-configuration' $false `
            'Static-analysis and spelling configuration does not require Java tests; Build and Analyze remain enabled.'
    }

    # Keep engineering changes conservative because they can alter all CI behavior.
    if ($normalizedPath -match '^eng/') {
        return New-PathClassification $normalizedPath 'engineering-input' $true `
            'Engineering-system changes default to full Java validation.'
    }

    # These files have dedicated workflow or agent validation and do not affect Java artifacts.
    if ($normalizedPath -match '^\.github/(workflows|skills|agents)/.*\.md$' -or
        $normalizedPath -match '^\.github/ISSUE_TEMPLATE/.*\.md$' -or
        $normalizedPath -in @('.github/PULL_REQUEST_TEMPLATE.md', '.github/copilot-instructions.md')) {
        return New-PathClassification $normalizedPath 'workflow-or-agent-input' $false `
            'Workflow and agent Markdown uses specialized validation rather than Java tests.'
    }

    if ($normalizedPath -match '^docs(/|$)' -and
        [System.IO.Path]::GetExtension($normalizedPath) -in @('.md', '.png')) {
        return New-PathClassification $normalizedPath 'consumer-documentation' $false `
            'The top-level docs tree does not affect Java runtime behavior.'
    }

    if ($fileName -in $knownDocumentNames) {
        return New-PathClassification $normalizedPath 'consumer-documentation' $false `
            'This is a known consumer, contributor, or policy document outside protected paths.'
    }

    if ($fileName -in @('LICENSE.txt', 'NOTICE.txt', 'THIRD-PARTY.txt')) {
        return New-PathClassification $normalizedPath 'legal-documentation' $false `
            'Legal documentation does not require Java tests.'
    }

    return New-PathClassification $normalizedPath 'unknown-or-functional' $true `
        'Unrecognized paths fail safe to full Java validation.'
}

function Get-ChangedPathsFromDiff {
    param([Parameter(Mandatory = $true)][string]$Path)

    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        Write-Warning "PR diff file does not exist. Falling back to full Java validation: $Path"
        return @()
    }

    $diff = Get-Content -LiteralPath $Path -Raw | ConvertFrom-Json
    $paths = @()

    if ($diff.PSObject.Properties.Name -contains 'ChangedFiles' -and $diff.ChangedFiles) {
        $paths += @($diff.ChangedFiles)
    }
    if ($diff.PSObject.Properties.Name -contains 'DeletedFiles' -and $diff.DeletedFiles) {
        $paths += @($diff.DeletedFiles)
    }

    return @($paths | ForEach-Object { Normalize-RepositoryPath $_ } | Sort-Object -Unique)
}

function Get-PRChangeClassification {
    param(
        [Parameter(Mandatory = $true)]
        [AllowEmptyCollection()]
        [string[]]$Paths,

        [Parameter()]
        [bool]$ForceValidation = $false
    )

    $classifications = @($Paths | ForEach-Object { Get-PathClassification $_ })
    $hasChanges = $classifications.Count -gt 0
    $runTests = -not $hasChanges -or @($classifications | Where-Object RequiresJavaTests).Count -gt 0

    if ($ForceValidation) {
        $runTests = $true
    }

    $documentationCategories = @('consumer-documentation', 'legal-documentation')
    $documentationOnly = $hasChanges `
        -and @($classifications | Where-Object { $_.Category -notin $documentationCategories }).Count -eq 0

    return [PSCustomObject]@{
        DocumentationOnly = $documentationOnly
        ForceFullValidation = $ForceValidation
        RunBuild = $true
        RunAnalyze = $true
        ChangeLogOnly = $false
        SkipBuildChangeLogValidation = $false
        RunTests = $runTests
        RunDocs = @($classifications | Where-Object {
                $_.Category -in @('consumer-documentation', 'legal-documentation')
            }).Count -gt 0
        RunCodegen = @($classifications | Where-Object Category -eq 'code-generation-input').Count -gt 0
        RunEngTests = @($classifications | Where-Object Category -eq 'engineering-input').Count -gt 0
        Paths = $classifications
    }
}

$forceValidation = $ForceFullValidation.IsPresent -or
    $env:FORCE_FULL_VALIDATION -match '(?i)^(true|1|yes)$'
$pathsToClassify = @(
    if ($PSCmdlet.ParameterSetName -eq 'Diff') {
        Get-ChangedPathsFromDiff $DiffPath
    } else {
        $ChangedFiles | ForEach-Object { Normalize-RepositoryPath $_ } | Sort-Object -Unique
    }
)

$result = Get-PRChangeClassification -Paths $pathsToClassify -ForceValidation $forceValidation

if ($UseCombinedPRValidation) {
    . (Join-Path $PSScriptRoot 'helpers' 'PR-ChangeLog-Routing.ps1')
    $coverage = Get-PRChangeLogCoverage -Enabled $true -ServiceDirectory $ServiceDirectory -SdkType $SdkType -Context @{
        TeamProject = $env:SYSTEM_TEAMPROJECT
        CollectionUri = $env:SYSTEM_COLLECTIONURI
        RepositoryName = $env:BUILD_REPOSITORY_NAME
        RepositoryProvider = $env:BUILD_REPOSITORY_PROVIDER
        RepositoryUri = $env:BUILD_REPOSITORY_URI
        Reason = $env:BUILD_REASON
        SourceBranch = $env:BUILD_SOURCEBRANCH
        TargetBranch = $env:SYSTEM_PULLREQUEST_TARGETBRANCH
    }
    Write-Host "Combined PR changelog coverage: $($coverage.Reason)"
    if ($coverage.Covered -and -not $forceValidation) {
        $timer = [System.Diagnostics.Stopwatch]::StartNew()
        try {
            if ($PSCmdlet.ParameterSetName -ne 'Diff' -or -not (Test-Path -LiteralPath $DiffPath -PathType Leaf)) {
                throw 'Changelog routing requires the original PR diff and PackageInfo, not a diagnostic path list.'
            }
            $RepositoryRoot = (Resolve-Path -LiteralPath $RepositoryRoot).Path
            $snapshot = Get-PRValidationSnapshot -RepositoryRoot $RepositoryRoot
            if ($snapshot.SourceCommit -cne $env:BUILD_SOURCEVERSION) {
                throw 'The checked-out merge commit does not match Build.SourceVersion.'
            }
            if (-not $ValidationDirectory) {
                $ValidationDirectory = Join-Path (Split-Path $DiffPath -Parent) 'pr-validation'
            }
            New-Item -ItemType Directory -Path $ValidationDirectory -Force | Out-Null
            $snapshot | ConvertTo-Json -Depth 30 | Set-Content -LiteralPath (Join-Path $ValidationDirectory 'diff.json')
            $pathsToClassify = @($snapshot.ChangedFiles) + @($snapshot.DeletedFiles)
            $result = Get-PRChangeClassification -Paths $pathsToClassify -ForceValidation $forceValidation
            $result.SkipBuildChangeLogValidation = $pathsToClassify.Count -gt 0

            # Inspect original metadata before the existing test-suppression code clears it below.
            $eligibility = Get-PRChangeLogOnlyEligibility -Snapshot $snapshot -RepositoryRoot $RepositoryRoot `
                -PackageInfoDirectory $PackageInfoDirectory
            if ($eligibility.Eligible) {
                $originalMetadata = Join-Path $ValidationDirectory 'PackageInfo'
                New-Item -ItemType Directory -Path $originalMetadata -Force | Out-Null
                Get-ChildItem -LiteralPath $PackageInfoDirectory -File -Filter '*.json' -Recurse |
                    Copy-Item -Destination $originalMetadata
                $result.ChangeLogOnly = $true
                $result.RunBuild = $false
                $result.RunAnalyze = $false
                $result.RunTests = $false
            }
            elseif ($eligibility.RequiresFullValidation) {
                $result.RunTests = $true
            }
            $timer.Stop()
            $eligibility | Add-Member -NotePropertyName DurationSeconds -NotePropertyValue $timer.Elapsed.TotalSeconds
            $eligibility | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath (Join-Path $ValidationDirectory 'routing.json')
            Write-Host "PR changelog-only routing ($($timer.Elapsed.TotalSeconds.ToString('F3'))s): $($eligibility.Reason)"
        }
        catch {
            $result = Get-PRChangeClassification -Paths $pathsToClassify -ForceValidation $forceValidation
            $result.RunTests = $true
            Write-Warning "PR changelog-only routing unavailable; retaining all Java validation and the Build changelog check: $($_.Exception.Message)"
        }
    }
    elseif ($forceValidation) {
        Write-Host 'ForceFullValidation retains Build, Analyze, tests, and the Build changelog check.'
    }
}

if (-not $result.RunTests -and $PackageInfoDirectory -and
    -not (Test-Path -LiteralPath $PackageInfoDirectory -PathType Container)) {
    Write-Warning "PackageInfo directory does not exist. Falling back to full Java validation: $PackageInfoDirectory"
    $result.RunTests = $true
}

foreach ($pathResult in $result.Paths) {
    Write-Host "PR change '$($pathResult.Path)' => $($pathResult.Category): $($pathResult.Reason)"
}

Write-Host "PR change classification: RunBuild=$($result.RunBuild), RunAnalyze=$($result.RunAnalyze), RunTests=$($result.RunTests), RunDocs=$($result.RunDocs), RunCodegen=$($result.RunCodegen), RunEngTests=$($result.RunEngTests), ChangeLogOnly=$($result.ChangeLogOnly), SkipBuildChangeLogValidation=$($result.SkipBuildChangeLogValidation), ForceFullValidation=$($result.ForceFullValidation)"

# Build/Analyze consume the routing outputs only in the opted-in pipeline scope.
# RunTests continues to control this job's PackageInfo removal and test matrices.
foreach ($variableName in @(
        'DocumentationOnly',
        'RunBuild',
        'RunAnalyze',
        'ChangeLogOnly',
        'SkipBuildChangeLogValidation',
        'RunTests',
        'RunDocs',
        'RunCodegen',
        'RunEngTests'
    )) {
    $value = ([string]$result.$variableName).ToLowerInvariant()
    Write-Host "##vso[task.setvariable variable=$variableName;isOutput=true]$value"
}

if (-not $result.RunTests -and $PackageInfoDirectory) {
    $packageInfoFiles = @(Get-ChildItem -LiteralPath $PackageInfoDirectory -Filter '*.json' -File -Recurse)
    foreach ($packageInfoFile in $packageInfoFiles) {
        Remove-Item -LiteralPath $packageInfoFile.FullName -Force
    }
    Write-Host "Removed $($packageInfoFiles.Count) job-local PackageInfo file(s) so the Java test matrix is empty."
    Write-Host '##vso[build.addbuildtag]JavaTestsSuppressed'
}

if ($PassThru) {
    Write-Output $result
}
