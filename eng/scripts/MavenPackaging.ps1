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

  $associatedArtifactFiles = @(Get-ChildItem -Path $PackageDetail.File.Directory -Filter $associtedArtifactFileFilter | Where-Object { $_ -match "^*\.(jar|pom|aar|module|md)$" })
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
    "com.azure*"                     { "88192f04117501" }
    "com.microsoft"                  { "108eda13eb3910" }
    "com.microsoft.azure*"           { "534d15ee3800f4" }
    "com.microsoft.commondatamodel*" { "36ba35bb1eff8"  }
    "com.microsoft.rest*"            { "66eef5eb9b85bd" }
    "com.microsoft.servicefabric*"   { "8acff2e04dc15e" }
    "com.microsoft.spring*"          { "615994e851c580" }
    "com.microsoft.sqlserver*"       { "2bafd8aecdb240" }
    "com.windowsazure*"              { "222b383b84716"  }
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

# Compare the contents of SHA hash files for a local repository to the same
# files in Maven Central or Azure DevOps. Returns $false if none of the hash
# files exist in the remote, $true if all hashes exist and match the local
# hash, otherwise throws.
function Test-ReleasedPackage([string]$RepositoryUrl, [MavenPackageDetail]$PackageDetail, [string]$BearerToken) {
  if ($RepositoryUrl -match "^https://pkgs.dev.azure.com/azure-sdk/\b(internal|public)\b/*") {
    if (!$BearerToken) {
      throw "BearerToken required for Azure DevOps package feeds"
    }
    
    $baseUrl = "https://pkgs.dev.azure.com/azure-sdk/internal/_packaging/azure-sdk-for-java-pr/maven/v1"
    $algorithm = "sha256"
    $headers = @{ Authorization="BEARER $BearerToken" }
  }
  elseif ($RepositoryUrl -match "^https://oss.sonatype.org/service/local/staging/deploy/maven2") {
    $baseUrl = "https://repo1.maven.org/maven2"
    $algorithm = "sha1"
    $headers = @{ }
  }
  else {
    throw "Repository URL must be either an Azure Artifacts feed, or a SonaType Nexus feed."
  }

  $packageUrl = "$baseUrl/$($PackageDetail.GroupId.Replace('.', '/'))/$($PackageDetail.ArtifactID)/$($PackageDetail.Version)"
  
  # Count the number of remote hashes found
  $remoteCount = 0

  # Count the number of remote hashes that match their local hash
  $matchCount = 0

  foreach($artifact in $PackageDetail.AssociatedArtifacts) {
    $localFileName = $artifact.File.Name;
    $remoteHashUrl = "$packageUrl/$localFileName.$algorithm"

    Write-Information "Comparing local and remote hashes for $localFileName"
    Write-Information "  Getting remote hash"
    $response = Invoke-WebRequest -Method GET -Uri $remoteHashUrl -Headers $headers -MaximumRetryCount 3 -SkipHttpErrorCheck

    if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 300) {
      $remoteCount++
      $remoteHash = $response.Content

      Write-Information "  Getting local hash"
      $localPath = $artifact.File.FullName
      $localHash = Get-FileHash -Path $localPath -Algorithm $algorithm | Select-Object -ExpandProperty 'Hash'
      
      if ($remoteHash -eq $localHash) {
        $matchCount++
        Write-Information "  Remote $remoteHash == Local $localHash"
      }
      else {
        Write-Information "  Remote $remoteHash != Local $localHash"
      }
    }
    else {
      Write-Information "  Unable to retrieve remote hash for $localFileName. Http reponse code: $($response.StatusCode)"
    }
  }

  if($remoteCount -eq 0) { return $false }
  if($matchCount -eq $PackageDetail.AssociatedArtifacts.Length) { return $true }
  throw "Package already deployed, but with different content."
}
