param(
  # $(Build.SourcesDirectory) - root of the repository
  [Parameter(Mandatory=$true)][string]$SourcesDirectory,
  # The yml file whose artifacts and additionalModules lists will be updated
  [Parameter(Mandatory=$true)][string]$YmlToUpdate
)

$StartTime = $(get-date)
. "${PSScriptRoot}/../common/scripts/common.ps1"
. "${PSScriptRoot}/../common/scripts/Helpers/PSModule-Helpers.ps1"
. "${PSScriptRoot}/../scripts/patchhelpers.ps1"

# Verify that the yml file to update exists
# !Test-Path doesn't work to negate as powershell thinks that !Test-Path is a cmdlet name
if (!(Test-Path $YmlToUpdate -PathType Leaf)) {
    LogError("$YmlToUpdate is not a valid file.")
    exit 1
}

# Verify that the SourcesDirectory exists and is a directory
if (!(Test-Path $SourcesDirectory -PathType Container)) {
    LogError("$SourcesDirectory is either not a directory or does not exist.")
    exit 1
}

Install-ModuleIfNotInstalled "powershell-yaml" "0.4.1" | Import-Module

$artifactsDict = [ordered]@{}
$addModulesDict  = [ordered]@{}
$ymlFiles = Get-ChildItem -Path $SourcesDirectory -Recurse -Depth 3 -File -Filter "ci.yml"
foreach ($ymlFile in $ymlFiles) {
    if ($ymlFile.FullName.Split([IO.Path]::DirectorySeparatorChar) -contains "resourcemanagerhybrid" -or
        $ymlFile.FullName -eq $YmlToUpdate) {
        continue
    }
    $ymlContent = Get-Content $ymlFile.FullName -Raw
    $ymlObject = ConvertFrom-Yaml $ymlContent -Ordered
    # We can have ci.yml files without artifacts, eng/code-quality-reports/ci.yml is an example of this
    if ((!$ymlObject["extends"]) -or (!$ymlObject["extends"]["parameters"]) -or (!$ymlObject["extends"]["parameters"]["artifacts"])) {
        Write-Host "$($ymlFile.FullName) has no artifacts, skipping..."
        continue
    }
    $ymlContent = Get-Content $ymlFile.FullName -Raw
    $ymlObject = ConvertFrom-Yaml $ymlContent -Ordered
    $serviceDir = $ymlObject["extends"]["parameters"]["ServiceDirectory"]

    foreach ($artifact in $ymlObject["extends"]["parameters"]["artifacts"]) {
        # The artifact type from the yml object is [System.Collections.Specialized.OrderedDictionary]
        # This needs to be an ordered list, the ordering needs to be preserved so that name is first.
        if ($artifact.Contains("releaseInBatch")) {
            $artifact.Remove("releaseInBatch")
        }
        # Since we're processing a random list of artifacts from a multitude of service directories,
        # add the ServiceDirectory to each artifact's metadata. This will allow some of the scripts
        # that need the ServiceDirectory to have it readily available.
        $artifact.Add("ServiceDirectory", $serviceDir)
        $key = $artifact["groupId"] + ":" + $artifact["name"]
        $artifactsDict.Add($key, $artifact)
    }
    # If there are no additional artifacts then continue
    if (!$ymlObject["extends"]["parameters"]["AdditionalModules"]) {
        continue
    }
    foreach ($additionalModule in $ymlObject["extends"]["parameters"]["AdditionalModules"]) {
        # AdditionalModules contain things like perf libraries which have a common perf library
        # that can exist in multiple ci.yml files. Note: AdditionalModule entries do not require
        # a ServiceDirectory entry. These don't have the same level of processing, in terms of
        # validation, that a releasing library would have.
        $key = $additionalModule["groupId"] + ":" + $additionalModule["name"]
        if (!$addModulesDict.Contains($key)) {
            $addModulesDict.Add($key, $additionalModule)
        }
    }
}

# Use OrderedDictionary here for later "FindAllArtifactsThatNeedPatching"
$ArtifactInfos = New-Object System.Collections.Specialized.OrderedDictionary

Write-Host "Loading libraries from text file."

foreach ($line in Get-Content "${PSScriptRoot}/../pipelines/patch_release_client.txt") {
    if (($line) -and !($line.StartsWith("#"))) {
        $libraryId = $line.split(" ")[0]
        $groupId, $artifactId = $libraryId.split(":")
        $ArtifactInfos[$artifactId] = GetVersionInfoForMavenArtifact -ArtifactId $artifactId -GroupId $groupId
    }
}

UpdateDependencies -ArtifactInfos $ArtifactInfos

$AllDependenciesWithVersion = CreateForwardLookingVersions -ArtifactInfos $ArtifactInfos

FindAllArtifactsThatNeedPatching -ArtifactInfos $ArtifactInfos -AllDependenciesWithVersion $AllDependenciesWithVersion

$ArtifactsToPatch = $ArtifactInfos.Keys | Where-Object { $null -ne $ArtifactInfos[$_].FutureReleasePatchVersion } | ForEach-Object { $ArtifactInfos[$_].ArtifactId }

Write-Host "Searching for libraries in dictionaries:"

$newArtifacts = @()
$newAdditionalMods = @()
# This is the list of service directories for the input ProjectList. This is necessary
# to compute the AdditionalBuildOptions
$serviceDirsForProjectList = @()
foreach ($artifactToPatch in $ArtifactsToPatch) {
    $project = $ArtifactInfos[$artifactToPatch].GroupId + ":" + $artifactToPatch

    if ($artifactsDict.Contains($project)) {
        Write-Host "Found $project in dictionary"
        $newArtifacts += $artifactsDict[$project]
        if (!$serviceDirsForProjectList.Contains($artifactsDict[$project]["ServiceDirectory"])) {
            $serviceDirsForProjectList += $artifactsDict[$project]["ServiceDirectory"]
        }
    } elseif ($addModulesDict.Contains($project)) {
        Write-Host "Found $project in add mod dictionary"
        $newAdditionalMods += $addModulesDict[$project]
    } else {
        LogError("Did not find $project in dictionary")
    }
}

$ymlContent = Get-Content $YmlToUpdate -Raw
$ymlObject = ConvertFrom-Yaml $ymlContent -Ordered
$ymlObject["extends"]["parameters"]["artifacts"] = $newArtifacts
if ($newAdditionalMods.Count -gt 0) {
    if ($ymlObject["extends"]["parameters"]["AdditionalModules"]) {
        $ymlObject["extends"]["parameters"]["AdditionalModules"] = $newAdditionalMods
    } else {
        $ymlObject["extends"]["parameters"].Add("AdditionalModules", $newAdditionalMods)
    }
} else {
    # Ensure that if there was previously and AdditionalModules section from a previous
    # build, and there isn't one now, that it's removed.
    if ($ymlObject["extends"]["parameters"]["AdditionalModules"]) {
        $ymlObject["extends"]["parameters"].Remove("AdditionalModules")
    }
}

$newContent = ConvertTo-Yaml $ymlObject
Write-Host "New content for $YmlToUpdate"
Write-Host "$newContent"
Out-File -FilePath $YmlToUpdate -InputObject $newContent

$ElapsedTime = $(get-date) - $StartTime
$TotalRunTime = "{0:HH:mm:ss}" -f ([datetime]$ElapsedTime.Ticks)
Write-Host "Total run time=$($TotalRunTime)"
