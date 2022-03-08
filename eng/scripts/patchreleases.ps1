# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

param(
    [string]$GroupId = "com.azure"
)

Write-Information "PS Script Root is: $PSScriptRoot"
$RepoRoot = Resolve-Path "${PSScriptRoot}../../.."
$CommonScriptFilePath = Join-Path $RepoRoot "eng" "common" "scripts" "common.ps1"
$BomHelpersFilePath = Join-Path $PSScriptRoot "bomhelpers.ps1"
. $CommonScriptFilePath
. $BomHelpersFilePath


class ArtifactInfo {
    [string]$GroupId = "com.azure"
    [string]$ArtifactId
    [string]$ServiceDirectoryName
    [string]$ArtifactDirPath
    [string]$LatestGAOrPatchVersion
    [string]$FutureReleasePatchVersion
    [string]$CurrentPomFileVersion
    [string]$ChangeLogPath
    [string]$ReadMePath
    [string]$PipelineName
    [hashtable]$Dependencies

    ArtifactInfo([string]$ArtifactId, [string]$LatestGAOrPatchVersion) {
        $this.ArtifactId = $ArtifactId
        $this.LatestGAOrPatchVersion = $LatestGAOrPatchVersion
    }
}

function ConvertToPatchInfo([ArtifactInfo]$ArInfo) {
    $patchInfo = [ArtifactPatchInfo]::new()
    $patchInfo.ArtifactId = $ArInfo.ArtifactId
    $patchInfo.ServiceDirectoryName = $ArInfo.ServiceDirectoryName
    $patchInfo.ArtifactDirPath = $ArInfo.ArtifactDirPath
    $patchInfo.LatestGAOrPatchVersion = $ArInfo.LatestGAOrPatchVersion
    $patchInfo.CurrentPomFileVersion = $ArInfo.CurrentPomFileVersion
    $patchInfo.ChangeLogPath = $ArInfo.ChangeLogPath
    $patchInfo.ReadMePath = $ArInfo.ReadMePath
    $patchInfo.PipelineName = $ArInfo.PipelineName
    $patchInfo.FutureReleasePatchVersion = $arInfo.FutureReleasePatchVersion

    return $patchInfo    
}

function GetVersionInfoForAllMavenArtifacts([string]$GroupId = "com.azure") {
    $artifactInfos = @{}
    $azComArtifactIds = GetAllAzComClientArtifactsFromMaven -GroupId $GroupId

    foreach ($artifactId in $azComArtifactIds) {
        $info = GetVersionInfoForAnArtifactId -ArtifactId $artifactId

        $artifactId = $info.ArtifactId
        $latestGAOrPatchVersion = $info.LatestGAOrPatchVersion
        $artifactInfos[$artifactId] = [ArtifactInfo]::new($artifactId, $latestGAOrPatchVersion)
    }

    return $artifactInfos
}

function UpdateDependencies($ArtifactInfos) {
    foreach ($artifactId in $ArtifactInfos.Keys) {
        $deps = @{}
        $sdkVersion = $ArtifactInfos[$artifactId].LatestGAOrPatchVersion
        $pomFileUri = "https://repo1.maven.org/maven2/com/azure/$artifactId/$sdkVersion/$artifactId-$sdkVersion.pom"
        $webResponseObj = Invoke-WebRequest -Uri $pomFileUri
        $dependencies = ([xml]$webResponseObj.Content).project.dependencies.dependency | Where-Object { (([String]::IsNullOrWhiteSpace($_.scope)) -or ($_.scope -eq 'compile')) }
        $dependencies | Where-Object { $_.groupId -eq $GroupId } | ForEach-Object { $deps[$_.artifactId] = $_.version }
        $ArtifactInfos[$artifactId].Dependencies = $deps
    }

    return
}

function ParseCIYamlFile([string]$FileName) {
    $artifactIdToPipelineName = @{}

    $templateRegex = "\s*template:(.*)";
    $artifactsRegex = "\s+Artifacts:\s*" 
    $artifactsRegex = "\s+Artifacts:\s*"
    $artifactIdRegex = ".*name:(.*)"
    $safeNameRegex = ".*safeName:(.*)"
    $fileContent = Get-Content -Path $FileName
    $index = 0

    while ($index -lt $fileContent.Length -and ($fileContent[$index] -notmatch $templateRegex)) {
        $index += 1
    }

    if ($index -eq $fileContent.Length) {
        return
    }

    do {

        while ($index -lt $fileContent.Length -and $fileContent[$index] -notmatch $artifactsRegex ) {
            $index += 1
        }

        while ($index -lt $fileContent.Length -and $fileContent[$index] -notmatch $artifactIdRegex) {
            $index += 1
        }

        if ($index -eq $fileContent.Length) {
            return $artifactIdToPipelineName
        }

        $artifactId = $Matches[1]

        while ($index -lt $fileContent.Length -and $fileContent[$index] -notmatch $safeNameRegex) {
            $index += 1
        }

        if ($index -eq $fileContent.Length) {
            return $artifactIdToPipelineName
        }

        $artifactIdToPipelineName[$artifactId] = $Matches[1]
    } while ($index -lt $fileContent.Length)

    return $artifactIdToPipelineName
}

