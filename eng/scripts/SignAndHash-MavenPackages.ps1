#Requires -Version 7

param(
  [Parameter(Mandatory=$true)][string]$Path,
  [Parameter(Mandatory=$true)][string]$DestinationPath,
  [Parameter(Mandatory=$true)][string]$GPGExecutablePath,
  [Parameter(Mandatory=$false)][AllowEmptyString()][string]$GroupIDFilter,
  [Parameter(Mandatory=$false)][AllowEmptyString()][string]$ArtifactIDFilter
)

Set-StrictMode -Version 2.0

$ErrorActionPreference = "Stop"

. $PSScriptRoot\MavenPackaging.ps1

# The Resolve-Path will normalize the path separators and throw if they don't exist.
# This is necessary because, the yml passes in ${{parameters.BuildToolsPath}}/ which
# on Windows means <drive>:\<BuildToolsPath>/<whatever>/<else>. Maven doesn't always
# behave well with different separators in the path.
$Path = Resolve-Path $Path
$GPGExecutablePath = Resolve-Path $GPGExecutablePath

function Get-RandomRepositoryDirectory() {
  Write-Host "Getting random repository directory."
  $randomSubDirectoryName = [System.IO.Path]::GetRandomFileName()
  $randomRepositoryDirectory = New-Item -Type Directory -Path $env:TEMP -Name $randomSubDirectoryName
  Write-Host "Random repository directory is: $randomRepositoryDirectory"
  return $randomRepositoryDirectory
}

function ConvertTo-DeploymentDetails($PackageDetail) {
  $pomArtifact = $packageDetail.AssociatedArtifacts | Where-Object { ($null -eq $_.Classifier) -and ($_.Type -eq "pom") }

  $fileArtifact = $packageDetail.AssociatedArtifacts.Length -eq 1 `
    ? $packageDetail.AssociatedArtifacts[0] `
    : ($packageDetail.AssociatedArtifacts | Where-Object { ($null -eq $_.Classifier) -and (($_.Type -eq "jar") -or ($_.Type -eq "aar")) })

  $javadocArtifact = $packageDetail.AssociatedArtifacts | Where-Object { ($_.Classifier -eq "javadoc") -and ($_.Type -eq "jar")}

  $sourcesArtifact = $packageDetail.AssociatedArtifacts | Where-Object { ($_.Classifier -eq "sources") -and ($_.Type -eq "jar") }

  [AssociatedArtifact[]]$additionalArtifacts = $packageDetail.AssociatedArtifacts | Where-Object {
    ($_ -ne $pomArtifact) -and
    ($_ -ne $fileArtifact) -and
    ($_ -ne $javadocArtifact) -and
    ($_ -ne $sourcesArtifact)
  }

  return [ordered]@{
    FullyQualifiedName= $packageDetail.FullyQualifiedName;
    GroupId= $packageDetail.GroupId;
    ArtifactId= $packageDetail.ArtifactId;
    Version= $packageDetail.Version;
    PomArtifact= $pomArtifact;
    FileArtifact= $fileArtifact;
    JavadocArtifact= $javadocArtifact;
    SourcesArtifact= $sourcesArtifact;
    AdditionalArtifacts= $additionalArtifacts;
  }
}


Write-Host "PS Script Root is: $PSScriptRoot"
Write-Host "Path is: $Path"
Write-Host "DestinationPath is: $DestinationPath"
Write-Host "GPG Executable Path is: $GPGExecutablePath"
Write-Host "Group ID Filter is: $GroupIDFilter"
Write-Host "Artifact ID Filter is: $ArtifactIDFilter"

Write-Host "Getting filtered package details."

[array]$packageDetails = Get-FilteredMavenPackageDetails -ArtifactDirectory $Path -GroupIDFilter $GroupIDFilter -ArtifactIDFilter $ArtifactIDFilter
| ForEach-Object { ConvertTo-DeploymentDetails $_ }

Write-Host "Found $($packageDetails.Length) packages to publish:"
$packageDetails | ForEach-Object { Write-Host $_.FullyQualifiedName }

if ($packageDetails.Length -eq 0) {
  throw "Aborting, no packages to publish."
}

Write-Host "Creating destination directory $DestinationPath"
if (Test-Path $DestinationPath) {
  Remove-Item $DestinationPath -Force -Recurse | Out-Null
}
New-Item -Path $DestinationPath -Type Directory | Out-Null

