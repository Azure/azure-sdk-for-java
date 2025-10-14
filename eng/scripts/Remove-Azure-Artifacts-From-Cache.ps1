# Use case:
# Given the root of the maven .m2 cache folder, clean any Azure artifacts (aka. things built
# as part of this repository) out of it.
# MavenCacheFolder - The root of the maven cache folder. Most likely would be the
#                    $(MAVEN_CACHE_FOLDER) environment variable.
#
param(
  [Parameter(Mandatory=$true)][string]$MavenCacheFolder
)

$StartTime = $(get-date)

# Any new subdirectories to clean would be added here.
$rootFolders = ("/com/azure", "/com/microsoft/azure", "/io/clientcore")

foreach ($rootFolder in $rootFolders) {
  # Determine the starting search path by joining the Maven cache folder with the specific Azure SDKs subpath.
  $searchPath = Join-Path -Path $MavenCacheFolder -ChildPath $rootFolder

  if (!(Test-Path -Path $searchPath)) {
    Write-Host "Skipping '$searchPath' as it doesn't exist."
    continue
  }

  # Find all directories that contain a "maven-metadata*" file. Maven metadata files are the source of truth for
  # what was built locally vs downloaded from Maven central. There are three types of Maven metadata files in
  # the repository:
  #
  # maven-metadata-azure-sdk-for-java.xml for dependencies from the Azure SDK for Java DevOps Artifacts.
  # maven-metadata-central.xml for dependencies from Maven central.
  # maven-metadata-local.xml for dependencies built locally.
  #
  # Select all folders that contain any of the three types of Maven metadata files, which ones the folder contains
  # will be inspected later.
  $artifactRootFolders = (Get-ChildItem -Path $searchPath -Recurse -Directory) | Where-Object { (Get-ChildItem -Path $_.FullName -Filter "maven-metadata*" | Measure-Object).Count -gt 0 }

  foreach ($artifactRootFolder in $artifactRootFolders) {
    # For each artifact root folder try to find maven-metadata-central.xml as that will determine which versions
    # of the artifact to retain in the DevOps cache.
    $mavenMetadataCentralPath = Join-Path -Path $artifactRootFolder -ChildPath "maven-metadata-central.xml"
    $mavenCentralVersions = {}
    if (Test-Path -Path $mavenMetadataCentralPath) {
      # Folder contains a 'maven-metadata-central.xml' file, parse it to determine which subfolders should be deleted.
      #
      # For example the metadata file lists azure-core 1.30.0, 1.31.0, and 1.32.0 and there are folders 1.33.0 and 1.34.0-beta.1
      # all folders will be deleted as Maven central doesn't know about those versions. Worst case, this over deletes
      # folders which is constant with the previous design where the root folders were indiscriminately cleaned, best case
      # this only deletes built from source folders.
      $metadataXml = [XML](Get-Content $mavenMetadataCentralPath)
      $mavenCentralVersions = Select-Xml -Xml $metadataXml -XPath "/metadata/versioning/versions/version" | ForEach-Object {$_.Node.InnerXml}
    } else {
      # Folder doesn't contain a 'maven-metadata-central.xml' file, delete the entire folder as it cannot be determined
      # what was built from source vs resolved from Maven central.
      Write-Host "Deleting folder '$artifactRootFolder' as it doesn't have a 'maven-metadata-central.xml' file."
      Remove-Item $artifactRootFolder -Recurse -ErrorAction Ignore
      continue
    }

    $mavenMetadataLocalPath = Join-Path -Path $artifactRootFolder -ChildPath "maven-metadata-local.xml"
    $mavenLocalVersions = {}
    if (Test-Path -Path $mavenMetadataLocalPath) {
      # Folder contains a 'maven-metadata-local.xml' file, parse it to determine which subfolders should be deleted.
      #
      # For example the metadata file lists azure-core 1.33.0 and 1.34.0-beta.1 and there are folders 1.33.0 and 1.34.0-beta.1
      # all folders will be deleted as Maven built those versions locally. Worst case, this over deletes folders which is constant
      # with the previous design where the root folders were indiscriminately cleaned, best case this only deletes built from source folders.
      $metadataXml = [XML](Get-Content $mavenMetadataLocalPath)
      $mavenLocalVersions = Select-Xml -Xml $metadataXml -XPath "/metadata/versioning/versions/version" | ForEach-Object {$_.Node.InnerXml}

      # Additionally, since we know the file exists, delete it. We don't want to cache information about what packages were built
      # locally as this could change build-to-build. For example, in one job 1.30.0 could be built locally while releasing, but
      # in the next job 1.31.0-beta.1 could be built locally as the version incremented. We don't want the file to then state both
      # 1.30.0 and 1.31.0-beta.1 were built locally.
      Write-Host "Deleting maven-metadata-local.xml '$mavenMetadataLocalPath'."
    }

    # Now loop over each directory in this package. These directories should be the versions built locally or resolved from
    # Maven central.   
    foreach ($versionFolder in (Get-ChildItem -Path $artifactRootFolder -Directory)) {
      # Both maven-metadata-central.xml and maven-metadata-local.xml are used with inverse checks as it's a possibility that
      # the project was built locally while Maven central also contains the package. This could happen when a PR has the prep
      # work for a release but doesn't pull in changes from main after the release completes. 
      #
      # Ex, 1.30.0 is being released, so the PR uses 1.30.0 as the package version, but if the PR doesn't pull from main after
      # the version increments to 1.31.0-beta.1 the job will have 1.30.0 in both maven-metadata-central.xml and maven-metadata.local.xml.
      # It's safer to over delete what will be in the cache than to have incorrect data. 
      if (!$mavenCentralVersions.Contains($versionFolder.Name)) {
        # If maven-metadata-central.xml doesn't contain the version this is an explicit indicator the package was built locally.
        Write-Host "Deleting folder '$versionFolder' as the version isn't in 'maven-metadata-central.xml'."
        Remove-Item $versionFolder -Recurse -ErrorAction Ignore
      } elseif ($mavenLocalVersions.Contains($versionFolder.Name)) {
        # If maven-metadata-local.xml contains the version this is an explicit indicator the package was built locally.
        Write-Host "Deleting folder '$versionFolder' as the version is in 'maven-metadata-local.xml'."
        Remove-Item $versionFolder -Recurse -ErrorAction Ignore
      }
    }
  }
}

Write-Host "POM files left in the cache folder"
Get-ChildItem $MavenCacheFolder -Recurse -Include *.pom | Select-Object Name

$ElapsedTime = $(get-date) - $StartTime
$TotalRunTime = "{0:HH:mm:ss}" -f ([datetime]$ElapsedTime.Ticks)
Write-Host "Total run time=$($TotalRunTime)"