function UpdateCIInformation($ArtifactsToPatch, $ArtifactInfos) {
    foreach ($artifactId in $ArtifactsToPatch) {
        $arInfo = [ArtifactInfo]$ArtifactInfos[$artifactId]
        $serviceDirectory = $arInfo.ServiceDirectoryName

        if (!$serviceDirectory) {
            $pkgProperties = [PackageProps](Get-PkgProperties -PackageName $artifactId -ServiceDirectory $serviceDirectory)
            $arInfo.ServiceDirectoryName = $pkgProperties.ServiceDirectory
            $arInfo.ArtifactDirPath = $pkgProperties.DirectoryPath
            $arInfo.CurrentPomFileVersion = $pkgProperties.Version
            $arInfo.ChangeLogPath = $pkgProperties.ChangeLogPath
            $arInfo.ReadMePath = $pkgProperties.ReadMePath
        }

        $arInfo.PipelineName = GetPipelineName -ArtifactId $arInfo.ArtifactId -ArtifactDirPath $arInfo.ArtifactDirPath
    }
}

function FindAllArtifactsToBePatched([String]$DependencyId, [String]$PatchVersion, [hashtable]$ArtifactInfos) {
    $artifactsToPatch = @{}

    foreach ($id in $ArtifactInfos.Keys) {
        $arInfo = $ArtifactInfos[$id]
        $futureReleasePatchVersion = $arInfo.FutureReleasePatchVersion

        if ($futureReleasePatchVersion) {
            # This library is already being patched and hence analyzed so we don't need to analyze it again.
            if ($id -ne 'azure-core' -or $id -ne 'azure-core-http-netty') {
                continue;
            }
        }

        $depVersion = $arInfo.Dependencies[$DependencyId]
        if ($depVersion -and $depVersion -ne $PatchVersion) {
            $currentGAOrPatchVersion = $arInfo.LatestGAOrPatchVersion
            $newPatchVersion = GetPatchVersion -ReleaseVersion $currentGAOrPatchVersion
            $arInfo.FutureReleasePatchVersion = $newPatchVersion
            $artifactsToPatch[$id] = $id
            $depArtifactsToPatch = FindAllArtifactsToBePatched -DependencyId $id -PatchVersion $newPatchVersion -ArtifactInfos $ArtifactInfos
            foreach ($recArtifacts in $depArtifactsToPatch.Keys) {
                $artifactsToPatch[$recArtifacts] = $recArtifacts
            }
        }
    }

    return $artifactsToPatch
}

function GetPatchSets($artifactsToPatch, [hashtable]$ArtifactInfos) {
    $patchSets = @()

    foreach ($artifactToPatch in $artifactsToPatch.Keys) {
        $patchDependencies = @{}
        $dependencies = $artifactInfos[$artifactToPatch].Dependencies
        $dependencies.Keys | Where-Object { $null -ne $artifactsToPatch[$_] } | ForEach-Object { $patchDependencies[$_] = $_ }
        $patchDependencies[$artifactToPatch] = $artifactToPatch

        $unionSet = @{}
        $patchDependencies.Keys | ForEach-Object { $unionSet[$_] = $_ }

        $reducedPatchSets = @()
        # Add this set to the exiting sets and reduce duplicates.
        foreach ($patchSet in $patchSets) {
            $matches = $patchDependencies.Keys | Where-Object { $patchSet[$_] } | Select-Object $_ -First 1

            if ($matches) {
                $patchSet.Keys | ForEach-Object { $unionSet[$_] = $_ }
            }
            else {
                $reducedPatchSets += $patchSet
            }
        }

        $patchSets = $reducedPatchSets
        $patchSets += $unionSet
    }

    return $patchSets
}
function UpdateDependenciesInVersionClient([string]$ArtifactId, [hashtable]$ArtifactInfos, [string]$GroupId = "com.azure") {
    ## We need to update the version_client.txt to have the correct versions in place.
    $arInfo = $ArtifactInfos[$ArtifactId]
    $dependencies = $arInfo.Dependencies
    foreach ($depId in $dependencies.Keys) {
        $depArtifactInfo = $ArtifactInfos[$depId]
        $newDependencyVersion = $depArtifactInfo.FutureReleasePatchVersion

        if (!$newDependencyVersion) {
            $newDependencyVersion = $depArtifactInfo.LatestGAOrPatchVersion
        }

        if ($newDependencyVersion) {
            $cmdOutput = SetDependencyVersion -GroupId $GroupId -ArtifactId $depId -Version $newDependencyVersion
        }
    }
}
function UndoVersionClientFile() {
    $repoRoot = Resolve-Path "${PSScriptRoot}../../.."
    $versionClientFile = Join-Path $repoRoot "eng" "versioning" "version_client.txt"
    $cmdOutput = git checkout $versionClientFile
}


