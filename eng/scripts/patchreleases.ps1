# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<# Patch releases script does the following
    1. Identify all the artifacts under maven 'com.azure' group that have misaligned dependencies.
    PS - This analysis is only done for the GA libraries.
    2. Create patched sources for each of these artifacts in a branch.
    3. Release patches from this branch.
    4. Generate the forward looking BOM file and create a branch for the BOM release.
#>
param(
    [string]$GroupId = "com.azure"
)

Write-Information "PS Script Root is: $PSScriptRoot"
$RepoRoot = Resolve-Path "${PSScriptRoot}../../.."
$CommonScriptFilePath = Join-Path $RepoRoot "eng" "common" "scripts" "common.ps1"
$BomHelpersFilePath = Join-Path $PSScriptRoot "bomhelpers.ps1"
$PatchHelpersFilePath = Join-Path $PSScriptRoot "patchhelpers.ps1"
$PatchReportFile = Join-Path $PSScriptRoot "patchreport.json"
$BomFilePath = Join-Path $RepoRoot "sdk" "boms" "azure-sdk-bom" "pom.xml"
$BomChangeLogPath = Join-Path $RepoRoot "sdk" "boms" "azure-sdk-bom" "changelog.md"
. $CommonScriptFilePath
. $BomHelpersFilePath
. $PatchHelpersFilePath

$ArtifactInfos = GetVersionInfoForAllMavenArtifacts -GroupId $GroupId
$IgnoreList = @(
    'azure-client-sdk-parent',
    'azure-core-management',
    'azure-core-parent',
    'azure-core-test',
    'azure-sdk-all',
    'azure-sdk-bom',
    'azure-sdk-parent',
    'azure-sdk-template',
    'azure-sdk-template-bom',
    'azure-data-sdk-parent',
    'azure-spring-data-cosmos',
    'azure-cosmos-cassandra-driver-3',
    'azure-cosmos-cassandra-driver-4',
    'azure-cosmos-cassandra-driver-3-extensions',
    'azure-cosmos-cassandra-driver-4-extensions',
    'azure-cosmos-cassandra-spring-data-extensions'
)

$inEligibleKeys = $ArtifactInfos.Keys | Where-Object { !$ArtifactInfos[$_].LatestGAOrPatchVersion -or $IgnoreList -contains $_ }
$inEligibleKeys | ForEach-Object { $ArtifactInfos.Remove($_) }

UpdateDependencies -ArtifactInfos $ArtifactInfos

# For testing only.
# $AzCoreArtifactId = "azure-core"
# $AzCoreVersion = $ArtifactInfos[$AzCoreArtifactId].LatestGAOrPatchVersion
# $AzCoreVersion = "1.28.0"
# $ArtifactInfos[$AzCoreArtifactId].FutureReleasePatchVersion = $AzCoreVersion
# $AzCoreNettyArtifactId = "azure-core-http-netty"
# $ArtifactInfos[$AzCoreNettyArtifactId].Dependencies[$AzCoreArtifactId] = $AzCoreVersion

$AllDependenciesWithVersion = CreateForwardLookingVersions -ArtifactInfos $ArtifactInfos
FindAllArtifactsThatNeedPatching -ArtifactInfos $ArtifactInfos -AllDependenciesWithVersion $AllDependenciesWithVersion
$ArtifactsToPatch =  $ArtifactInfos.Keys | Where-Object { $null -ne $ArtifactInfos[$_].FutureReleasePatchVersion } | ForEach-Object {$ArtifactInfos[$_].ArtifactId}

$RemoteName = GetRemoteName
$CurrentBranchName = GetCurrentBranchName
if ($LASTEXITCODE -ne 0) {
    LogError "Could not correctly get the current branch name."
    exit 1
}
UpdateCIInformation -ArtifactsToPatch $ArtifactsToPatch.Keys -ArtifactInfos $ArtifactInfos

$bomPatchVersion = GetNextBomVersion
$bomBranchName = "bom_$bomPatchVersion"
$ArtifactPatchInfos = @()
Write-Output "Preparing patch releases for BOM updates."
try {
    $patchBranchName = "PatchSet_$bomPatchVersion"
    Write-Host "git checkout -b $patchBranchName $RemoteName/main"
    git checkout -b $patchBranchName $RemoteName/main
    UpdateDependenciesInVersionClient -ArtifactInfos $ArtifactInfos

    foreach ($artifactId in $ArtifactsToPatch) {
        $arInfo = $ArtifactInfos[$artifactId]
        $patchInfo = [ArtifactPatchInfo]::new()
        $patchInfo = ConvertToPatchInfo -ArInfo $arInfo
        $ArtifactPatchInfos += $patchInfo
        GeneratePatches -ArtifactPatchInfos $patchInfo -BranchName $patchBranchName -RemoteName $RemoteName -GroupId $GroupId
    }

    Write-Host "git -c user.name=`"azure-sdk`" -c user.email=`"azuresdk@microsoft.com`" push $RemoteName $patchBranchName"
    $cmdOutput = git -c user.name="azure-sdk" -c user.email="azuresdk@microsoft.com" push $RemoteName $patchBranchName
    if ($LASTEXITCODE -ne 0) {
      LogError "Could not push the changes to $RemoteName/$BranchName. Exiting..."
      exit $LASTEXITCODE
    }
    Write-Output "Pushed the changes to remote:$RemoteName, Branch:$BranchName"
}
finally {
    Write-Host "git checkout $CurrentBranchName"
    $cmdOutput = git checkout $CurrentBranchName
}

GenerateBOMFile -ArtifactInfos $ArtifactInfos -BomFileBranchName $bomBranchName
GenerateJsonReport -ArtifactPatchInfos $ArtifactPatchInfos -PatchBranchName $patchBranchName -BomFileBranchName $bomBranchName
#$orderedArtifacts = GetTopologicalSort -ArtifactIds $ArtifactsToPatch.Keys -ArtifactInfos $ArtifactInfos
#GenerateHtmlReport -Artifacts $orderedArtifacts -PatchBranchName $patchBranchName -BomFileBranchName $bomBranchName
