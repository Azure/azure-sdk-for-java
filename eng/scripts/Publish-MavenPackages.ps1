param(
  [Parameter(Mandatory=$true)][string]$ArtifactDirectory,
  [Parameter(Mandatory=$true)][string]$RepositoryUrl,
  [Parameter(Mandatory=$true)][string]$RepositoryUsername,
  [Parameter(Mandatory=$true)][string]$RepositoryPassword,
  [Parameter(Mandatory=$true)][string]$GPGExecutablePath,
  [Parameter(Mandatory=$false)][switch]$StageOnly,
  [Parameter(Mandatory=$false)][string]$GroupIDFilter,
  [Parameter(Mandatory=$false)][string]$ArtifactIDFilter
)

$ErrorActionPreference = "Stop"

if ((Test-Path $ArtifactDirectory) -ne $true) { throw "Artifact directory does not exist." }
if ((Test-Path $GPGExecutablePath) -ne $true) { throw "GPG executable path does not exist." }

# This class is the top level representation of a Maven package
# for this script. A MavenPackageDetail maps to one physical POM
# file and then contains a collection of associated artifacts
# which are useful when constructing the command to publish the
# artifacts via the Maven tooling.
class MavenPackageDetail {
  [System.IO.FileInfo]$File
  [string]$FullyQualifiedName
  [string]$GroupID
  [string]$ArtifactID
  [string]$Version
  [string]$SonaTypeProfileID
  [AssociatedArtifact[]]$AssociatedArtifacts
}

# This class represents a physical file on disk which is
# grouped under a specific POM file. There will be one of
# these for the javadoc file, the sources, the main jar file
# (or aar file) and POM file. If this is a POM only package
# then there will only be one associated artifact. Additionally
# if other artifacts with different classications are present
# they will also be stored in one of these instances.
class AssociatedArtifact {
  [System.IO.FileInfo]$File
  [string]$Type
  [string]$Classifier
}

# Given a Maven package, discover the associated artifacts and return them in an array. We
# do this by filtering all the artifacts in the directory by the artifactId and version prefix
# of the file to get the full set of artifacts, and then do processing on the filenames
# to figure out what the classifier (e.g. -sources, -javadoc and -uber) and type (extension).
function Get-AssociatedArtifacts([MavenPackageDetail]$PackageDetail) {
  Write-Information "Detecting associated artifacts for $($PackageDetail.FullyQualifiedName)"
  $associtedArtifactFileFilter = "$($PackageDetail.ArtifactID)-$($PackageDetail.Version)*"
  Write-Information "Search filter is: $associtedArtifactFileFilter (jar, aar and pom files only)"
  
  $associatedArtifactFiles = @(Get-ChildItem -Path $PackageDetail.File.Directory -Filter $associtedArtifactFileFilter | Where-Object { $_ -match "^*\.(jar|pom|aar|module)$" })
  Write-Information "Found $($associatedArtifactFiles.Length) possible artifacts:"

  [AssociatedArtifact[]]$associatedArtifacts = @()

  foreach ($associatedArtifactFile in $associatedArtifactFiles)
  {
    $associatedArtifact = [AssociatedArtifact]::new()
    $associatedArtifact.File = $associatedArtifactFile

    Write-Information "Processsing artifact $($associatedArtifact.File.Name) of $($PackageDetail.FullyQualifiedName)"

    $associatedArtifact.Type = $associatedArtifact.File.Extension.Replace(".", "")
    Write-Information "Type is: $($associatedArtifact.Type)"

    $artifactPrefix = "$($PackageDetail.ArtifactID)-$($PackageDetail.Version)-"
    if ($associatedArtifact.File.BaseName.Contains($artifactPrefix)) {
      $associatedArtifact.Classifier = $associatedArtifact.File.BaseName.Replace($artifactPrefix, "")
    } 
    Write-Information "Classifier is: $($associatedArtifact.Classifier)"

    $associatedArtifacts += $associatedArtifact
  }

  return $associatedArtifacts
}