$ArtifactInfos = GetVersionInfoForAllMavenArtifacts -GroupId $GroupId
$IgnoreList = @(
    'azure-client-sdk-parent',
    'azure-core-parent',
    'azure-core-test',
    'azure-sdk-all',
    'azure-sdk-bom',
    'azure-sdk-parent',
    'azure-sdk-template',
    'azure-sdk-template-bom',
    'azure-data-sdk-parent',
    'azure-spring-data-cosmos',
    'azure-core-management'
)

$inEligibleKeys = $ArtifactInfos.Keys | Where-Object { !$ArtifactInfos[$_].LatestGAOrPatchVersion -or $IgnoreList -contains $_ }
$inEligibleKeys | ForEach-Object { $ArtifactInfos.Remove($_) }

UpdateDependencies -ArtifactInfos $ArtifactInfos
$AzCoreArtifactId = "azure-core"
$AzCoreVersion = $ArtifactInfos[$AzCoreArtifactId].LatestGAOrPatchVersion

# For testing only.
# $AzCoreVersion = "1.26.0"
# $ArtifactInfos[$AzCoreArtifactId].FutureReleasePatchVersion = $AzCoreVersion
# $AzCoreNettyArtifactId = "azure-core-http-netty"
# $ArtifactInfos[$AzCoreNettyArtifactId].Dependencies[$AzCoreArtifactId] = $AzCoreVersion

$ArtifactsToPatch = FindAllArtifactsToBePatched -DependencyId $AzCoreArtifactId -PatchVersion $AzCoreVersion -ArtifactInfos $ArtifactInfos
$ReleaseSets = GetPatchSets -ArtifactsToPatch $ArtifactsToPatch -ArtifactInfos $ArtifactInfos
$RemoteName = GetRemoteName
$CurrentBranchName = GetCurrentBranchName
if ($LASTEXITCODE -ne 0) {
    LogError "Could not correctly get the current branch name."
    exit 1
}
# UpdateCIInformation -ArtifactsToPatch $ArtifactsToPatch.Keys -ArtifactInfos $ArtifactInfos

$fileContent = [System.Text.StringBuilder]::new()
$fileContent.AppendLine("BranchName;ArtifactId");
Write-Output "Preparing patch releases for BOM updates."
## We now can run the generate_patch script for all those dependencies.
foreach ($patchSet in $ReleaseSets) {
    try {
        $patchInfos = [ArtifactPatchInfo[]]@()
        foreach ($artifactId in $patchSet.Keys) {
            $arInfo = $ArtifactInfos[$artifactId]
            $patchInfo = [ArtifactPatchInfo]::new()
            $patchInfo = ConvertToPatchInfo -ArInfo $arInfo
            $patchInfos += $patchInfo
            UpdateDependenciesInVersionClient -ArtifactId $artifactId -ArtifactInfos $ArtifactInfos
        }

        $remoteBranchName = GetBranchName -ArtifactId "PatchSet"
        GeneratePatches -ArtifactPatchInfos $patchInfos -BranchName $remoteBranchName -RemoteName $RemoteName -GroupId $GroupId

        $artifactIds = @()
        $patchInfos | ForEach-Object { $artifactIds += $_.ArtifactId }
        $fileContent.AppendLine("$remoteBranchName;$($artifactIds);");
    }
    finally {
        $cmdOutput = git checkout $CurrentBranchName
    }
}

New-Item -Path . -Name "ReleasePatchInfo.csv" -ItemType "file" -Value $fileContent.ToString() -Force