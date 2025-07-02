# Maven requires the following 4 artifacts to publish
# 1. The library's pom file
# 2. The library's jar file
# 3. The sources jar file
# 4. The javadoc jar file
# The changelog.md and readme.md are also published, if they exist, but they aren't required. Further, the
# changelog.md and readme.md are verified elsewhere and are also affected by YML variables as to whether
# or not they even need to be verified. Because they aren't mandatory, they will not be verified here.
param(
  [Parameter(Mandatory=$true)][string]$BuildOutputDirectory,
  # ArtifactsList will be using ('${{ convertToJson(parameters.Artifacts) }}' | ConvertFrom-Json | Select-Object name, groupId)
  [Parameter(Mandatory=$false)][array] $ArtifactsList,
  [Parameter(Mandatory=$false)][string] $PackageInfoDir = $null
)
. "${PSScriptRoot}/../common/scripts/common.ps1"

Write-Host "BuildOutputDirectory=$($BuildOutputDirectory)"
Write-Host "BuildOutputDirectory contents"
# Capture the names of the files in the $BuildOutputDirectory
$files = Get-ChildItem -Path $BuildOutputDirectory -Recurse -Name
# Print the files to the console for debugging purposes
$files | Out-Host
# Filter the files to only include those that are relevant for Maven publishing
# This file list will then be used further down to verify that all the required files
# for either the passed $ArtifactsList or the PackageInfoDir are present.
$files = $files | Where-Object { $_.EndsWith(".pom") -or $_.EndsWith(".jar") -or $_.EndsWith("-javadoc.jar") -or $_.EndsWith("-sources.jar") }

if (-not $ArtifactsList) {
  $ArtifactsList = @()
}

if ($ArtifactsList.Count -eq 0) {
  if (-not $PackageInfoDir -or (-not (Test-Path -Path $PackageInfoDir))) {
    LogError "ArtifactsList was empty and PackageInfoDir was null or incorrect."
    exit(1)
  }
  Write-Host "Artifacts List was empty, getting Artifacts from PackageInfoDir=$PackageInfoDir"
  [array]$packageInfoFiles = Get-ChildItem -Path $PackageInfoDir "*.json"
  foreach($packageInfoFile in $packageInfoFiles) {
    $packageInfoJson = Get-Content $packageInfoFile -Raw
    $packageInfo = ConvertFrom-Json $packageInfoJson
    $ArtifactsList += New-Object PSObject -Property @{
                          groupId = $packageInfo.Group
                          name    = $packageInfo.ArtifactName
                      }

  }
}

Write-Host ""
Write-Host "ArtifactsList:"
$ArtifactsList | Format-Table -Property GroupId, Name | Out-String | Write-Host

# These are the absolute minimum of the files required to publish to Maven
$requiredFileTypes = (".pom", ".jar", "-javadoc.jar", "-sources.jar")

# The expected format for a dependency, as found in the eng\versioning\version_*.txt files, is as follows:
# groupId:artifactId;dependency-version;current-version
class Dependency {
  [string]$id
  [string]$depVer
  [string]$curVer
  Dependency(
      [string]$inputString
  ){
    $split = $inputString.Split(";")
    if (($split.Count -ne 3) -and ($split.Count -ne 2))
    {
        # throw and let the caller handle the error since it'll have access to the
        # filename of the file with the malformed line for reporting
        throw
    }
    $this.id = $split[0]
    $this.depVer = $split[1]
    if ($split.Count -eq 3)
    {
        $this.curVer = $split[2]
    }
  }
}

function Build-Dependency-Hash-From-File {
  param(
      [hashtable]$depHash,
      [string]$depFile)
  foreach($line in Get-Content $depFile)
  {
    if (!$line -or $line.Trim() -eq '' -or $line.StartsWith("#"))
    {
      continue
    }
    try {
      [Dependency]$dep = [Dependency]::new($line)
      if ($depHash.ContainsKey($dep.id))
      {
        Write-Host "Error: Duplicate dependency encountered. '$($dep.id)' defined in '$($depFile)' already exists in the dependency list which means it is defined in multiple version_*.txt files."
        continue
      }
      $depHash.Add($dep.id, $dep)
    }
    catch {
      Write-Host "Invalid dependency line='$($line) in file=$($depFile)"
    }
  }
}

$Path = Resolve-Path ($PSScriptRoot + "/../../")
$libHash = @{}
Build-Dependency-Hash-From-File $libHash $Path\eng\versioning\version_client.txt
$foundError = $false

# Check for the existence of the files in the build directory. The resulting built artifacts
# will have the format artifactId-version[.pom|.jar|-javadoc.jar|-sources.jar]. Unfortunately,
# we can't * match away the version because things like azure-core-amqp would also end up
# matching azure-core*. Load the version files to get the current version of the libraries and
# construct the filenames to verify using that.
foreach($artifact in $ArtifactsList) {
  $libHashKey = "$($artifact.groupId):$($artifact.name)"
  foreach ($fileType in $requiredFileTypes) {
    $fileName = "$($artifact.name)-$($libHash[$libHashKey].curVer)$($fileType)"
    # Only need to check for a file ending with the $fileName.
    # A previous version of this script used Get-ChildItem with a check for $fileName.
    # That was extremely inefficient as the $BuildOutputDirectory can contain thousands of files.
    # Using Where-Object to filter the files is much more efficient.
    $file = $files | Where-Object { $_.EndsWith($fileName) }
    if (!$file) {
      $foundError = $true
      LogError "Required file, $fileName, was not produced with the build."
    }
  }
}

if ($foundError) {
  LogError "One or more required Maven Artifacts were not produced with the build. If the missing files were javadoc.jar or sources.jar please contact the Azure SDK EngSys team through email or their Teams Channel (azuresdkengsysteam@microsoft.com or Azure SDK > Engineering System on teams) for assistance."
  exit(1)
} else {
  Write-Host "Success! All Maven required artifacts has been produced."
}
