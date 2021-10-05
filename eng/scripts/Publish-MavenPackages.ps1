param(
  [Parameter(Mandatory=$true)][string]$ArtifactDirectory,
  [Parameter(Mandatory=$true)][string]$RepositoryUrl,
  [Parameter(Mandatory=$true)][string]$RepositoryUsername,
  [Parameter(Mandatory=$true)][string]$RepositoryPassword,
  [Parameter(Mandatory=$true)][string]$GPGExecutablePath,
  [Parameter(Mandatory=$false)][switch]$StageOnly,
  [Parameter(Mandatory=$false)][switch]$ShouldPublish,
  [Parameter(Mandatory=$false)][AllowEmptyString()][string]$GroupIDFilter,
  [Parameter(Mandatory=$false)][AllowEmptyString()][string]$ArtifactIDFilter
)

$ErrorActionPreference = "Stop"

. $PSScriptRoot\MavenPackaging.ps1

# The Resolve-Path will normalize the path separators and throw if they don't exist.
# This is necessary because, the yml passes in ${{parameters.BuildToolsPath}}/ which
# on Windows means <drive>:\<BuildToolsPath>/<whatever>/<else>. Maven doesn't always
# behave well with different separators in the path.
$ArtifactDirectory = Resolve-Path $ArtifactDirectory
$GPGExecutablePath = Resolve-Path $GPGExecutablePath

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
Write-Information "Stage Only is: $StageOnly"
Write-Information "Should Publish is: $ShouldPublish"

Write-Information "Getting filtered package details."
$packageDetails = Get-FilteredMavenPackageDetails -ArtifactDirectory $ArtifactDirectory -GroupIDFilter $GroupIDFilter -ArtifactIDFilter $ArtifactIDFilter

Write-Host "Found $($packageDetails.Length) packages to publish:"
$packageDetails | % { Write-Host $_.FullyQualifiedName }

if ($packageDetails.Length -eq 0) {
  throw "Aborting, no packages to publish."
}

if ($StageOnly)
{
  foreach ($packageDetail in $packageDetails) {
    if ($packageDetail.IsSnapshot) {
      throw "Package $($packageDetail.FullyQualifiedName) is a Snapshot and StageOnly is set to 'true'. Staging of snapshot packages is not supported."
    }
  }
}

Write-Host "Starting GPG signing and publishing"