# This function maps the group ID that is detected in the POM file against a
# know set of mappings to SonaType Nexus profile IDs. A profile ID is needed
# to stage a Maven package in https://oss.sonatype.org prior to being "released"
# into the public Maven Central repository (and mirrors).
#
# NOTE that the current azuresdk SonaType user identity has access to all of
# the profile Ids below for publishing. If a new profile is introduced then
# the azuresdk identity will need to be granted access to it so that it can
# publish to it.
function Get-SonaTypeProfileID([string]$GroupID) {
  $sonaTypeProfileID = switch -wildcard ($GroupID)
  {
    "com.azure*"                   { "88192f04117501" }
    "com.microsoft.azure*"         { "534d15ee3800f4" }
    "com.microsoft.rest*"          { "66eef5eb9b85bd" }
    "com.microsoft.servicefabric*" { "8acff2e04dc15e" }
    "com.microsoft.spring*"        { "615994e851c580" }
    "com.microsoft.sqlserver*"     { "2bafd8aecdb240" }
    default {
      throw "Profile ID for group ID $GroupID was not found."
    }
  }

  return $sonaTypeProfileID
}

# This function returns an array of object where each object represents a logical Maven package
# including all of its associated artifacts. It takes care of extracting out group ID, artifact ID,
# and version information, as well as providing metadata for each of the associated files including
# types and classifiers.
function Get-MavenPackageDetails([string]$ArtifactDirectory) {
  Write-Information "Searching artifact directory for POM files."
  $pomFiles = @(Get-ChildItem -Path $ArtifactDirectory -Filter *.pom -Recurse)
  Write-Information "Found $($pomFiles.Length) POM files."

  [MavenPackageDetail[]] $packageDetails = @()

  foreach ($pomFile in $pomFiles) {
    $packageDetail = [MavenPackageDetail]::new()
    $packageDetail.File = $pomFile

    Write-Information "Processing POM file: $pomFile"
    [xml]$pomDocument = Get-Content $pomFile

    $packageDetail.GroupID = $pomDocument.project.groupId    
    Write-Information "Group ID is: $($packageDetail.GroupID)"

    $packageDetail.ArtifactID = $pomDocument.project.artifactId
    Write-Information "Artifact ID is: $($packageDetail.ArtifactID)"

    $packageDetail.Version = $pomDocument.project.version
    Write-Information "Version  is: $($packageDetail.Version)"

    $packageDetail.SonaTypeProfileID = Get-SonaTypeProfileID($packageDetail.GroupID)
    Write-Information "SonaType Profile ID is: $($packageDetail.SonaTypeProfileID)"

    $packageDetail.FullyQualifiedName = "$($packageDetail.GroupID):$($packageDetail.ArtifactID):$($packageDetail.Version)"
    Write-Information "Fully-qualified name is: $($packageDetail.FullyQualifiedName)"

    $associatedArtifacts = Get-AssociatedArtifacts($packageDetail)
    $packageDetail.AssociatedArtifacts = $associatedArtifacts

    $packageDetails += $packageDetail
  }

  return $packageDetails
}

# Implements filtering logic on the set of detected packages within the artifact directory
# that is specified. In theory we could do the filtering as soon as we read the artifact ID
# and group ID from the POM file, however discovering the full set of packages accessible
# under a path may be a useful diagnostic when looking into release issues.
function Get-FilteredMavenPackageDetails([string]$ArtifactDirectory, [string]$GroupIDFilter, [string]$ArtifactIDFilter) {
  [MavenPackageDetail[]]$packageDetails = Get-MavenPackageDetails($ArtifactDirectory)
  [MavenPackageDetail[]]$filteredPackageDetails = @()

  if (($GroupIDFilter -ne "") -and ($ArtifactIDFilter -ne "")) {
    $filteredPackageDetails = @($packageDetails | Where-Object { ($_.GroupID -eq $GroupIDFilter) -and ($_.ArtifactID -eq $ArtifactIDFilter) })
  }
  elseif (($GroupIDFilter -ne "") -or ($ArtifactIDFilter -ne "")) {
    throw "You must specify both -GroupIDFilter and -ArtifactIDFilter together."
  }
  else {
    $filteredPackageDetails = $packageDetails
  }

  return $filteredPackageDetails
}

function Get-RandomRepositoryDirectory() {
  Write-Information "Getting random repository directory."
  $randomSubDirectoryName = [System.IO.Path]::GetRandomFileName()
  $randomRepositoryDirectory = New-Item -Type Directory -Path $env:TEMP -Name $randomSubDirectoryName
  Write-Information "Random repository directory is: $randomRepositoryDirectory"
  return $randomRepositoryDirectory
}

