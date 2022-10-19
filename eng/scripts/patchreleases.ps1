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
$PatchReportFile = Join-Path $PSScriptRoot "patchreport.json"
$BomFilePath = Join-Path $RepoRoot "sdk" "boms" "azure-sdk-bom" "pom.xml"
$BomChangeLogPath = Join-Path $RepoRoot "sdk" "boms" "azure-sdk-bom" "changelog.md"
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

# Get version info for all the maven artifacts under the groupId = 'com.azure'
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

# Parse the dependency information for each of the artifact from maven.
function UpdateDependencies($ArtifactInfos) {
    foreach ($artifactId in $ArtifactInfos.Keys) {
        $deps = @{}
        $sdkVersion = $ArtifactInfos[$artifactId].LatestGAOrPatchVersion
        $pomFileUri = "https://repo1.maven.org/maven2/com/azure/$artifactId/$sdkVersion/$artifactId-$sdkVersion.pom"
        $webResponseObj = Invoke-WebRequest -Uri $pomFileUri
        $dependencies = ([xml]$webResponseObj.Content).project.dependencies.dependency | Where-Object { (([String]::IsNullOrWhiteSpace($_.scope)) -or ($_.scope -eq 'compile')) }
        $dependencies | ForEach-Object { $deps[$_.artifactId] = $_.version }
        $ArtifactInfos[$artifactId].Dependencies = $deps
    }

    return
}