foreach ($packageDetail in $packageDetails) {
  Write-Host "GPG signing and publishing package: $($packageDetail.FullyQualifiedName)"

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

  $javadocOption = ""
  $javadocAssociatedArtifact = $packageDetail.AssociatedArtifacts | Where-Object { ($_.Classifier -eq "javadoc") -and ($_.Type -eq "jar")}
  if (-not $javadocAssociatedArtifact) {
    Write-Information "No JavaDoc artifact, omitting JavaDoc Option"
  } else {
    $javadocOption = "-Djavadoc=$($javadocAssociatedArtifact.File.FullName)"
    Write-Information "JavaDoc Option is: $javadocOption"
  }

  $sourcesOption = ""
  $sourcesAssociatedArtifact = $packageDetail.AssociatedArtifacts | Where-Object { ($_.Classifier -eq "sources") -and ($_.Type -eq "jar") }
  if (-not $sourcesAssociatedArtifact) {
    Write-Information "No Sources artifact, omitting Sources Option"
  } else {
    $sourcesOption = "-Dsources=$($sourcesAssociatedArtifact.File.FullName)"
    Write-Information "Sources Option is: $sourcesOption"
  }

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

  $shouldPublishPackage = $ShouldPublish
  $packageReposityUrl = $RepositoryUrl

  if ($packageReposityUrl -match "https://pkgs.dev.azure.com/azure-sdk/\b(internal|public)\b/*") {
    # Azure DevOps feeds don't support staging
    $shouldPublishPackage = $ShouldPublish -and !$StageOnly
    $releaseType = 'AzureDevOps'
  }
  elseif ($packageReposityUrl -like "https://oss.sonatype.org/service/local/staging/deploy/maven2/") {
    if ($packageDetail.IsSnapshot) {
      # Snapshots don't go to the standard maven central url
      $packageReposityUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
      $releaseType = 'MavenCentralSnapshot'
    }
    elseif ($StageOnly) {
      $releaseType = 'MavenCentralStaging'
    }
    else {
      $releaseType = 'MavenCentral'
    }
  }
  else {
    throw "Repository URL must be either an Azure Artifacts feed, or a SonaType Nexus feed."
  }

  #Local GPG deployment is required when we're not going to publish a package, or when we're publishing to maven central
  $requiresLocalGpg = !$shouldPublishPackage -or ($releaseType -eq 'MavenCentralStaging') -or ($releaseType -eq 'MavenCentral')

  Write-Information "Release Type: $releaseType"
  Write-Information "Should Publish Package: $shouldPublishPackage"
  Write-Information "Requires local GPG deployment: $requiresLocalGpg"

  Write-Information "Files Option is: $filesOption"
  Write-Information "Classifiers Option is: $classifiersOption"
  Write-Information "Types Option is: $typesOption"

  $gpgexeOption = "-Dgpgexe=$GPGExecutablePath"
  Write-Information "GPG Executable Option is: $gpgexeOption"

  if ($requiresLocalGpg) {
    $localRepositoryDirectory = Get-RandomRepositoryDirectory
    $localRepositoryDirectoryUri = $([Uri]$localRepositoryDirectory.FullName).AbsoluteUri
    Write-Information "Local Repository Directory URI is: $localRepositoryDirectoryUri"

    $urlOption = "-Durl=$localRepositoryDirectoryUri"
    Write-Information "URL Option is: $urlOption"

    Write-Information "Signing and deploying package to $localRepositoryDirectoryUri"
    Write-Information "mvn gpg:sign-and-deploy-file `"--batch-mode`" `"$pomOption`" `"$fileOption`" `"$javadocOption`" `"$sourcesOption`" `"$filesOption`" $classifiersOption `"$typesOption`" `"$urlOption`" `"$gpgexeOption`" `"-DrepositoryId=target-repo`" `"--settings=$PSScriptRoot\..\maven.publish.settings.xml`""
    mvn gpg:sign-and-deploy-file "--batch-mode" "$pomOption" "$fileOption" "$javadocOption" "$sourcesOption" "$filesOption" $classifiersOption "$typesOption" "$urlOption" "$gpgexeOption" "-DrepositoryId=target-repo" "--settings=$PSScriptRoot\..\maven.publish.settings.xml"
    if ($LASTEXITCODE) { exit $LASTEXITCODE }
  }

  if(!$shouldPublishPackage)
  {
    Write-Information "Skipping deployment because Should Publish Package == false."
    continue
  }

  if ($releaseType -eq 'AzureDevOps') {
    Write-Information "GPG Signing and deploying package in one step to devops feed: $packageReposityUrl"
    Write-Information "mvn gpg:sign-and-deploy-file `"--batch-mode`" `"$pomOption`" `"$fileOption`" `"$javadocOption`" `"$sourcesOption`" `"$filesOption`" $classifiersOption `"$typesOption`" `"-Durl=$packageReposityUrl`" `"$gpgexeOption`" `"-DrepositoryId=target-repo`" `"-Drepo.password=[redacted]`" `"--settings=$PSScriptRoot\..\maven.publish.settings.xml`""
    mvn gpg:sign-and-deploy-file "--batch-mode" "$pomOption" "$fileOption" "$javadocOption" "$sourcesOption" "$filesOption" $classifiersOption "$typesOption" "-Durl=$packageReposityUrl" "$gpgexeOption" "-DrepositoryId=target-repo" "-Drepo.password=$RepositoryPassword" "--settings=$PSScriptRoot\..\maven.publish.settings.xml"

    if ($LASTEXITCODE -eq 0) {
      Write-Information "Package $($packageDetail.FullyQualifiedName) deployed"
      continue
    }

    Write-Information "Release attempt $attemt exited with code $LASTEXITCODE"
    Write-Information "Checking Azure DevOps to see if release was successful"
    if (Test-ReleasedPackage -RepositoryUrl $packageReposityUrl -PackageDetail $packageDetail) {
      Write-Information "Package $($packageDetail.FullyQualifiedName) deployed despite non-zero exit code."
      continue
    }

    exit $LASTEXITCODE
  }
  elseif ($releaseType -eq 'MavenCentralSnapshot') {
    Write-Information "GPG Signing and deploying package in one step to Sonatype snapshots: $packageReposityUrl"
    Write-Information "mvn gpg:sign-and-deploy-file `"--batch-mode`" `"$pomOption`" `"$fileOption`" `"$javadocOption`" `"$sourcesOption`" `"$filesOption`" $classifiersOption `"$typesOption`" `"-Durl=$packageReposityUrl`" `"$gpgexeOption`" `"-DrepositoryId=target-repo`" `"-Drepo.username=`"`"$RepositoryUsername`"`"`" `"-Drepo.password=[redacted]`" `"--settings=$PSScriptRoot\..\maven.publish.settings.xml`""
    mvn gpg:sign-and-deploy-file "--batch-mode" "$pomOption" "$fileOption" "$javadocOption" "$sourcesOption" "$filesOption" $classifiersOption "$typesOption" "-Durl=$packageReposityUrl" "$gpgexeOption" "-DrepositoryId=target-repo" "-Drepo.username=""$RepositoryUsername""" "-Drepo.password=""$RepositoryPassword""" "--settings=$PSScriptRoot\..\maven.publish.settings.xml"
    if ($LASTEXITCODE) { exit $LASTEXITCODE }
  }
  else {
    # Maven Central Staging + optional Release
    $repositoryDirectoryOption = "-DrepositoryDirectory=$localRepositoryDirectory"
    Write-Information "Repository Directory Option is: $repositoryDirectoryOption"

    $stagingProfileIdOption = "-DstagingProfileId=$($packageDetail.SonaTypeProfileID)"
    Write-Information "Staging Profile ID Option is: $stagingProfileIdOption"

    $stagingDescriptionOption = "-DstagingDescription=$($packageDetail.FullyQualifiedName)"
    Write-Information "Staging Description Option is: $stagingDescriptionOption"

    Write-Information "Staging package to Maven Central"
    Write-Information "mvn org.sonatype.plugins:nexus-staging-maven-plugin:deploy-staged-repository `"--batch-mode`" `"-DnexusUrl=https://oss.sonatype.org`" `"$repositoryDirectoryOption`" `"$stagingProfileIdOption`" `"$stagingDescriptionOption`" `"-DrepositoryId=target-repo`" `"-DserverId=target-repo`" `"-Drepo.username=$RepositoryUsername`" `"-Drepo.password=`"[redacted]`"`" `"--settings=$PSScriptRoot\..\maven.publish.settings.xml`""
    mvn org.sonatype.plugins:nexus-staging-maven-plugin:deploy-staged-repository "--batch-mode" "-DnexusUrl=https://oss.sonatype.org" "$repositoryDirectoryOption" "$stagingProfileIdOption" "$stagingDescriptionOption" "-DrepositoryId=target-repo" "-DserverId=target-repo" "-Drepo.username=$RepositoryUsername" "-Drepo.password=""$RepositoryPassword""" "--settings=$PSScriptRoot\..\maven.publish.settings.xml"
    if ($LASTEXITCODE) { exit $LASTEXITCODE }

    Write-Information "Reading staging properties."
    $stagedRepositoryProperties = ConvertFrom-StringData (Get-Content "$localRepositoryDirectory\$($packageDetail.SonaTypeProfileID).properties" -Raw)

    $stagedRepositoryId = $stagedRepositoryProperties["stagingRepository.id"]
    Write-Information "Staging Repository ID is: $stagedRepositoryId"

    $stagedRepositoryUrl = $stagedRepositoryProperties["stagingRepository.url"]
    Write-Information "Staging Repository URL is: $stagedRepositoryUrl"

    if ($releaseType -eq 'MavenCentralStaging') {
      Write-Information "Skipping release of staging repository because Stage Only == true."
      continue
    }

    $attempt = 0
    while ($attempt++ -lt 3) {
      Write-Information "Releasing staging repostiory $stagedRepositoryId, attempt $attempt"
      Write-Information "mvn org.sonatype.plugins:nexus-staging-maven-plugin:rc-release `"-DstagingRepositoryId=$stagedRepositoryId`" `"-DnexusUrl=https://oss.sonatype.org`" `"-DrepositoryId=target-repo`" `"-DserverId=target-repo`" `"-Drepo.username=$RepositoryUsername`" `"-Drepo.password=`"`"[redacted]`"`"`" `"--settings=$PSScriptRoot\..\maven.publish.settings.xml`""
      mvn org.sonatype.plugins:nexus-staging-maven-plugin:rc-release "-DstagingRepositoryId=$stagedRepositoryId" "-DnexusUrl=https://oss.sonatype.org" "-DrepositoryId=target-repo" "-DserverId=target-repo" "-Drepo.username=$RepositoryUsername" "-Drepo.password=""$RepositoryPassword""" "--settings=$PSScriptRoot\..\maven.publish.settings.xml"

      if ($LASTEXITCODE -eq 0) {
        Write-Information "Package $($packageDetail.FullyQualifiedName) deployed"
        break
      }

      Write-Information "Release attempt $attemt exited with code $LASTEXITCODE"
      Write-Information "Checking Maven Central to see if release was successful"

      if (Test-ReleasedPackage -RepositoryUrl $packageReposityUrl -PackageDetail $packageDetail) {
        Write-Information "Package $($packageDetail.FullyQualifiedName) deployed despite non-zero exit code."
        break
      }

      if ($attempt -ge 3) {
        exit $LASTEXITCODE
      }
    }
  }
}
