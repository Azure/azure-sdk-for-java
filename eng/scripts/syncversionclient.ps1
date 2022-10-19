# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

param(
    [Parameter(Mandatory = $false)][string]$GroupId = "com.azure"
)

Write-Information "PS Script Root is: $PSScriptRoot"

$RepoRoot = Resolve-Path "${PSScriptRoot}..\..\.."
$EngDir = Join-Path $RepoRoot "eng"
$EngVersioningDir = Join-Path $EngDir "versioning"
$EngCommonScriptsDir = Join-Path $EngDir "common" "scripts"

. (Join-Path $EngCommonScriptsDir common.ps1)
. (Join-Path $PSScriptRoot bomhelpers.ps1)

function UpdateDependencyVersion($ArtifactInfo, $EngSysVersionInfo) {
    $version = $ArtifactInfo.LatestGAOrPatchVersion
    $sdkName = $ArtifactInfo.ArtifactId
    $groupId = $ArtifactInfo.GroupId
    $engsysCurrentVersion = $EngSysVersionInfo.CurrentVersion

    if([String]::IsNullOrWhiteSpace($version)) {
        return
    }

    SetDependencyVersion -GroupId $groupId -ArtifactId $sdkName -Version $version
    SetCurrentVersion -GroupId $groupId -ArtifactId $sdkName -Version $engsysCurrentVersion
}

class EngSysVersionInfo{
    [String] $GroupId
    [String] $Name
    [String] $DependencyVersion
    [String] $CurrentVersion

    EngSysVersionInfo($Name, $DependencyVersion, $CurrentVersion) {
        $this.Name = $Name
        $this.DependencyVersion = $DependencyVersion
        $this.CurrentVersion = $CurrentVersion
        $this.GroupId = 'com.azure'
    }
}

function ParseVersionClientFile($GroudpId) {
    $versionClientInfo = @{}
    $versionClientFilePath = Join-Path $EngVersioningDir "version_client.txt"
    $regexPattern = "$($GroupId):(.*);(.*);(.*)"

    foreach($line in Get-Content $versionClientFilePath) {
        if($line -match $regexPattern) {
            $artifactId = $Matches.1
            $dependencyVersion = $Matches.2
            $currentVersion = $Matches.3

            $engSysVersionInfo = [EngSysVersionInfo]::new($artifactId, $dependencyVersion, $currentVersion)
            $versionClientInfo[$artifactId] = $engSysVersionInfo
        }
    }

    return $versionClientInfo

}

function SyncVersionClientFile([String]$GroupId) {
    $artifactIds = GetAllAzComClientArtifactsFromMaven -GroupId $GroupId
    $versionClientInfo = ParseVersionClientFile -GroudpId $GroupId

    foreach($artifactId in $artifactIds) {
        $artifactInfo = GetVersionInfoForAnArtifactId -ArtifactId $artifactId
        $latestPatchOrGaVersion = $ArtifactInfo.LatestGAOrPatchVersion
        
        if([String]::IsNullOrWhiteSpace($latestPatchOrGaVersion)) {
            # This library does not have a released version so we are likely good here.
            continue
        }

        $engSysArtifactInfo = $versionClientInfo[$artifactId]
        if($null -eq $engSysArtifactInfo) {
            continue
        }

        $dependencyVersion = $engSysArtifactInfo.DependencyVersion;
        if($dependencyVersion -eq $latestPatchOrGaVersion) {
            continue
        }

        UpdateDependencyVersion -ArtifactInfo $artifactInfo -EngSysVersionInfo $engSysArtifactInfo
        UpdateDependencyOfClientSDK
    }
}

# Don't call functions when the script is being dot sourced
if ($MyInvocation.InvocationName -ne ".") {
    SyncVersionClientFile -GroupId "com.azure"
}