$DestinationPath = Resolve-Path -Path $DestinationPath
$destinationPathUri = $([Uri]$DestinationPath).AbsoluteUri

foreach ($packageDetail in $packageDetails) {
  Write-Host "GPG signing and publishing package: $($packageDetail.FullyQualifiedName)"

  $pomOption = "-DpomFile=$($packageDetail.PomArtifact.File.FullName)"
  Write-Host "POM Option is: $pomOption"

  $fileOption = "-Dfile=$($packageDetail.FileArtifact.File.FullName)"
  Write-Host "File Option is: $fileOption"

  $javadocOption = ""
  if (-not $packageDetail.JavadocArtifact) {
    Write-Host "No JavaDoc artifact, omitting JavaDoc Option"
  } else {
    $javadocOption = "-Djavadoc=$($packageDetail.JavadocArtifact.File.FullName)"
    Write-Host "JavaDoc Option is: $javadocOption"
  }

  $sourcesOption = ""
  if (-not $packageDetail.SourcesArtifact) {
    Write-Host "No Sources artifact, omitting Sources Option"
  } else {
    $sourcesOption = "-Dsources=$($packageDetail.SourcesArtifact.File.FullName)"
    Write-Host "Sources Option is: $sourcesOption"
  }

  foreach ($additionalArtifact in $packageDetail.AdditionalArtifacts) {
    Write-Host "Additional associated artifact is: $($additionalArtifact.File.FullName)"
  }

  $commaDelimitedFileNames = ""
  $commaDelimitedClassifiers = ""
  $commaDelimitedTypes = ""

  if ($null -ne $packageDetail.AdditionalArtifacts) {
    foreach($additionalArtifact in $packageDetail.AdditionalArtifacts) {
      $commaDelimitedFileNames += ",$($additionalArtifact.File.FullName)"
      $commaDelimitedClassifiers += ",$($additionalArtifact.Classifier)"
      $commaDelimitedTypes += ",$($additionalArtifact.Type)"
    }
  }

  $filesOption = "-Dfiles=$($commaDelimitedFileNames.Substring(1))"
  $classifiersOption = "-Dclassifiers=$($commaDelimitedClassifiers.Substring(1))"
  $typesOption = "-Dtypes=$($commaDelimitedTypes.Substring(1))"

  Write-Host "Files Option is: $filesOption"
  Write-Host "Classifiers Option is: $classifiersOption"
  Write-Host "Types Option is: $typesOption"

  $gpgexeOption = "-Dgpgexe=$GPGExecutablePath"
  Write-Host "GPG Executable Option is: $gpgexeOption"

  $urlOption = "-Durl=$destinationPathUri"
  Write-Host "URL Option is: $urlOption"

  $settingsOption = "--settings=$(Join-Path $PSScriptRoot '..' 'maven.publish.settings.xml' -Resolve)"
  Write-Host "Settings Option is: $settingsOption"

  Write-Host ""
  Write-Host "Signing package"
  
  Write-Host @"
  mvn gpg:sign-and-deploy-file "--batch-mode" "-Daether.checksums.algorithms=SHA-256,MD5,SHA-1" "$pomOption" "$fileOption" "$javadocOption" "$sourcesOption" "$filesOption" "$classifiersOption" "$typesOption" "$urlOption" "$gpgexeOption" "-DrepositoryId=target-repo" "$settingsOption"
"@
  
  mvn gpg:sign-and-deploy-file "--batch-mode" "-Daether.checksums.algorithms=SHA-256,MD5,SHA-1" "$pomOption" "$fileOption" "$javadocOption" "$sourcesOption" "$filesOption" "$classifiersOption" "$typesOption" "$urlOption" "$gpgexeOption" "-DrepositoryId=target-repo" "$settingsOption"
  
  if ($LASTEXITCODE) { exit $LASTEXITCODE }

  $groupId = $packageDetail.GroupId
  $artifactId = $packageDetail.ArtifactId
  $version = $packageDetail.Version

  $relativePath = "$($groupId.Replace('.', '/'))/$artifactId/$version"
  $signedArtifactPath = Join-Path $DestinationPath $relativePath

  if (-not (Test-Path $signedArtifactPath)) {
    Write-Error "Unable to located expected gpg output folder $signedArtifactPath"
    exit 1
  }

  Write-Host "##vso[task.setvariable variable=packageLocation;isoutput=true]$signedArtifactPath"
  Write-Host ""
}

exit 0
