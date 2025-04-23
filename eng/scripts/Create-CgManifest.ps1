<#
.SYNOPSIS
Creates a cgmanifest file for the given projects.

.DESCRIPTION
Given the passed projects, in the form of 'groupId:artifactId', this script will create a cgmanifest file in the passed
directory. The cgmanifest file will appropriately attribute dependencies, both Maven project dependencies and Maven
plugin dependencies, to the projects passed.

.PARAMETER Projects
The projects to create the cgmanifest for, in the form of 'groupId:artifactId,groupId:artifactId,...'.

.PARAMETER OutputDirectory
The directory to create the cgmanifest file in. If not specified, the root of the repository will be used.

.PARAMETER MavenCacheFolder
The Maven cache folder to use. If not specified, the environment configuration will be used.
#>

param(
    [Parameter(Mandatory = $true)]
    [string]$Projects = "com.azure:azure-core,com.azure:azure-core-test",
    [Parameter(Mandatory = $false)]
    [string]$OutputDirectory,
    [Parameter(Mandatory = $false)]
    [string]$MavenCacheFolder
)

class MavenDependency {
    [string]$GroupId
    [string]$ArtifactId
    [string]$Version
    [bool]$DevelopmentDependency

    MavenDependency([string]$GroupId, [string]$ArtifactId, [string]$Version, [bool]$DevelopmentDependency) {
        $this.GroupId = $GroupId
        $this.ArtifactId = $ArtifactId
        $this.Version = $Version
        $this.DevelopmentDependency = $DevelopmentDependency
    }

    MavenDependency([string]$HashTableKey) {
        $parts = $HashTableKey -split ":"
        if ($parts.Count -ne 4) {
            throw "Invalid MavenDependency key format: $HashTableKey. Expected format is 'groupId:artifactId:version:<developmentDependency>'."
        }
        $this.GroupId = $parts[0]
        $this.ArtifactId = $parts[1]
        $this.Version = $parts[2]
        $this.DevelopmentDependency = [bool]::Parse($parts[3])
    }

    [string] ToHashTableKey() {
        return "$($this.GroupId):$($this.ArtifactId):$($this.Version):$($this.DevelopmentDependency)"
    }
}


function Build-CgManifestData {
    param (
        [string]$DependencyFileName,
        [hashtable]$CgManifestData,
        [string]$PomFile,
        [boolean]$IsPlugins
    )
    
    foreach ($line in (Get-Content $DependencyFileName)) {
        $line = $line.Trim()
        if ($line -notmatch "((?:[\w\.-]+:){2,}(?:[\w\.-]+))") {
            continue
        }
        $line = $Matches[1]

        $mavenDependency
        $parts = $line -split ":"
        if ($parts.Count -eq 5) {
            $mavenDependency = [MavenDependency]::new($parts[0], $parts[1], $parts[3], $parts[4] -eq "test" -or $IsPlugins)
        } elseif ($parts.Count -eq 6) {
            $mavenDependency = [MavenDependency]::new($parts[0], $parts[1], $parts[4], $parts[5] -eq "test" -or $IsPlugins)
        } else {
            continue
        }

        $key = $mavenDependency.ToHashTableKey()
        if (-not $CgManifestData.ContainsKey($key)) {
            $CgManifestData[$key] = @()
        }

        $CgManifestData[$key] += $PomFile
    }
}

$repoRoot = Resolve-Path ($PSScriptRoot + "/../..")

# Root SDK directory is two levels up from this script's directory and in the 'sdk' folder.
$sdkRoot = Join-Path $repoRoot "sdk"

# Processing location is the 'target' folder in the repo root directory.
# If the target folder does not exist, create it.
$processingLocation = Join-Path $repoRoot "target"
if (-not (Test-Path $processingLocation)) {
    New-Item -Path $processingLocation -ItemType Directory | Out-Null
}

