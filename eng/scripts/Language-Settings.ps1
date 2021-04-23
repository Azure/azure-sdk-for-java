$Language = "java"
$LanguageDisplayName = "Java"
$PackageRepository = "Maven"
$packagePattern = "*.pom"
$MetadataUri = "https://raw.githubusercontent.com/Azure/azure-sdk/master/_data/releases/latest/java-packages.csv"
$BlobStorageUrl = "https://azuresdkdocs.blob.core.windows.net/%24web?restype=container&comp=list&prefix=java%2F&delimiter=%2F"
$MavenDownloadSite = "https://repo1.maven.org/maven2"
$ignorePackagePath = "eng/docms-ignore-packages.txt"

function Get-java-PackageInfoFromRepo ($pkgPath, $serviceDirectory)
{
  $projectPath = Join-Path $pkgPath "pom.xml"
  if (Test-Path $projectPath)
  {
    $projectData = New-Object -TypeName XML
    $projectData.load($projectPath)

    if ($projectData.project.psobject.properties.name -notcontains "artifactId" -or !$projectData.project.artifactId) {
      Write-Host "$projectPath doesn't have a defined artifactId so skipping this pom."
      return $null
    }

    if ($projectData.project.psobject.properties.name -notcontains "version" -or !$projectData.project.version) {
      Write-Host "$projectPath doesn't have a defined version so skipping this pom."
      return $null
    }

    if ($projectData.project.psobject.properties.name -notcontains "groupid" -or !$projectData.project.groupId) {
      Write-Host "$projectPath doesn't have a defined groupId so skipping this pom."
      return $null
    }

    $projectPkgName = $projectData.project.artifactId
    $pkgVersion = $projectData.project.version
    $pkgGroup = $projectData.project.groupId

    $pkgProp = [PackageProps]::new($projectPkgName, $pkgVersion.ToString(), $pkgPath, $serviceDirectory, $pkgGroup)
    if ($projectPkgName -match "mgmt" -or $projectPkgName -match "resourcemanager")
    {
      $pkgProp.SdkType = "mgmt"
    }
    elseif ($projectPkgName -match "spring")
    {
      $pkgProp.SdkType = "spring"
    }
    else
    {
      $pkgProp.SdkType = "client"
    }
    $pkgProp.IsNewSdk = $False
    if ($pkgGroup) {
      $pkgProp.IsNewSdk = $pkgGroup.StartsWith("com.azure")
    }
    $pkgProp.ArtifactName = $projectPkgName
    return $pkgProp
  }
  return $null
}

# Returns the maven (really sonatype) publish status of a package id and version.
function IsMavenPackageVersionPublished($pkgId, $pkgVersion, $groupId)
{
  try
  {
    $uri = "https://oss.sonatype.org/content/repositories/releases/$groupId/$pkgId/$pkgVersion/$pkgId-$pkgVersion.pom"
    $pomContent = Invoke-RestMethod -MaximumRetryCount 3 -RetryIntervalSec 10 -Method "GET" -uri $uri

    if ($pomContent -ne $null -or $pomContent.Length -eq 0)
    {
      return $true
    }
    else
    {
      return $false
    }
  }
  catch
  {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $statusDescription = $_.Exception.Response.StatusDescription

    # if this is 404ing, then this pkg has never been published before
    if ($statusCode -eq 404) {
      return $false
    }

    Write-Host "VersionCheck to maven for packageId $pkgId failed with statuscode $statusCode"
    Write-Host $statusDescription
    exit(1)
  }
}

# Parse out package publishing information given a maven POM file
function Get-java-PackageInfoFromPackageFile ($pkg, $workingDirectory)
{
  [xml]$contentXML = Get-Content $pkg

  $pkgId = $contentXML.project.artifactId
  $docsReadMeName = $pkgId -replace "^azure-" , ""
  $pkgVersion = $contentXML.project.version
  $groupId = if ($contentXML.project.groupId -eq $null) { $contentXML.project.parent.groupId } else { $contentXML.project.groupId }
  $releaseNotes = ""
  $readmeContent = ""

  # if it's a snapshot. return $null (as we don't want to create tags for this, but we also don't want to fail)
  if ($pkgVersion.Contains("SNAPSHOT")) {
    return $null
  }

  $changeLogLoc = @(Get-ChildItem -Path $pkg.DirectoryName -Recurse -Include "$($pkg.Basename)-changelog.md")[0]
  if ($changeLogLoc) {
    $releaseNotes = Get-ChangeLogEntryAsString -ChangeLogLocation $changeLogLoc -VersionString $pkgVersion
  }

  $readmeContentLoc = @(Get-ChildItem -Path $pkg.DirectoryName -Recurse -Include "$($pkg.Basename)-readme.md")[0]
  if ($readmeContentLoc) {
    $readmeContent = Get-Content -Raw $readmeContentLoc
  }

  return New-Object PSObject -Property @{
    PackageId      = $pkgId
    GroupId        = $groupId
    PackageVersion = $pkgVersion
    ReleaseTag     = "$($pkgId)_$($pkgVersion)"
    Deployable     = $forceCreate -or !(IsMavenPackageVersionPublished -pkgId $pkgId -pkgVersion $pkgVersion -groupId $groupId.Replace(".", "/"))
    ReleaseNotes   = $releaseNotes
    ReadmeContent  = $readmeContent
    DocsReadMeName = $docsReadMeName
  }
}