# Update CII information for the artifacts.
function UpdateCIInformation($ArtifactInfos) {
    foreach ($artifactId in $ArtifactInfos.Keys) {
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

# Create the forward looking graph for once the artifacts have been patched.
function CreateForwardLookingVersions($ArtifactInfos) {
    $allDependenciesWithVersion = @{}
    foreach ($arId in $ArtifactInfos.Keys) {
        foreach ($depId in $ArtifactInfos[$arId].Dependencies.Keys) {
            $depVersion = $ArtifactInfos[$arId].Dependencies[$depId]
            $currentVersion = $allDependenciesWithVersion[$depId]
            if ($null -eq $currentVersion) {
                $latestVersion = $depVersion
            }
            else {
                $orderedVersions = @($depVersion, $currentVersion) | ForEach-Object { [AzureEngSemanticVersion]::ParseVersionString($_) }
                $sortedVersions = [AzureEngSemanticVersion]::SortVersions($orderedVersions)
                if($null -eq $sortedVersions) {
                    # We currently have a bug where semantic version may have 4 values just like jackson-databind.
                    $latestVersion = $depVersion
                } else {
                $latestVersion = $sortedVersions[0].RawVersion
                }
            }
            
            $allDependenciesWithVersion[$depId] = $latestVersion
        }
    }

    return $allDependenciesWithVersion
}

# Find all the artifacts that will need to be patched based on the dependency analysis.
function FindAllArtifactsThatNeedPatching($ArtifactInfos, $AllDependenciesWithVersion) {
    foreach($arId in $ArtifactInfos.Keys) {
        $arInfo = $ArtifactInfos[$arId]
        if($arInfo.GroupId -ne 'com.azure') {
            continue;
        }

        foreach($depId in $arInfo.Dependencies.Keys) {
            $depVersion = $arInfo.Dependencies[$depId]

            if($depVersion -ne $AllDependenciesWithVersion[$depId]) {
                $currentGAOrPatchVersion = $arInfo.LatestGAOrPatchVersion
                $newPatchVersion = GetPatchVersion -ReleaseVersion $currentGAOrPatchVersion
                $arInfo.FutureReleasePatchVersion = $newPatchVersion
                $AllDependenciesWithVersion[$arId] = $newPatchVersion
            }
        }
    }
}

# Helper class that analyzes all the artifacts that need to be patched if a given artifact is patched.
function ArtifactsToPatchUtil([String] $DependencyId, [hashtable]$ArtifactInfos, $AllDependenciesWithVersion) {
    $arInfo = $ArtifactInfos[$DependencyId]
    $currentGAOrPatchVersion = $arInfo.LatestGAOrPatchVersion
    $newPatchVersion = GetPatchVersion -ReleaseVersion $currentGAOrPatchVersion
    $arInfo.FutureReleasePatchVersion = $newPatchVersion
    $AllDependenciesWithVersion[$depId] = $newPatchVersion

    foreach($arId in $ArtifactInfos.Keys) {
        $arInfo = $ArtifactInfos[$arId]
        $depVersion = $arInfo.Dependencies[$DependencyId]
        if($depVersion -and $depVersion -ne $newPatchVersion) {
            ArtifactsToPatchUtil -DependencyId $DependencyId -ArtifactInfos $ArtifactInfos -AllDependenciesWithVersion $AllDependenciesWithVersion
        }
    }
}

# Update dependencies in the version client file.
function UpdateDependenciesInVersionClient([hashtable]$ArtifactInfos, [string]$GroupId = "com.azure") {
    ## We need to update the version_client.txt to have the correct versions in place.
    foreach ($artifactId in $ArtifactInfos.Keys) {
        $newDependencyVersion = $ArtifactInfos[$artifactId].FutureReleasePatchVersion

        if (!$newDependencyVersion) {
            $newDependencyVersion = $ArtifactInfos[$artifactId].LatestGAOrPatchVersion
        }

        $currentFileVersion = $ArtifactInfos[$artifactId].CurrentPomFileVersion

        if ($newDependencyVersion) {
            $cmdOutput = SetDependencyVersion -GroupId $GroupId -ArtifactId $artifactId -Version $newDependencyVersion
            $cmdOutput = SetCurrentVersion -GroupId $GroupId -ArtifactId $artifactId -Version $currentFileVersion
        }
    }
}

# Get the release version for the next bom artifact.
function GetNextBomVersion() {
    $pkgProperties = [PackageProps](Get-PkgProperties -PackageName "azure-sdk-bom")
    $currentVersion = $pkgProperties.Version

    $patchVersion = GetPatchVersion -ReleaseVersion $currentVersion
    return $patchVersion
}

# Find the correct order in which all the artifacts need to be released.
function TopologicalSortUtil($ArtifactId, $ArtifactInfos, $ArtifactIds, $Visited, $Order) {
    $Visited[$ArtifactId] = $true

    # Find all dependencies that are also getting patched.
    $adjDependencies = $ArtifactInfos[$ArtifactId].Dependencies.Keys | Where-Object { $ArtifactIds -Contains $_ }

    foreach ($arId in $adjDependencies) {
        if (!$Visited.ContainsKey($arId)) {
            TopologicalSortUtil -ArtifactId $arId -ArtifactInfos $ArtifactInfos -ArtifactIds $ArtifactIds -Visited $Visited -Order $Order
        }
    }

    $cmdOutput = $Order.Add($ArtifactId)
}

function GetTopologicalSort($ArtifactIds, $ArtifactInfos) {
    $order = [System.Collections.ArrayList]::new()
    # $reverseOrder = @()
    $visited = [System.Collections.Hashtable]::new()
    foreach ($artifactId in $ArtifactIds) {
        if (!$visited.ContainsKey($artifactId)) {
            TopologicalSortUtil -ArtifactId $artifactId -ArtifactInfos $ArtifactInfos -ArtifactIds $ArtifactIds -Visited $visited -Order $order
        }
    }

    $pipelineOrdered = @()
    $visited = @{}

    for($i=0; $i -lt $order.Count; $i++) {
        $arId = $order[$i]
        if($null -ne $visited[$arId]) {
            continue;
        }
        
        $visited[$arId] = $true
        $pipelineName = $ArtifactInfos[$arId].PipelineName
        $pipelineOrdered += @{
            ArtifactId = $arId
            PipelineName = $pipelineName
        }

        for($j=$i; $j -lt $order.Count; $j++) {
            $curArId = $order[$j]
            if($null -eq $visited[$curArId] -and $pipelineName -eq $ArtifactInfos[$curArId].PipelineName) {
                $pipelineOrdered += @{
                    ArtifactId = $curArId
                    PipelineName = $pipelineName
                }
                $visited[$curArId] = $true
            }
        }
    }

    return $pipelineOrdered
}

# Create the dependency section for the BOM artifact.
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

# Generate BOM file for the given artifacts.
function GenerateBOMFile($ArtifactInfos, $BomFileBranchName) {
    $gaArtifacts = @()

    foreach ($artifact in $ArtifactInfos.Values) {
        $version = $artifact.LatestGAOrPatchVersion

        if ($null -eq $version) {
            $version = $artifact.FutureReleasePatchVersion
        }

        $gaArtifacts += @{
            GroupId    = $artifact.GroupId
            ArtifactId = $artifact.ArtifactId
            Version    = $version
        }
    }

    $gaArtifacts = $gaArtifacts | Sort-Object -Property ArtifactId

    #Now we need to create the BOM file.
    $bomFileContent = [xml](Get-Content -Path $BomFilePath)
    $dependencyManagement = $bomFileContent.project.dependencyManagement
    $dependencies = $dependencyManagement.dependencies
    $cmdOutput = $dependencyManagement.RemoveChild($dependencies)
    $dependencies = $bomFileContent.CreateElement("dependencies", $bomFileContent.Project.xmlns);
    $cmdOutput = $dependencyManagement.AppendChild($dependencies);

    foreach ($dependency in $gaArtifacts) {
        CreateDependencyXmlElement -Artifact $dependency -Doc $bomFileContent
    }

    $currentBranchName = GetCurrentBranchName
    try {
        UpdateDependenciesInVersionClient -ArtifactInfos $ArtifactInfos
        $releaseVersion = $bomFileContent.project.version
        $patchVersion = GetPatchVersion -ReleaseVersion $releaseVersion
        $remoteName = GetRemoteName
        Write-Host "git checkout -b $BomFileBranchName $remoteName/main "
        $cmdOutput = git checkout -b $BomFileBranchName $remoteName/main 
        $bomFileContent.Save($BomFilePath)
        Write-Host "git add $BomFilePath"
        git add $BomFilePath
        $content = GetChangeLogContentFromMessage -ContentMessage '- Updated Azure SDK dependency versions to the latest releases.'
        UpdateChangeLogEntry -ChangeLogPath $BomChangeLogPath -PatchVersion $patchVersion -ArtifactId "azure-sdk-bom" -Content $content
        GitCommit -Message "Prepare BOM for release version $releaseVersion"
        Write-Host 'git push -c user.name="azure-sdk" -c user.email="azuresdk@microsoft.com" -f $remoteName $BomFileBranchName'
        git push -c user.name="azure-sdk" -c user.email="azuresdk@microsoft.com" -f $remoteName $BomFileBranchName
    }
    finally {
        Write-Host 'git checkout $currentBranchName'
        git checkout $currentBranchName
    }
}

# Generate json report for all the artifacts that need to be patched.
function GenerateJsonReport($ArtifactPatchInfos, $PatchBranchName, $BomFileBranchName) {
    $patchReport = @{
        PathBranchName = $PatchBranchName
        ArtifactsToPatch = $ArtifactPatchInfos
    }

    $jsonReport = @{
        BomBranchName = $BomFileBranchName
        PatchReport = $patchReport
    }

    $jsonReport | ConvertTo-Json -Depth 5 | Out-File $PatchReportFile
}

# This is an HTML report for all the artifacts that are being patched.
function GenerateHtmlReport($Artifacts, $PatchBranchName, $BomFileBranchName) {
    $count = $Artifacts.Count
    $index = 0
    $html = @()
    $html += "<head><title>Patch Report</title></head><body><table border='1'><tr><th>Artifact</th><th>PipelineName</th><th>Release Branch</th><tr>"
    $pipelineCountIndex = 0
    foreach ($artifact in $Artifacts) {
        $artifactId = $artifact.ArtifactId
        $pipelineName = $artifact.PipelineName
        $pipelineNameCount = $Artifacts | Where-Object $_.PipelineName -eq $pipelineName

        $html += "<tr>"
        if ($index++ -eq 0) {
            $html += "<td  rowspan='$count'>$PatchBranchName</td>"
        }
        $html += "<td rowspan='$pipelineNameCount'>$pipelineName</td>"
        $html += "<td>$artifactId</td>"
        $html += "</tr>"
    }
    
    $html += "<tr><td>$BomFileBranchName</td><td>azure-sdk-bom</td></tr>"
    $html += "</table>"
    $currentDate = Get-Date -Format "dddd MM/dd/yyyy HH:mm K"

    $html += "<p>Report generated on $currentDate </p>"
    $html | Out-File -FilePath $PatchReportFile -Force
}

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
    Write-Host 'git checkout -b $patchBranchName $RemoteName/main'
    git checkout -b $patchBranchName $RemoteName/main
    UpdateDependenciesInVersionClient -ArtifactInfos $ArtifactInfos

    foreach ($artifactId in $ArtifactsToPatch) {
        $arInfo = $ArtifactInfos[$artifactId]
        $patchInfo = [ArtifactPatchInfo]::new()
        $patchInfo = ConvertToPatchInfo -ArInfo $arInfo
        $ArtifactPatchInfos += $patchInfo
        GeneratePatches -ArtifactPatchInfos $patchInfo -BranchName $patchBranchName -RemoteName $RemoteName -GroupId $GroupId
    }

    Write-Host 'git -c user.name="azure-sdk" -c user.email="azuresdk@microsoft.com" push $RemoteName $patchBranchName'
    $cmdOutput = git -c user.name="azure-sdk" -c user.email="azuresdk@microsoft.com" push $RemoteName $patchBranchName
    if ($LASTEXITCODE -ne 0) {
      LogError "Could not push the changes to $RemoteName/$BranchName. Exiting..."
      exit $LASTEXITCODE
    }
    Write-Output "Pushed the changes to remote:$RemoteName, Branch:$BranchName"
}
finally {
    Write-Host 'git checkout $CurrentBranchName'
    $cmdOutput = git checkout $CurrentBranchName
}

GenerateBOMFile -ArtifactInfos $ArtifactInfos -BomFileBranchName $bomBranchName
GenerateJsonReport -ArtifactPatchInfos $ArtifactPatchInfos -PatchBranchName $patchBranchName -BomFileBranchName $bomBranchName
#$orderedArtifacts = GetTopologicalSort -ArtifactIds $ArtifactsToPatch.Keys -ArtifactInfos $ArtifactInfos
#GenerateHtmlReport -Artifacts $orderedArtifacts -PatchBranchName $patchBranchName -BomFileBranchName $bomBranchName