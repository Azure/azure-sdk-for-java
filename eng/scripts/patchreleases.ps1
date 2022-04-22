# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

param(
    [string]$GroupId = "com.azure"
)

Write-Information "PS Script Root is: $PSScriptRoot"
$RepoRoot = Resolve-Path "${PSScriptRoot}../../.."
$CommonScriptFilePath = Join-Path $RepoRoot "eng" "common" "scripts" "common.ps1"
$BomHelpersFilePath = Join-Path $PSScriptRoot "bomhelpers.ps1"
$PatchReportFile = Join-Path $PSScriptRoot "patchreport.html"
$BomFilePath = Join-Path $RepoRoot "sdk" "boms" "azure-sdk-bom" "pom.xml"
$BomChangeLogPath = Join-Path $RepoRoot "sdk" "boms" "azure-sdk-bom" "changelog.md"
$NewBomFilePath = Join-Path $PSScriptRoot "bom.xml"
$NewBomFileReport = Join-Path $PSScriptRoot "bompom.html" 
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

function CreateDependencyXmlElement($Artifact, [xml]$Doc) {
    $xmlns = $Doc.Project.xmlns
    $xsi = $Doc.Project.xsi

    $dependency = $Doc.CreateElement("dependency", $xmlns);
    $groupId = $Doc.CreateElement("groupId", $xmlns);
    $groupId.InnerText = $Artifact.GroupId
    $cmdOutput = $dependency.AppendChild($groupId);
    $artifactId = $Doc.CreateElement("artifactId", $xmlns);
    $artifactId.InnerText = $Artifact.ArtifactId
    $cmdOutput = $dependency.AppendChild($artifactId);
    $version = $Doc.CreateElement("version", $xmlns);
    $version.InnerText = $Artifact.Version
    $cmdOutput = $dependency.AppendChild($version);

    $dependencies = $bomFileContent.GetElementsByTagName("dependencies")[0]
    $cmdOutput = $dependencies.AppendChild($dependency)
}

function TopologicalSortUtil($ArtifactId, $ArtifactInfos, $ArtifactIds, $Visited, $Order) {
    $Visited[$ArtifactId] = $true

    # Find all dependencies that are also getting patched.
    $adjDependencies = $ArtifactInfos[$ArtifactId].Dependencies.Keys | Where-Object { $ArtifactIds -Contains $_ }

    foreach($arId in $adjDependencies) {
        if(!$Visited.ContainsKey($arId)) {
            TopologicalSortUtil -ArtifactId $arId -ArtifactInfos $ArtifactInfos -ArtifactIds $ArtifactIds -Visited $Visited -Order $Order
        }
    }

    $cmdOutput = $Order.Add($ArtifactId)
}

function GetTopologicalSort($ArtifactIds, $ArtifactInfos) {
    $order = [System.Collections.ArrayList]::new()
    # $reverseOrder = @()
    $visited = [System.Collections.Hashtable]::new()
    foreach($artifactId in $ArtifactIds) {
        if(!$visited.ContainsKey($artifactId)) {
            TopologicalSortUtil -ArtifactId $artifactId -ArtifactInfos $ArtifactInfos -ArtifactIds $ArtifactIds -Visited $visited -Order $order
        }
    }
    return $order
}

# function UndoVersionClientFile() {
#     $repoRoot = Resolve-Path "${PSScriptRoot}../../.."
#     $versionClientFile = Join-Path $repoRoot "eng" "versioning" "version_client.txt"
#     $cmdOutput = git checkout $versionClientFile
# }