# Stage and Upload Docs to blob Storage
function Publish-java-GithubIODocs ($DocLocation, $PublicArtifactLocation)
{
  $PublishedDocs = Get-ChildItem "$DocLocation" | Where-Object -FilterScript {$_.Name.EndsWith("-javadoc.jar")}
  foreach ($Item in $PublishedDocs)
  {
    $UnjarredDocumentationPath = ""
    try
    {
      $PkgName = $Item.BaseName
      # The jar's unpacking command doesn't allow specifying a target directory
      # and will unjar all of the files in whatever the current directory is.
      # Create a subdirectory to unjar into, set the location, unjar and then
      # set the location back to its original location.
      $UnjarredDocumentationPath = Join-Path -Path $DocLocation -ChildPath $PkgName
      New-Item -ItemType directory -Path "$UnjarredDocumentationPath"
      $CurrentLocation = Get-Location
      Set-Location $UnjarredDocumentationPath
      jar -xf "$($Item.FullName)"
      Set-Location $CurrentLocation

      # If javadocs are produced for a library with source, there will always be an
      # index.html. If this file doesn't exist in the UnjarredDocumentationPath then
      # this is a sourceless library which means there are no javadocs and nothing
      # should be uploaded to blob storage.
      $IndexHtml = Join-Path -Path $UnjarredDocumentationPath -ChildPath "index.html"
      if (!(Test-Path -path $IndexHtml))
      {
        Write-Host "$($PkgName) does not have an index.html file, skippping."
        continue
      }

      # Get the POM file for the artifact we're processing
      $PomFile = $Item.FullName.Substring(0,$Item.FullName.LastIndexOf(("-javadoc.jar"))) + ".pom"
      Write-Host "PomFile $($PomFile)"

      # Pull the version from the POM
      [xml]$PomXml = Get-Content $PomFile
      $Version = $PomXml.project.version
      $ArtifactId = $PomXml.project.artifactId

      Write-Host "Start Upload for $($PkgName)/$($Version)"
      Write-Host "DocDir $($UnjarredDocumentationPath)"
      Write-Host "PkgName $($ArtifactId)"
      Write-Host "DocVersion $($Version)"
      $releaseTag = RetrieveReleaseTag $PublicArtifactLocation
      Upload-Blobs -DocDir $UnjarredDocumentationPath -PkgName $ArtifactId -DocVersion $Version -ReleaseTag $releaseTag

    }
    Finally
    {
      if (![string]::IsNullOrEmpty($UnjarredDocumentationPath))
      {
        if (Test-Path -Path $UnjarredDocumentationPath)
        {
          Write-Host "Cleaning up $UnjarredDocumentationPath"
          Remove-Item -Recurse -Force $UnjarredDocumentationPath
        }
      }
    }
  }
}

