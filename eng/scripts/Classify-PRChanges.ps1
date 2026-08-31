# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
.SYNOPSIS
Classifies pull request paths to determine whether Java test matrix jobs are required.

.DESCRIPTION
Classifies changes for the unified Java pull request pipeline. It is deny-first and
defaults unknown paths to full Java test validation. When every changed path has a
specialized non-Java validation route, it clears the job-local PackageInfo directory
so Create-PrJobMatrix produces an empty matrix.

Build and Analyze remain enabled in this phase so their unique validation is preserved.

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
    [switch]$PassThru
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

    # These paths can affect shipped artifacts or test behavior regardless of extension.
    if ($normalizedPath -match '(^|/)src/(main|test)(/|$)') {
        return New-PathClassification $normalizedPath 'source-or-test-input' $true `
            'Files under src/main or src/test can affect runtime artifacts or test behavior.'
    }

    # Swagger Markdown is still a live AutoRest input for a small set of legacy packages.
    if ($normalizedPath -match '(^|/)(swagger|codegen)(/|$)' -or
        $fileName -in @('tsp-location.yaml', 'tspconfig.yaml')) {
        return New-PathClassification $normalizedPath 'code-generation-input' $true `
            'Code-generation inputs require generator validation and affected Java tests.'
    }

    if ($normalizedPath -match '^eng/versioning/.*\.txt$' -or
        $normalizedPath -match '^sdk/spring/scripts/.*managed_external_dependencies\.txt$' -or
        $normalizedPath -match '^eng/bomgenerator/includes/.*\.txt$' -or
        $normalizedPath -eq 'eng/pipelines/patch_release_client.txt') {
        return New-PathClassification $normalizedPath 'version-or-dependency-input' $true `
            'Version, dependency, BOM, and release inputs require Java build validation.'
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

    if ($normalizedPath -in @(
            'AGENTS.md',
            'CODE_OF_CONDUCT.md',
            'CONTRIBUTING.md',
            'README.md',
            'SECURITY.md',
            'SUPPORT.md'
        )) {
        return New-PathClassification $normalizedPath 'consumer-documentation' $false `
            'This is a repository-level consumer or contributor document.'
    }

    if ($fileName -in @('README.md', 'CHANGELOG.md', 'SAMPLE.md', 'TROUBLESHOOTING.md', 'AGENTS.md') -and
        $normalizedPath -match '^sdk/') {
        return New-PathClassification $normalizedPath 'consumer-documentation' $false `
            'This is SDK-area consumer documentation outside source and code-generation paths.'
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
        # These outputs are reserved diagnostics until later phases wire their owning jobs.
        RunBuild = $true
        RunAnalyze = $true
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

if (-not $result.RunTests -and $PackageInfoDirectory -and
    -not (Test-Path -LiteralPath $PackageInfoDirectory -PathType Container)) {
    Write-Warning "PackageInfo directory does not exist. Falling back to full Java validation: $PackageInfoDirectory"
    $result.RunTests = $true
}

foreach ($pathResult in $result.Paths) {
    Write-Host "PR change '$($pathResult.Path)' => $($pathResult.Category): $($pathResult.Reason)"
}

Write-Host "PR change classification: RunBuild=$($result.RunBuild), RunAnalyze=$($result.RunAnalyze), RunTests=$($result.RunTests), RunDocs=$($result.RunDocs), RunCodegen=$($result.RunCodegen), RunEngTests=$($result.RunEngTests), ForceFullValidation=$($result.ForceFullValidation)"

# RunTests controls PackageInfo removal below. The remaining output variables are
# diagnostic/reserved signals for subsequent rollout phases.
foreach ($variableName in @(
        'DocumentationOnly',
        'RunBuild',
        'RunAnalyze',
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
}

if ($PassThru) {
    Write-Output $result
}
