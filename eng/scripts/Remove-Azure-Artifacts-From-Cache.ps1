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
$rootFolders = ("/com/azure", "/com/microsoft/azure")

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
    if (Test-Path -Path $mavenMetadataCentralPath) {
      # Folder contains a 'maven-metadata-central.xml' file, parse it to determine which subfolders should be deleted.
      #
      # For example the metadata file lists azure-core 1.30.0, 1.31.0, and 1.32.0 and there are folders 1.33.0 and 1.34.0-beta.1
      # all folders will be deleted as Maven central doesn't know about those versions. Worst case, this over deletes
      # folders which is constant with the previous design where the root folders were indiscriminately cleaned, best case
      # this only deletes built from source folders.
      $metadataXml = [XML](Get-Content $mavenMetadataCentralPath)
      $versions = Select-Xml -Xml $metadataXml -XPath "/metadata/versioning/versions/version" | ForEach-Object {$_.Node.InnerXml}
      foreach ($versionFolder in (Get-ChildItem -Path $artifactRootFolder -Directory)) {
        if (!$versions.Contains($versionFolder.Name)) {
          Write-Host "Deleting folder '$versionFolder' as the version isn't in 'maven-metadata-central.xml'."
          Remove-Item $versionFolder -Recurse -ErrorAction Ignore
        }
      }
    } else {
      # Folder doesn't contain a 'maven-metadata-central.xml' file, delete the entire folder as it cannot be determined
      # what was built from source vs resolved from Maven central.
      Write-Host "Deleting folder '$artifactRootFolder' as it doesn't have a 'maven-metadata-central.xml' file."
      Remove-Item $artifactRootFolder -Recurse -ErrorAction Ignore
    }
  }
}

Write-Host "POM files left in the cache folder"
Get-ChildItem $MavenCacheFolder -Recurse -Include *.pom | Select-Object Name

$ElapsedTime = $(get-date) - $StartTime
$TotalRunTime = "{0:HH:mm:ss}" -f ([datetime]$ElapsedTime.Ticks)
Write-Host "Total run time=$($TotalRunTime)"