# Process and validate the projects passed.
$projectsByGroupId = @{}
foreach ($project in ($Projects -split ",")) {
    # Split the project into groupId and artifactId.
    $projectParts = $project -split ":"
    if ($projectParts.Count -ne 2) {
        Write-Error "Invalid project format: $project. Expected format is 'groupId:artifactId'."
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

# Then from the SDK root, find all pom.xml files with a depth of 2 levels.
# The -Depth parameter limits the recursion to 2 levels.
$pomFiles = Get-ChildItem -Path $sdkRoot -Filter pom.xml -Recurse -Depth 2
$pomFilesToProcess = @()

# For each pom.xml file, get the groupdId and artifactId from the Maven xml.
foreach ($pomFile in $pomFiles) {
    $xmlContent = New-Object xml
    $xmlContent.Load($pomFile)

    # Get the groupId and artifactId from the pom.xml file.
    $groupId = $xmlContent.project.groupId
    $artifactId = $xmlContent.project.artifactId

    if ($null -eq $groupId -or $null -eq $artifactId) {
        Write-Error "Invalid pom.xml file: $pomFile. Missing groupId or artifactId."
        continue
    }

    $projectsOfGroupId = $projectsByGroupId[$groupId]
    if ($null -eq $projectsOfGroupId) {
        # groupId is not in the list of projects passed, so skip it.
        continue
    }

    if (-not $projectsOfGroupId.Contains($artifactId)) {
        # artifactId is not in the list of projects passed, so skip it.
        continue
    }

    # Add the pom.xml file to the list of files to process.
    $pomFilesToProcess += $pomFile
}

# Commands to get dependencies will be the following:
#
# Will retrieve the Maven project dependencies (both compile and test dependencies):
# mvn dependency:resolve -DexcludeTransitive=true -DoutputFile=<processing location> -f <path to project pom>
#
# Will retrieve the Maven plugin dependencies:
# mvn dependency:resolve-plugins -DexcludeTransitive=true -DoutputFile=<processing location> -f <path to project pom>
#
# -Dmaven.repo.local=$MavenCacheFolder will be added to each command if $MavenCacheFolder is specified.
#
# This script makes an assumption that the dependencies and plugin dependencies are able to be resolved from either
# the local Maven cache or the remote Maven repository. If this is not the case, the script will fail.
#
# When processing the result files, data will be bucketed by 'groupId:artifactId:version:<developmentDependency>'
# and each bucket will contain a list of project using the dependency.
# The output will be in the form of 'groupId:artifactId:version:<developmentDependency> = <project1>,<project2>,...'
#
# The processed output will then be converted to a cgmanifest file and written to the output directory with the
# name 'cgmanifest.json'.
$cgManifestData = @{}
foreach ($pomFile in $pomFilesToProcess) {
    $dependencyOutputFileName = Join-Path $processingLocation "depencies.txt"
    $pluginOutputFileName = Join-Path $processingLocation "plugins.txt"

    # Create the command to get the Maven project dependencies.
    $command = "mvn dependency:resolve -DexcludeTransitive=true -DoutputType=text -DoutputFile=$dependencyOutputFileName"
    if ($MavenCacheFolder) {
        $command += " -Dmaven.repo.local=$MavenCacheFolder"
    }
    $command += " -f $pomFile"

    # Execute the command.
    $output = Invoke-Expression $command
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to execute command: $command. Error: $output"
        return
    }

    Build-CgManifestData $dependencyOutputFileName $cgManifestData $pomFile $false

    # Create the command to get the Maven plugin dependencies.
    $command = "mvn dependency:resolve-plugins -DexcludeTransitive=true -DoutputFile=$pluginOutputFileName"
    if ($MavenCacheFolder) {
        $command += " -Dmaven.repo.local=$MavenCacheFolder"
    }
    $command += " -f $pomFile"

    # Execute the command.
    $output = Invoke-Expression $command
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to execute command: $command. Error: $output"
        return
    }

    Build-CgManifestData $pluginOutputFileName $cgManifestData $pomFile $true
}

# Create the cgmanifest file.
[System.Text.Json.Nodes.JsonObject]$cgManifestJson = [System.Text.Json.Nodes.JsonObject]::new()
$cgManifestJson["`$schema"] = "https://json.schemastore.org/component-detection-manifest.json"
$cgManifestJson["version"] = 1
$registrations = [System.Text.Json.Nodes.JsonArray]::new()

foreach ($cgManifestRegistration in $cgManifestData.GetEnumerator()) {
    $cgManifestRegistrationKey = [MavenDependency]::new($cgManifestRegistration.Key)
    $cgManifestRegistrationValue = $cgManifestRegistration.Value

    # Create the registration object.
    $registration = [System.Text.Json.Nodes.JsonObject]::new()
    $registration["component"] = [System.Text.Json.Nodes.JsonObject]::new()
    $registration["component"]["type"] = "maven"
    $registration["component"]["maven"] = [System.Text.Json.Nodes.JsonObject]::new()
    $registration["component"]["maven"]["groupId"] = $cgManifestRegistrationKey.groupId
    $registration["component"]["maven"]["artifactId"] = $cgManifestRegistrationKey.artifactId
    $registration["component"]["maven"]["version"] = $cgManifestRegistrationKey.Version
    $registration["developmentDependency"] = $cgManifestRegistrationKey.DevelopmentDependency

    $detectedComponentLocations = [System.Text.Json.Nodes.JsonArray]::new()
    $registration["detectedComponentLocations"] = $detectedComponentLocations

    foreach ($detectedComponentLocation in $cgManifestRegistrationValue) {
        $detectedComponentLocations.Add($detectedComponentLocation)
    }

    # Add the registration to the registrations array.
    $registrations.Add($registration)
}

$cgManifestJson["registrations"] = $registrations

$cgManifestFile
if ($OutputDirectory) {
    $cgManifestFile = Join-Path $OutputDirectory "cgmanifest.json"
} else {
    $cgManifestFile = Join-Path $repoRoot "cgmanifest.json"
}

$cgManifestJson.ToJsonString() | Set-Content -Path $cgManifestFile -Encoding UTF8
