<#
.SYNOPSIS
Prepares a pipeline run for running Component Detection.

.DESCRIPTION
Prepares a pipeline to run Component Detection by deleting all 'pom.xml' files that weren't part of the pipeline run.

Deleting these POMs will reduce what Component Detection attempts to scan, as many (or most), won't be built and would
likely cause the Maven commands that Component Detection runs to fail.

.PARAMETER Projects
The projects ran as part of the pipeline, in the form of 'groupId:artifactId,groupId:artifactId,...'.
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$Projects,
    [Parameter(Mandatory = $false)]
    [boolean]$DryRun = $false
)

$repoRoot = Resolve-Path ($PSScriptRoot + "/../../..")

# Delete the root pom.xml and /samples.
if (-not $DryRun) {
    Remove-Item -Path (Join-Path $repoRoot "pom.xml") -Force
    Remove-Item -Path (Join-Path $repoRoot "samples" "azure-samples-graalvm-spring-storageexplorer" "pom.xml") -Force
} else {
    Write-Host "Would delete $(Join-Path $repoRoot "pom.xml")"
    Write-Host "Would delete $(Join-Path $repoRoot "samples" "azure-samples-graalvm-spring-storageexplorer" "pom.xml")"
}

# Root SDK directory is in the 'sdk' folder.
$sdkRoot = Join-Path $repoRoot "sdk"
$parentPomsDir = Join-Path $sdkRoot "parents"

# Process and validate the projects passed.
$projectsByGroupId = @{}
foreach ($project in ($Projects -split ",")) {
    # Split the project into groupId and artifactId.
    $projectParts = $project -split ":"
    if ($projectParts.Count -ne 2) {
        Write-Host "Invalid project format: $project. Expected format is 'groupId:artifactId'."
        return
    }

    # Trim whitespace from groupId and artifactId.
    $groupId = $projectParts[0].Trim()
    $artifactId = $projectParts[1].Trim()

    # Add the project to the hashtable.
    if (-not $projectsByGroupId.ContainsKey($groupId)) {
        $projectsByGroupId[$groupId] = @()
    }
    $projectsByGroupId[$groupId] += $artifactId
}

# Then from the SDK root, find all pom.xml files.
$pomFiles = Get-ChildItem -Path $sdkRoot -Filter pom.xml -Recurse

# Iterate through all pom.xml files found and delete all that aren't part of the $Projects passed.
foreach ($pomFile in $pomFiles) {
    if ($pomFile.Directory.FullName.Contains($parentPomsDir)) {
        # Skip parent POMs.
        continue
    }

    $xmlContent = New-Object xml
    $xmlContent.Load($pomFile)

    # Get the groupId and artifactId from the pom.xml file.
    $groupId = $xmlContent.project.groupId
    if ($null -eq $groupId -and $null -ne $xmlContent.project.parent) {
        $groupId = $xmlContent.project.parent.groupId
    }

    $artifactId = $xmlContent.project.artifactId

    if ($null -eq $groupId -or $null -eq $artifactId) {
        Write-Host "Invalid pom.xml file: $pomFile. Missing groupId or artifactId."
        continue
    }

    $projectsOfGroupId = $projectsByGroupId[$groupId]
    if ($null -eq $projectsOfGroupId) {
        # groupId is not in the list of projects passed, so delete it.
        if (-not $DryRun) {
            Remove-Item -Path $pomFile -Force
        } else {
            Write-Host "Would delete $pomFile"
        }
    } elseif (-not $projectsOfGroupId.Contains($artifactId)) {
        # artifactId is not in the list of projects passed, so delete it.
        if (-not $DryRun) {
            Remove-Item -Path $pomFile -Force
        } else {
            Write-Host "Would delete $pomFile"
        }
        continue
    }
}