Write-Information "PS Script Root is: $PSScriptRoot"
Write-Information "ArtifactDirectory is: $ArtifactDirectory"
Write-Information "Repository URL is: $RepositoryUrl"
Write-Information "Repository Username is: $RepositoryUsername"
Write-Information "Repository Password is: [redacted]"
Write-Information "GPG Executable Path is: $GPGExecutablePath"
Write-Information "Group ID Filter is: $GroupIDFilter"
Write-Information "Artifact ID Filter is: $ArtifactIDFilter"

Write-Information "Getting filtered package details."
$packageDetails = Get-FilteredMavenPackageDetails -ArtifactDirectory $ArtifactDirectory -GroupIDFilter $GroupIDFilter -ArtifactIDFilter $ArtifactIDFilter

Write-Host "Found $($packageDetails.Length) packages to publish:"
$packageDetails | % { Write-Host $_.FullyQualifiedName }

Write-Host "Starting GPG signing and publishing"

foreach ($packageDetail in $packageDetails) {
  Write-Host "GPG signing and publishing package: $($packageDetail.FullyQualifiedName)"
  $localRepositoryDirectory = Get-RandomRepositoryDirectory
  $localRepositoryDirectoryUri = $([Uri]$localRepositoryDirectory.FullName).AbsoluteUri
  Write-Information "Local Repository Directory URI is: $localRepositoryDirectoryUri"

  $pomAssociatedArtifact = $packageDetail.AssociatedArtifacts | Where-Object { ($_.Classifier -eq $null) -and ($_.Type -eq "pom") }
  $pomOption = "-DpomFile=$($pomAssociatedArtifact.File.FullName)"
  Write-Information "POM Option is: $pomOption"

  if ($packageDetail.AssociatedArtifacts.Length -ne 1) {
    $fileAssociatedArtifact = $packageDetail.AssociatedArtifacts | Where-Object { ($_.Classifier -eq $null) -and (($_.Type -eq "jar") -or ($_.Type -eq "aar")) }
  } else {
    $fileAssociatedArtifact = $packageDetail.AssociatedArtifacts[0]
  }

  $fileOption = "-Dfile=$($fileAssociatedArtifact.File.FullName)"
  Write-Information "File Option is: $fileOption"

  $javadocAssociatedArtifact = $packageDetail.AssociatedArtifacts | Where-Object { ($_.Classifier -eq "javadoc") -and ($_.Type -eq "jar")}
  $javadocOption = "-Djavadoc=$($javadocAssociatedArtifact.File.FullName)"
  Write-Information "JavaDoc Option is: $javadocOption"

  $sourcesAssociatedArtifact = $packageDetail.AssociatedArtifacts | Where-Object { ($_.Classifier -eq "sources") -and ($_.Type -eq "jar") }
  $sourcesOption = "-Dsources=$($sourcesAssociatedArtifact.File.FullName)"
  Write-Information "Sources Option is: $sourcesOption"

  [AssociatedArtifact[]]$additionalAssociatedArtifacts = @()
  foreach ($additionalAssociatedArtifact in $packageDetail.AssociatedArtifacts) {
    if (($additionalAssociatedArtifact -ne $pomAssociatedArtifact) -and
        ($additionalAssociatedArtifact -ne $fileAssociatedArtifact) -and
        ($additionalAssociatedArtifact -ne $javadocAssociatedArtifact) -and
        ($additionalAssociatedArtifact -ne $sourcesAssociatedArtifact)) {

      Write-Information "Additional associated artifact is: $($additionalAssociatedArtifact.File.FullName)"
      $additionalAssociatedArtifacts += $additionalAssociatedArtifact
    }
  }

  if ($additionalAssociatedArtifacts -ne $null) {
    $commaDelimitedFileNames = ""
    $additionalAssociatedArtifacts | ForEach-Object { $commaDelimitedFileNames += ",$($_.File.FullName)" }
    $filesOption = "-Dfiles=$($commaDelimitedFileNames.Substring(1))"
    
    $commaDelimitedClassifiers = ""
    $additionalAssociatedArtifacts | ForEach-Object { $commaDelimitedClassifiers += ",$($_.Classifier)" }
    $classifiersOption = "-Dclassifiers=$($commaDelimitedClassifiers.Substring(1))"
    
    $commaDelimitedTypes = ""
    $additionalAssociatedArtifacts | ForEach-Object { $commaDelimitedTypes += ",$($_.Type)" }
    $typesOption = "-Dtypes=$($commaDelimitedTypes.Substring(1))"
  }
  
  Write-Information "Files Option is: $filesOption"
  Write-Information "Classifiers Option is: $classifiersOption"
  Write-Information "Types Option is: $typesOption"

  $urlOption = "-Durl=$localRepositoryDirectoryUri"
  Write-Information "URL Option is: $urlOption"

  $repositoryDirectoryOption = "-DrepositoryDirectory=$localRepositoryDirectory"
  Write-Information "Repository Directory Option is: $repositoryDirectoryOption"

  $gpgexeOption = "-Dgpgexe=$GPGExecutablePath"
  Write-Information "GPG Executable Option is: $gpgexeOption"

  $stagingProfileIdOption = "-DstagingProfileId=$($packageDetail.SonaTypeProfileID)"
  Write-Information "Staging Profile ID Option is: $stagingProfileIdOption"

  $stagingDescriptionOption = "-DstagingDescription=$($packageDetail.FullyQualifiedName)"
  Write-Information "Staging Description Option is: $stagingDescriptionOption"

  if ($RepositoryUrl -like "https://pkgs.dev.azure.com/azure-sdk/public/*") {
    Write-Information "GPG Signing and deploying package in one step to: $RepositoryUrl"
    mvn gpg:sign-and-deploy-file "--batch-mode" "$pomOption" "$fileOption" "$javadocOption" "$sourcesOption" "$filesOption" $classifiersOption "$typesOption" "-Durl=$RepositoryUrl" "$gpgexeOption" "-DrepositoryId=target-repo" "-Drepo.password=$RepositoryPassword" "--settings=$PSScriptRoot\..\maven.publish.settings.xml"
  }
  elseif ($RepositoryUrl -like "https://oss.sonatype.org/service/local/staging/deploy/maven2/") {
    Write-Information "Signing and deploying package to $localRepositoryDirectoryUri"
    mvn gpg:sign-and-deploy-file "--batch-mode" "$pomOption" "$fileOption" "$javadocOption" "$sourcesOption" "$filesOption" $classifiersOption "$typesOption" "$urlOption" "$gpgexeOption" "-DrepositoryId=target-repo" "--settings=$PSScriptRoot\..\maven.publish.settings.xml"
  
    Write-Information "Staging package to Maven Central"
    mvn org.sonatype.plugins:nexus-staging-maven-plugin:deploy-staged-repository "--batch-mode" "-DnexusUrl=https://oss.sonatype.org" "$repositoryDirectoryOption" "$stagingProfileIdOption" "$stagingDescriptionOption" "-DrepositoryId=target-repo" "-DserverId=target-repo" "-Drepo.username=$RepositoryUsername" "-Drepo.password=""$RepositoryPassword""" "--settings=$PSScriptRoot\..\maven.publish.settings.xml"

    Write-Information "Reading staging properties."
    $stagedRepositoryProperties = ConvertFrom-StringData (Get-Content "$localRepositoryDirectory\$($packageDetails.SonaTypeProfileID).properties" -Raw)
    
    $stagedRepositoryId = $stagedRepositoryProperties["stagingRepository.id"]
    Write-Information "Staging Repository ID is: $stagedRepositoryId"

    $stagedRepositoryUrl = $stagedRepositoryProperties["stagingRepository.url"]
    Write-Information "Staging Repository URL is: $stagedRepositoryUrl"

    if ($StageOnly) {
      Write-Information "Skipping release of staging repository because stage only is set to false."
    }
    else {
      Write-Information "Releasing staging repostiory $stagedRepositoryId"
      mvn org.sonatype.plugins:nexus-staging-maven-plugin:rc-release "-DstagingRepositoryId=$stagedRepositoryId" "-DnexusUrl=https://oss.sonatype.org" "-DrepositoryId=target-repo" "-DserverId=target-repo" "-Drepo.username=$RepositoryUsername" "-Drepo.password=""$RepositoryPassword""" "--settings=$PSScriptRoot\..\maven.publish.settings.xml"  
    }
  }
  else {
    throw "Repository URL must be either an Azure Artifacts feed, or a SonaType Nextus feed."
  }

}