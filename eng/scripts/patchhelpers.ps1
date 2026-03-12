# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

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

    ArtifactInfo([string]$ArtifactId, [string]$GroupId, [string]$LatestGAOrPatchVersion) {
        $this.ArtifactId = $ArtifactId
        $this.GroupId = $GroupId
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
        $artifactInfos[$artifactId] = GetVersionInfoForMavenArtifact -ArtifactId $artifactId -GroupId $GroupId
    }

    return $artifactInfos
}

# Get version info for all a Maven artifact
function GetVersionInfoForMavenArtifact($ArtifactId, $GroupId = "com.azure") {
    $info = GetVersionInfoForAnArtifactId -GroupId $groupId -ArtifactId $artifactId
    $artifactId = $info.ArtifactId
    $latestGAOrPatchVersion = $info.LatestGAOrPatchVersion

    return [ArtifactInfo]::new($artifactId, $groupId, $latestGAOrPatchVersion)
}

# Parse the dependency information for each of the artifact from maven.
function UpdateDependencies($ArtifactInfos) {
    foreach ($artifactId in $ArtifactInfos.Keys) {
        $deps = @{}
        $sdkVersion = $ArtifactInfos[$artifactId].LatestGAOrPatchVersion
        $groupPath = $ArtifactInfos[$artifactId].GroupId -replace '\.', '/'
        $pomFileUri = "https://repo1.maven.org/maven2/$groupPath/$artifactId/$sdkVersion/$artifactId-$sdkVersion.pom"
        $webResponseObj = Invoke-WebRequest -Uri $pomFileUri -UserAgent "azure-sdk-for-java" -Headers @{ "Content-signal" = "search=yes,ai-train=no" }
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
            $pkgProperties = [PackageProps](Get-PkgProperties -PackageName $artifactId -ServiceDirectory $serviceDirectory -GroupId $arInfo.GroupId)
            $arInfo.ServiceDirectoryName = $pkgProperties.ServiceDirectory
            $arInfo.ArtifactDirPath = $pkgProperties.DirectoryPath
            $arInfo.CurrentPomFileVersion = $pkgProperties.Version
            $arInfo.ChangeLogPath = $pkgProperties.ChangeLogPath
            $arInfo.ReadMePath = $pkgProperties.ReadMePath
        }

        $arInfo.PipelineName = GetPipelineName -ArtifactId $arInfo.ArtifactId -ArtifactDirPath $arInfo.ArtifactDirPath
    }
}

# Find all the artifacts that will need to be patched based on dependency analysis.
# Iterates until no more patches are found (fixed-point), so the result is correct
# regardless of artifact ordering in patch_release_client.txt.
# Only dependencies that are themselves in the patch list are checked — external
# dependencies (reactor-core, jackson, etc.) are ignored.
function FindArtifactsThatNeedPatching($ArtifactInfos) {
    $latestVersions = @{}
    foreach ($arId in $ArtifactInfos.Keys) {
        $latestVersions[$arId] = $ArtifactInfos[$arId].LatestGAOrPatchVersion
    }

    do {
        $changed = $false
        foreach ($arId in $ArtifactInfos.Keys) {
            $arInfo = $ArtifactInfos[$arId]
            if ($arInfo.FutureReleasePatchVersion) { continue }
            foreach ($depId in $arInfo.Dependencies.Keys) {
                if (-not $latestVersions.ContainsKey($depId)) { continue }
                if ($arInfo.Dependencies[$depId] -ne $latestVersions[$depId]) {
                    $patchVersion = GetPatchVersion -ReleaseVersion $arInfo.LatestGAOrPatchVersion
                    $arInfo.FutureReleasePatchVersion = $patchVersion
                    $latestVersions[$arId] = $patchVersion
                    $changed = $true
                    break
                }
            }
        }
    } while ($changed)
}

# Update dependencies in the version client file.
function UpdateDependenciesInVersionClient([hashtable]$ArtifactInfos) {
    ## We need to update the version_client.txt to have the correct versions in place.
    foreach ($artifactId in $ArtifactInfos.Keys) {
        $newDependencyVersion = $ArtifactInfos[$artifactId].FutureReleasePatchVersion

        if (!$newDependencyVersion) {
            $newDependencyVersion = $ArtifactInfos[$artifactId].LatestGAOrPatchVersion
        }

        $currentFileVersion = $ArtifactInfos[$artifactId].CurrentPomFileVersion

        if ($newDependencyVersion) {
            $cmdOutput = SetDependencyVersion -GroupId $ArtifactInfos[$artifactId].GroupId -ArtifactId $artifactId -Version $newDependencyVersion
            $cmdOutput = SetCurrentVersion -GroupId $ArtifactInfos[$artifactId].GroupId -ArtifactId $artifactId -Version $currentFileVersion
        }
    }
}

# Get the release version for the next bom artifact.
function GetNextBomVersion() {
    $pkgProperties = [PackageProps](Get-PkgProperties -PackageName "azure-sdk-bom" -GroupId "com.azure")
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
function GenerateBOMFile($ArtifactInfos, $BomFileBranchName, [bool]$UseCurrentBranch = $false) {
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
        $base = if ($UseCurrentBranch) { "HEAD" } else { "$remoteName/main" }
        Write-Host "git checkout -b $BomFileBranchName $base"
        $cmdOutput = git checkout -b $BomFileBranchName $base
        $bomFileContent.Save($BomFilePath)
        Write-Host "git add $BomFilePath"
        git add $BomFilePath
        $content = GetChangeLogContentFromMessage -ContentMessage '- Updated Azure SDK dependency versions to the latest releases.'
        UpdateChangeLogEntry -ChangeLogPath $BomChangeLogPath -PatchVersion $patchVersion -ArtifactId "azure-sdk-bom" -Content $content
        GitCommit -Message "Prepare BOM for release version $releaseVersion"
        Write-Host "git push -c user.name=`"azure-sdk`" -c user.email=`"azuresdk@microsoft.com`" -f $remoteName $BomFileBranchName"
        git push -c user.name="azure-sdk" -c user.email="azuresdk@microsoft.com" -f $remoteName $BomFileBranchName
    }
    finally {
        Write-Host "git checkout $currentBranchName"
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