function GenerateBOMFile($ArtifactInfos, $ArtifactsToPatch, $BomFileBranchName) {
    $gaArtifacts = @()
    foreach($artifact in $ArtifactInfos.Values) {
        if($null -eq $ArtifactsToPatch[$artifact.ArtifactId]) {
            $gaArtifacts += @{
                GroupId = $artifact.GroupId
                ArtifactId = $artifact.ArtifactId
                Version = $artifact.LatestGAOrPatchVersion
            }
        }
    }

    foreach($patchInfo in $ArtifactsToPatch.Values) {
        $artifactInfo = $ArtifactInfos[$patchInfo]

        $gaArtifacts += @{
            GroupId = $artifactInfo.GroupId
            ArtifactId = $artifactInfo.ArtifactId
            Version = $artifactInfo.FutureReleasePatchVersion
        }
    }

    $nonBomDependencies = @('azure-cosmos-cassandra-driver-3-extensions', 'azure-cosmos-cassandra-driver-4', 'azure-cosmos-cassandra-driver-4-extensions', 'azure-cosmos-cassandra-spring-data-extensions', 'azure-cosmos-cassandra-driver-3', 'azure-core-management')
    $gaArtifacts += $patchArtifacts
    $gaArtifacts = $gaArtifacts | Where-Object {$nonBomDependencies -notcontains $_.ArtifactId} | Sort-Object -Property ArtifactId

    #Now we need to create the BOM file.
    $bomFileContent = [xml](Get-Content -Path $BomFilePath)
    $dependencyManagement = $bomFileContent.project.dependencyManagement
    $dependencies = $dependencyManagement.dependencies
    $cmdOutput = $dependencyManagement.RemoveChild($dependencies)
    $dependencies = $bomFileContent.CreateElement("dependencies", $bomFileContent.Project.xmlns);
    $cmdOutput = $dependencyManagement.AppendChild($dependencies);

    foreach($dependency in $gaArtifacts) {
        CreateDependencyXmlElement -Artifact $dependency -Doc $bomFileContent
    }

    $bomFileContent.Save($NewBomFilePath)
    $bomContentAsString = Get-Content -Path $NewBomFilePath
    $colCount = $bomContentAsString.Length
    $body = $bomContentAsString -join "`r`n" | Out-String
    $html = "<textarea rows='$colCount' cols='300' style='border:none'>" + $body + '</textarea>'
    $html | Out-File -FilePath $NewBomFileReport 

    $currentBranchName = GetCurrentBranchName
    try {
        $releaseVersion = $bomFileContent.project.version
        $patchVersion = GetPatchVersion -ReleaseVersion $releaseVersion
        $remoteName = GetRemoteName
        $branchName = "bot/bom_$patchVersion"
        $cmdOutput = git checkout -b $branchName $remoteName/main 
        $bomFileContent.Save($BomFilePath)
        git add $BomFilePath
        $content = 'Updated the dependencies of the GA libraries'
        UpdateChangeLogEntry -ChangeLogPath $BomChangeLogPath -PatchVersion $patchVersion -ArtifactId "azure-sdk-bom" -Content $content    
        git add $BomChangeLogPath
        git commit -m "Prepare BOM for release version $releaseVersion"
        git push -f $remoteName $branchName
        $BomFileBranchName = $branchName
    }
    finally {
        git checkout $currentBranchName
    }
}

function GenerateHtmlReportRow($Html, $ArtifactIds, $ReleaseBranch) {
    $index = 0
    $count = $ArtifactIds.Count
    foreach($artifactId in $ArtifactIds) {
        $cmdOutput = $Html.Add("<tr>")
        if($index++ -eq 0) {
            $cmdOutput = $Html.Add("<td  rowspan='$count'>$ReleaseBranch</td>")
        }
        $cmdOutput = $Html.Add("<td>$artifactId</td>")
        $cmdOutput = $Html.Add("</tr>")
    }
}

function GenerateHtmlReport($JsonReport, $BomFileBranchName) {
    $html = [System.Collections.ArrayList]::new()
    $cmdOutput = $html.add("<head><title>Patch Report</title></head><body><table border='1'><tr><th>Release Branch</th><th>Artifact</th><tr>")
    foreach($elem in $JsonReport) {
        GenerateHtmlReportRow -Html $html -ArtifactIds $elem.Artifacts -ReleaseBranch $elem.Branch
    }

    $cmdOutput = $html.add("<tr><td>$BomFileBranchName</td><td>azure-sdk-bom</td></tr>");
    $cmdOutput = $html.Add("</table>")
    $currentDate = Get-Date -Format "dddd MM/dd/yyyy HH:mm K"

    $cmdOutput = $html.Add("<p>Report generated on $currentDate </p>")
    $html | Out-File -FilePath $PatchReportFile -Force
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
$AzCoreVersion = "1.28.0"
$ArtifactInfos[$AzCoreArtifactId].FutureReleasePatchVersion = $AzCoreVersion
$AzCoreNettyArtifactId = "azure-core-http-netty"
$ArtifactInfos[$AzCoreNettyArtifactId].Dependencies[$AzCoreArtifactId] = $AzCoreVersion

$ArtifactsToPatch = FindAllArtifactsToBePatched -DependencyId $AzCoreArtifactId -PatchVersion $AzCoreVersion -ArtifactInfos $ArtifactInfos

$ReleaseSets = GetPatchSets -ArtifactsToPatch $ArtifactsToPatch -ArtifactInfos $ArtifactInfos
$RemoteName = GetRemoteName
$CurrentBranchName = GetCurrentBranchName
if ($LASTEXITCODE -ne 0) {
    LogError "Could not correctly get the current branch name."
    exit 1
}
# UpdateCIInformation -ArtifactsToPatch $ArtifactsToPatch.Keys -ArtifactInfos $ArtifactInfos

Write-Output "Preparing patch releases for BOM updates."
$JsonReport = @()
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
        # GeneratePatches -ArtifactPatchInfos $patchInfos -BranchName $remoteBranchName -RemoteName $RemoteName -GroupId $GroupId

        $artifactIds = @()
        $patchInfos | ForEach-Object { $artifactIds += $_.ArtifactId }
        $sortedArtifactIds = GetTopologicalSort -ArtifactIds $artifactIds -ArtifactInfos $ArtifactInfos
        $JsonReport += @{
            Artifacts = $sortedArtifactIds
            Branch = $remoteBranchName
        }

    }
    finally {
        $cmdOutput = git checkout $CurrentBranchName
    }
}

GenerateBOMFile -ArtifactInfos $ArtifactInfos -ArtifactsToPatch $ArtifactsToPatch -BomFileBranchName $BomFileBranchName
GenerateHtmlReport -JsonReport $JsonReport -BomFileBranchName $BomFileBranchName