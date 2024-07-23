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
  [bool]$IsSnapshot
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
    if (!$packageDetail.GroupID) { $packageDetail.GroupID = $pomDocument.project.parent.groupId }
    if (!$packageDetail.GroupID) { throw "No GroupID found for $pomFile" }
    Write-Information "Group ID is: $($packageDetail.GroupID)"

    $packageDetail.ArtifactID = $pomDocument.project.artifactId
    if (!$packageDetail.ArtifactID) { throw "No ArtifactID found for $pomFile" }
    Write-Information "Artifact ID is: $($packageDetail.ArtifactID)"

    $packageDetail.Version = $pomDocument.project.version
    if (!$packageDetail.Version) { $packageDetail.Version = $pomDocument.project.parent.version }
    if (!$packageDetail.Version) { throw "No Version found for $pomFile" }
    Write-Information "Version is: $($packageDetail.Version)"

    $packageDetail.IsSnapshot = $packageDetail.Version.EndsWith("-SNAPSHOT")
    Write-Information "IsSnapshot is: $($packageDetail.IsSnapshot)"

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
    $baseUrl = $RepositoryUrl
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

  foreach ($artifact in $PackageDetail.AssociatedArtifacts) {
    $localFileName = $artifact.File.Name;
    $remoteHashUrl = "$packageUrl/$localFileName.$algorithm"

    Write-Information "Comparing local and remote hashes for $localFileName"
    Write-Information "  Getting remote hash"
    $response = Invoke-WebRequest -Method GET -Uri $remoteHashUrl -Headers $headers -MaximumRetryCount 3 -SkipHttpErrorCheck

    if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 300) {
      $remoteCount++

      if ($artifact.File.Extension -ieq '.jar') {
        # Because authenticode signing isn't determinsitic, we can't compare the hash of 2 separately signed jars
        Write-Information "  Remote hash of jar file esists."
        $matchCount++
      }
      else {
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
    }
    else {
      Write-Information "  Unable to retrieve remote hash for $localFileName. Http reponse code: $($response.StatusCode)"
    }
  }

  if ($remoteCount -eq 0) { return $false }
  if ($matchCount -eq $PackageDetail.AssociatedArtifacts.Length) { return $true }
  throw "Package already deployed, but with different content."
}