function Get-java-GithubIoDocIndex()
{
  # Update the main.js and docfx.json language content
  UpdateDocIndexFiles -appTitleLang "Java"
  # Fetch out all package metadata from csv file.
  $metadata = Get-CSVMetadata -MetadataUri $MetadataUri
  # Leave the track 2 packages if multiple packages fetched out.
  $clientPackages = $metadata | Where-Object { $_.GroupId -eq 'com.azure' }
  $nonClientPackages = $metadata | Where-Object { $_.GroupId -ne 'com.azure' -and !$clientPackages.Package.Contains($_.Package) }
  $uniquePackages = $clientPackages + $nonClientPackages
  # Get the artifacts name from blob storage
  $artifacts =  Get-BlobStorage-Artifacts -blobStorageUrl $BlobStorageUrl -blobDirectoryRegex "^java/(.*)/$" -blobArtifactsReplacement '$1'
  # Build up the artifact to service name mapping for GithubIo toc.
  $tocContent = Get-TocMapping -metadata $uniquePackages -artifacts $artifacts
  # Generate yml/md toc files and build site.
  GenerateDocfxTocContent -tocContent $tocContent -lang "Java"
}
function check-source-jar($artifactId, $groudId, $version) 
{
  $groudIdUrl = $groudId.Replace(".", "/")
  $MavenDownloadUrl = "$MavenDownloadSite/$groudIdUrl/$artifactId/$version"
  $sourceJarName = "$artifactId-$version-sources.jar"
  try 
  {
    $resp = Invoke-WebRequest $MavenDownloadUrl
    $source_jar_existence = $resp -and $resp.Links.href.Contains($sourceJarName)
    if (!$source_jar_existence) {
      Write-Host "The package $sourceJarName does not publish in site $MavenDownloadUrl. Please double check your release."
      return $false
    }
    # Download the maven package to local
    $MavenDownloadLink = "$MavenDownloadUrl/$sourceJarName"
    New-Item -ItemType Directory -Force -Path ./package > $null 2>&1
    Push-Location -Path ./package
    Invoke-WebRequest $MavenDownloadLink -OutFile "$artifactId-$version-sources.jar" >$null 2>&1
    jar xf "$artifactId-$version-sources.jar" >$null 2>&1
    $check_source_code_existence = Test-Path -Path ./com
    if (!$check_source_code_existence) {
      Write-Host "The package $sourceJarName does not contain any source code with path ./com. Please check whether it has real API doc to publish."
    }
    Pop-Location 
    # Clean up the package folder
    Remove-Item ./package -Recurse
    return $check_source_code_existence
  }
  catch 
  {
    return $false
  }
}
# a "package.json configures target packages for all the monikers in a Repository, it also has a slightly different
# schema than the moniker-specific json config that is seen in python and js
# details on CSV schema can be found here
# https://review.docs.microsoft.com/en-us/help/onboard/admin/reference/dotnet/documenting-nuget?branch=master#set-up-the-ci-job
function Update-java-CIConfig($ciRepo, $locationInDocRepo)
{ 
  # Add ignore package list in file eng/docms-ignore-packages.txt, so that we can exclude the package from package.json in a quick way.
  $ignorePackages = @()
  Write-Host $ignorePackagePath
  if (Test-Path $ignorePackagePath)
  {
    $ignorePackages = [Array](Get-Content $ignorePackagePath | ForEach-Object { ($_ -replace "#.*", "").Trim() } | Where-Object { $_ -ne "" })
    Write-Host $ignorePackages
  }
  Write-Host "Updating the package.json in Java"
  # Read release csv file, and filter out by New=true, Hide!=true
  $metadata = Get-CSVMetadata -MetadataUri $MetadataUri | Where-Object {$_.New -eq "true"}  | Where-Object {$_.Hide -ne "true"} 
  $preview =  @{
    language = "java"
    output_path = "preview/docs-ref-autogen"
    packages = @()
  }
  $latest = @{
    language = "java"
    output_path = "docs-ref-autogen"
    packages = @()
  }
  # Read package list from package.json
  $pkgJsonLoc = (Join-Path -Path $ciRepo -ChildPath $locationInDocRepo)
  if (-not (Test-Path $pkgJsonLoc)) {
    Write-Error "Unable to locate package csv at location $pkgJsonLoc, exiting."
    exit(1)
  }
  # Read the pacakge from package.json, if not show up in release csv, retain it in package.json as it is.
  $allCSVRows = Get-Content $pkgJsonLoc | Out-String | ConvertFrom-Json
  # Used for retaining config from package json. E.g. excludePackaages.
  $latestHash = @{}
  $previewHash = @{}
  $allCSVRows[0].packages | foreach { $latestHash["$($_.packageGroupId):$($_.packageArtifactId)"] = $_ }
  $allCSVRows[1].packages | foreach { $previewHash["$($_.packageGroupId):$($_.packageArtifactId)"] = $_ }
  for ($i=0; $i -lt $metadata.Length; $i++) {
    if (!$metadata[$i].Package) {
      continue
    }
    # Fill in the latest first
    if ($metadata[$i].VersionGA) {
      # Check whether the source jar exists in maven site or whether it has source code API docs. Exclude from package json if returns false.
      if (!(check-source-jar -artifactId $metadata[$i].Package -groudId $metadata[$i].GroupId -version $metadata[$i].VersionGA)) {
        continue
      }
      $latest_object = @{}
      $latest_object["packageDownloadUrl"] = $MavenDownloadSite
      $latest_object["packageGroupId"] = $metadata[$i].GroupId
      $latest_object["packageArtifactId"] = $metadata[$i].Package
      $latest_object["packageVersion"] = $metadata[$i].VersionGA
      $excludePackages = $latestHash["$($metadata[$i].GroupId):$($metadata[$i].Package)"].excludePackages
      if ($excludePackages) {
        $latest_object["excludepackages"] = $excludePackages
      }
      $latest.packages += $latest_object
    }
    # Then fill in the preview 
    if ($metadata[$i].VersionPreview) {
      if (!(check-source-jar -artifactId $metadata[$i].Package -groudId $metadata[$i].GroupId -version $metadata[$i].VersionPreview)) {
        continue
      }
      $preview_object = @{}
      $preview_object["packageDownloadUrl"] = $MavenDownloadSite
      $preview_object["packageGroupId"] = $metadata[$i].GroupId
      $preview_object["packageArtifactId"] = $metadata[$i].Package
      $preview_object["packageVersion"] = $metadata[$i].VersionPreview
      # We currently only retain the excludePacakges in package.json.
      $excludePackages = $previewHash["$($metadata[$i].GroupId):$($metadata[$i].Package)"].excludePackages
      if ($excludePackages) {
        $preview_object["excludepackages"] = $excludePackages
      }
      $preview.packages += $preview_object
    }
  }
  $jsonRepresentation = @($latest, $preview)

  # Check the package which is not in release csv but in package.json. Maintain the config in pacakge.json
  for ($i=0; $i -lt $allCSVRows.Length; $i++) {
    $packages = $allCSVRows[$i].packages
    for ($j=0; $j -lt $packages.Length; $j++) {
      $pkg = $packages[$j].packageArtifactId
      $groupId = $packages[$j].packageGroupId
      if (!($metadata.Package -contains $pkg -and $metadata.GroupId -contains $groupId)) {
        $jsonRepresentation[$i].packages += $packages[$j]
      }
    }
  }
  # remove package from package.json
  for ($i=0; $i -lt $jsonRepresentation.Length; $i++) {
    $packages = $jsonRepresentation[$i].packages
    for ($j=0; $j -lt $packages.Length; $j++) {
      if ($ignorePackages -contains "$($packages[$j].packageGroupId):$($packages[$j].packageArtifactId)") {
        Write-Host "The package $($packages[$j].packageGroupId):$($packages[$j].packageArtifactId) exists in ignore list."
        $jsonRepresentation[$i].packages = $jsonRepresentation[$i].packages - $packages[$j]
      }
    }
  }
  $jsonRepresentation | ConvertTo-Json -depth 100 | Out-File $pkgJsonLoc
}

