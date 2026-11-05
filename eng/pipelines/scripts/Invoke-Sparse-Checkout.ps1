<#
.SYNOPSIS
Prepares or restores working-tree changes around a native sparse checkout.

.DESCRIPTION
Combines the initial sparse checkout patterns with paths computed by Java's dependency discovery.
Tracked-file changes are saved outside the repository and restored after the Azure Pipelines checkout
step resets them. The checkout must use clean: false to preserve generated untracked files.

.PARAMETER PathsJson
JSON representation of the additional paths to checkout.

.PARAMETER ChangesPath
Absolute path outside the repository for the patch containing tracked-file changes.

.PARAMETER SourceVersion
The original source revision, which must still be checked out before restoring changes.

.PARAMETER Restore
Restore the saved changes after the native checkout has completed.
#>

[CmdletBinding(DefaultParameterSetName = 'Prepare')]
param(
    [Parameter(Mandatory = $true, ParameterSetName = 'Prepare')]
    [string]$PathsJson,

    [Parameter(Mandatory = $true)]
    [string]$ChangesPath,

    [Parameter(Mandatory = $true, ParameterSetName = 'Restore')]
    [string]$SourceVersion,

    [Parameter(Mandatory = $true, ParameterSetName = 'Restore')]
    [switch]$Restore
)

$ErrorActionPreference = 'Stop'

if ($Restore) {
    $currentVersion = git rev-parse HEAD
    if ($LASTEXITCODE -ne 0 -or $currentVersion -ne $SourceVersion) {
        throw "The native checkout changed the source revision. Patch retained at $ChangesPath."
    }
    if ((Get-Item -LiteralPath $ChangesPath).Length -gt 0) {
        git apply --whitespace=nowarn -- $ChangesPath
        if ($LASTEXITCODE -ne 0) {
            throw "Restoring checkout changes failed with exit code $LASTEXITCODE. Patch retained at $ChangesPath."
        }
    }
    Remove-Item -LiteralPath $ChangesPath
    return
}

Write-Output '##vso[task.setvariable variable=SparseCheckoutRequired]false'

# Paths may be sourced as a yaml object literal OR a dynamically generated variable json string.
# If the latter, convertToJson will wrap the 'string' in quotes, so remove them.
$paths = $PathsJson.Trim('"') | ConvertFrom-Json
if (@($paths).Count -eq 0) {
    return
}

$isWorkingTree = git rev-parse --is-inside-work-tree
if ($LASTEXITCODE -ne 0 -or $isWorkingTree -ne 'true') {
    throw 'The repository is not an initialized Git working tree.'
}

$isSparseCheckout = git config --type=bool --default=false --get core.sparseCheckout
if ($LASTEXITCODE -ne 0) {
    throw 'Unable to determine sparse checkout mode.'
}
if ($isSparseCheckout -ne 'true') {
    Write-Information 'The repository has a full checkout. Skipping expansion.' -InformationAction Continue
    return
}

$patternsPath = git rev-parse --git-path info/sparse-checkout
if ($LASTEXITCODE -ne 0) {
    throw 'Unable to locate the initial sparse checkout patterns.'
}
$patterns = @(Get-Content -LiteralPath $patternsPath) + @($paths)
$quotedPatterns = foreach ($pattern in $patterns) {
    if ($pattern -match "[`r`n]") {
        throw 'Sparse checkout patterns cannot contain line breaks.'
    }
    '"' + ($pattern -replace '(\\*)"', '$1$1\"' -replace '(\\+)$', '$1$1') + '"'
}

New-Item -ItemType Directory -Path (Split-Path -Parent $ChangesPath) -Force | Out-Null
$SourceVersion = git rev-parse HEAD
if ($LASTEXITCODE -ne 0) {
    throw 'Unable to determine the original source revision.'
}
git diff --binary --no-ext-diff --no-textconv --src-prefix=a/ --dst-prefix=b/ --output=$ChangesPath $SourceVersion --
if ($LASTEXITCODE -ne 0) {
    throw "Saving checkout changes failed with exit code $LASTEXITCODE."
}

$patternsValue = ($quotedPatterns -join ' ').Replace('%', '%AZP25')
Write-Output "##vso[task.setvariable variable=SparseCheckoutPatterns]$patternsValue"
Write-Output "##vso[task.setvariable variable=SparseCheckoutSourceVersion]$SourceVersion"
Write-Output '##vso[task.setvariable variable=SparseCheckoutRequired]true'