# function is used to filter packages to submit to API view tool
function Find-java-Artifacts-For-Apireview($artifactDir, $pkgName)
{
  # Find all source jar files in given artifact directory
  # Filter for package in "com.azure*" groupid.
  $artifactPath = Join-Path $artifactDir "com.azure*" $pkgName
  Write-Host "Checking for source jar in artifact path $($artifactPath)"
  $files = Get-ChildItem -Recurse "${artifactPath}" | Where-Object -FilterScript {$_.Name.EndsWith("sources.jar")}
  if (!$files)
  {
    Write-Host "$($artifactPath) does not have any package"
    return $null
  }
  elseif($files.Count -ne 1)
  {
    Write-Host "$($artifactPath) should contain only one (1) published source jar package"
    Write-Host "No of Packages $($files.Count)"
    return $null
  }

  $packages = @{
    $files[0].Name = $files[0].FullName
  }

  return $packages
}

function SetPackageVersion ($PackageName, $Version, $ServiceDirectory, $ReleaseDate, $BuildType = "client", $GroupId = "com.azure", $PackageProperties)
{
  if ($PackageProperties)
  {
    $GroupId = $PackageProperties.Group
    if ($PackageProperties.SdkType -eq "client")
    {
      if ($PackageProperties.IsNewSDK) {
        $BuildType = "client"
      }
      else {
        $BuildType = "data"
      }
    }
  }

  if($null -eq $ReleaseDate)
  {
    $ReleaseDate = Get-Date -Format "yyyy-MM-dd"
  }
  python "$EngDir/versioning/set_versions.py" --build-type $BuildType --new-version $Version --ai $PackageName --gi $GroupId
  python "$EngDir/versioning/update_versions.py" --update-type library --build-type $BuildType --sr
  & "$EngCommonScriptsDir/Update-ChangeLog.ps1" -Version $Version -ServiceDirectory $ServiceDirectory -PackageName $PackageName `
  -Unreleased $False -ReplaceLatestEntryTitle $True -ReleaseDate $ReleaseDate
}

function GetExistingPackageVersions ($PackageName, $GroupId=$null)
{
  try {
    $Uri = 'https://search.maven.org/solrsearch/select?q=g:"' + $GroupId + '"+AND+a:"' + $PackageName +'"&core=gav&rows=20&wt=json'
    $existingVersion = Invoke-RestMethod -Method GET -Uri $Uri
    $existingVersion = $existingVersion.response.docs.v
    [Array]::Reverse($existingVersion)
    return $existingVersion
  }
  catch {
    LogError "Failed to retrieve package versions. `n$_"
    return $null
  }
}
